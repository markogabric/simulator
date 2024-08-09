package com.example.simulator;

import com.example.simulator.Topology.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EndpointsManager {
    private final Controller con;
    private final TopologyManager db;

    public EndpointsManager(Controller controller) {
        this.con = controller;
        this.db = con.getDatabase();
    }

    public void handleLinkConfigured(String linkName, String nearEndUnitName, String nearEndPortName, String farEndUnitName, String farEndPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();
        db.registerLinkConfigured(linkName, nearEndUnitName, nearEndPortName, farEndUnitName, farEndPortName);

        UnitEntry farEndUnit = db.findUnitByName(farEndUnitName);
        getImpactedLinks(farEndUnit, impactedLinks);

        System.out.println("Link configured");
        for (LinkEntry linkEntry : impactedLinks) {
            System.out.println(linkEntry);
        }
        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleLinkConnected(String nearEndUnitName, String nearEndPortName, String endpointSerialNumber, String endpointPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        LinkEntry link = db.findLinkByUnitAndPortNames(nearEndUnitName, nearEndPortName);
        if (link == null || !link.isLinkConfigured()) {
            db.registerLinkConnected(nearEndUnitName, nearEndPortName, endpointSerialNumber, endpointPortName);
            return;
        }

        getImpactedLinks(link.getFarEndPort().getUnit(), impactedLinks);
        db.registerLinkConnected(nearEndUnitName, nearEndPortName, endpointSerialNumber, endpointPortName);
        getImpactedLinks(link.getFarEndPort().getUnit(), impactedLinks);

        System.out.println("Link connected");
        for (LinkEntry linkEntry : impactedLinks) {
            System.out.println(linkEntry);
        }
        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleLinkUnconfigured(String nearEndUnitName, String nearEndPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        LinkEntry link = db.findLinkByUnitAndPortNames(nearEndUnitName, nearEndPortName);
        if (link == null) {
            System.out.println("Link on unit " + nearEndUnitName + " and port " + nearEndPortName + " not found");
            return;
        }

        UnitEntry farEndUnit = link.getFarEndPort().getUnit();

        getImpactedLinks(farEndUnit, impactedLinks);
        db.unconfigureLink(link);
        getImpactedLinks(farEndUnit, impactedLinks);

        impactedLinks.remove(link);

        System.out.println("Link unconfigured");
        for (LinkEntry linkEntry : impactedLinks) {
            System.out.println(linkEntry);
        }
        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleLinkDisconnected(String nearEndUnitName, String nearEndPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        LinkEntry link = db.findLinkByUnitAndPortNames(nearEndUnitName, nearEndPortName);
        if (link == null) {
            System.out.println("Link on unit " + nearEndUnitName + " and port " + nearEndPortName + " not found");
            return;
        }

        if (!link.isLinkConfigured()) {
            return;
        }

        UnitEntry farEndUnit = link.getFarEndPort().getUnit();

        getImpactedLinks(farEndUnit, impactedLinks);
        db.disconnectLink(link);
        getImpactedLinks(farEndUnit, impactedLinks);

        System.out.println("Link disconnected");
        for (LinkEntry linkEntry : impactedLinks) {
            System.out.println(linkEntry);
        }
        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleUnitSNConfigure(String unitName, String serialNumber) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        UnitEntry unit = db.findUnitByName(unitName);
        if (unit == null) {
            System.out.println("Unit not found");
            return;
        }

        getImpactedLinks(unit, impactedLinks);
        unit.setCMSerialNumber(serialNumber);
        getImpactedLinks(unit, impactedLinks);

        runMisconnectionAlgorithm(impactedLinks);
    }

    public void getImpactedLinks(UnitEntry unit, Set<LinkEntry> impactedLinks) {
        List<LinkEntry> linksToUnit = db.getLinksOfUnit(unit, PortRole.SECONDARY);

        Set<String> hwSerialNumbers = new HashSet<>();
        for(LinkEntry link : linksToUnit) {
                hwSerialNumbers.add(link.getEndpointSerialNumber());
        }

        List<LinkEntry> linksBySerialNumber = new ArrayList<>();
        for(String serialNumber : hwSerialNumbers) {
            linksBySerialNumber.addAll(db.getLinksByEndpointSerialNumber(serialNumber));
        }

        if (!unit.getCMSerialNumber().isEmpty())
            linksBySerialNumber.addAll(db.getLinksByEndpointSerialNumber(unit.getCMSerialNumber()));

        List<UnitEntry> configuredUnits = new ArrayList<>();
        for (LinkEntry link : linksBySerialNumber) {
            configuredUnits.add(link.getFarEndPort().getUnit());
        }

        for (UnitEntry confUnit : configuredUnits) {
            impactedLinks.addAll(db.getLinksOfUnit(confUnit, PortRole.SECONDARY));
        }
    }

    public void runMisconnectionAlgorithm(Set<LinkEntry> impactedLinks) {
        for(LinkEntry link : impactedLinks) {
            FaultEntry fault = unitMisconnectionCheck(link);
            if (fault == null)
                fault = portMisconnectionCheck(link);

            link.setFault(fault);
            con.handleLinkFaultChange(link.getName(), fault != null ? fault.getInfo() : "");
        }
    }

    public FaultEntry unitMisconnectionCheck(LinkEntry link) {
        if (!link.isLinkConfigured() || !link.isLinkConnected())
            return null;

        if (!link.getFarEndPort().getUnit().getCMSerialNumber().isEmpty()) {
            if (!link.getFarEndPort().getUnit().getCMSerialNumber().equals(link.getEndpointSerialNumber()))
                return createNewFault(FaultType.UNIT_MISCONNECTION_FAULT, link);

            return null;
        }

        if (db.findUnitByCMSerialNumber(link.getEndpointSerialNumber()) != null)
            return createNewFault(FaultType.UNIT_MISCONNECTION_FAULT, link);

        if (checkSerialNumberMismatch(link) || checkUnitMismatch(link))
            return createNewFault(FaultType.UNIT_MISCONNECTION_FAULT, link);

        return null;
    }

    public FaultEntry portMisconnectionCheck(LinkEntry link) {
        if (!link.isLinkConfigured() || !link.isLinkConnected())
            return null;

        if (!link.getFarEndPort().getName().equals(link.getEndpointPortName()))
            return createNewFault(FaultType.PORT_MISCONNECTION_FAULT, link);

        return null;
    }

    public boolean checkSerialNumberMismatch(LinkEntry link) {
        Set<String> serialNumbers = new HashSet<>();

        for (LinkEntry l: db.getLinksOfUnit(link.getFarEndPort().getUnit(), PortRole.SECONDARY)) {
            if (!l.isLinkConnected() || !l.isLinkConfigured())
                continue;
            if (db.findUnitByCMSerialNumber(l.getEndpointSerialNumber()) == null)
                serialNumbers.add(l.getEndpointSerialNumber());
        }

        return serialNumbers.size() > 1;
    }

    public boolean checkUnitMismatch(LinkEntry link) {
        Set<UnitEntry> units = new HashSet<>();

        for (LinkEntry l: db.getLinksByEndpointSerialNumber(link.getEndpointSerialNumber())) {
            if (!l.isLinkConnected() || !l.isLinkConfigured())
                continue;
            if (l.getFarEndPort().getUnit().getCMSerialNumber().isEmpty())
                units.add(l.getFarEndPort().getUnit());
        }

        return units.size() > 1;
    }

    public FaultEntry createNewFault(FaultType type, LinkEntry link) {
        String faultInfo;

        if (type == FaultType.PORT_MISCONNECTION_FAULT) {
            faultInfo = String.format("Port misconnection found on link. Link configured to port %s of unit but connected to " +
                    "port %s. Please check your connection.",
                    link.getFarEndPort().getName(),
                    link.getEndpointPortName());
        } else if (type == FaultType.UNIT_MISCONNECTION_FAULT && !link.getFarEndPort().getUnit().getCMSerialNumber().isEmpty()){
            faultInfo = String.format("Unit misconnection found on link. Link configured to unit %s but connected to " +
                    "unit %s. Please check your connection.",
                    link.getFarEndPort().getUnit().getCMSerialNumber(),
                    link.getEndpointSerialNumber());
        } else {
            faultInfo = String.format("Unit misconnection found on link. Mismatch detected on link(s) between " +
                    "connected and configured units of this link. Please check your connection.");
        }

        System.out.println("FAULT: " + faultInfo);
        return new FaultEntry(faultInfo, type);
    }
}
