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

        UnitEntry farEndUnit = db.getUnitByName(farEndUnitName);
        getImpactedLinks(farEndUnit, impactedLinks);

        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleLinkConnected(String nearEndUnitName, String nearEndPortName, String endpointSerialNumber, String endpointPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        LinkEntry link = db.getLinkByUnitAndPortNames(nearEndUnitName, nearEndPortName);
        if (link == null || !link.isLinkConfigured()) {
            db.registerLinkConnected(nearEndUnitName, nearEndPortName, endpointSerialNumber, endpointPortName);
            return;
        }

        getImpactedLinks(link.getFarEndPort().getUnit(), impactedLinks);
        db.registerLinkConnected(nearEndUnitName, nearEndPortName, endpointSerialNumber, endpointPortName);
        getImpactedLinks(link.getFarEndPort().getUnit(), impactedLinks);

        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleLinkUnconfigured(String nearEndUnitName, String nearEndPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        LinkEntry link = db.getLinkByUnitAndPortNames(nearEndUnitName, nearEndPortName);
        if (link == null) {
            System.err.println("Link on unit " + nearEndUnitName + " and port " + nearEndPortName + " not found");
            return;
        }

        UnitEntry farEndUnit = link.getFarEndPort().getUnit();

        getImpactedLinks(farEndUnit, impactedLinks);
        db.unconfigureLink(link);
        getImpactedLinks(farEndUnit, impactedLinks);

        impactedLinks.remove(link);

        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleLinkDisconnected(String nearEndUnitName, String nearEndPortName) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        LinkEntry link = db.getLinkByUnitAndPortNames(nearEndUnitName, nearEndPortName);
        if (link == null) {
            System.err.println("Link on unit " + nearEndUnitName + " and port " + nearEndPortName + " not found");
            return;
        }

        if (!link.isLinkConfigured()) {
            return;
        }

        UnitEntry farEndUnit = link.getFarEndPort().getUnit();

        getImpactedLinks(farEndUnit, impactedLinks);
        db.disconnectLink(link);
        getImpactedLinks(farEndUnit, impactedLinks);

        runMisconnectionAlgorithm(impactedLinks);
    }

    public void handleUnitSNConfigure(String unitName, String serialNumber) {
        Set<LinkEntry> impactedLinks = new HashSet<>();

        UnitEntry unit = db.getUnitByName(unitName);
        if (unit == null) {
            System.err.println("Unit not found");
            return;
        }

        getImpactedLinks(unit, impactedLinks);
        unit.setCMSerialNumber(serialNumber);
        getImpactedLinks(unit, impactedLinks);

        runMisconnectionAlgorithm(impactedLinks);
    }

    private void getImpactedLinks(UnitEntry unit, Set<LinkEntry> impactedLinks) {
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

    private void runMisconnectionAlgorithm(Set<LinkEntry> impactedLinks) {
        for(LinkEntry link : impactedLinks) {
            FaultEntry fault = unitMisconnectionCheck(link);
            if (fault == null)
                fault = portMisconnectionCheck(link);

            link.setFault(fault);
            con.handleLinkFaultChange(link.getName(), fault != null ? fault.getInfo() : "");
        }
    }

    private FaultEntry unitMisconnectionCheck(LinkEntry link) {
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

    private FaultEntry portMisconnectionCheck(LinkEntry link) {
        if (!link.isLinkConfigured() || !link.isLinkConnected())
            return null;

        if (!link.getFarEndPort().getName().equals(link.getEndpointPortName()))
            return createNewFault(FaultType.PORT_MISCONNECTION_FAULT, link);

        return null;
    }

    private boolean checkSerialNumberMismatch(LinkEntry link) {
        Set<String> serialNumbers = new HashSet<>();

        for (LinkEntry l: db.getLinksOfUnit(link.getFarEndPort().getUnit(), PortRole.SECONDARY)) {
            if (!l.isLinkConnected() || !l.isLinkConfigured())
                continue;
            if (db.findUnitByCMSerialNumber(l.getEndpointSerialNumber()) == null)
                serialNumbers.add(l.getEndpointSerialNumber());
        }

        return serialNumbers.size() > 1;
    }

    private boolean checkUnitMismatch(LinkEntry link) {
        Set<UnitEntry> units = new HashSet<>();

        for (LinkEntry l: db.getLinksByEndpointSerialNumber(link.getEndpointSerialNumber())) {
            if (!l.isLinkConnected() || !l.isLinkConfigured())
                continue;
            if (l.getFarEndPort().getUnit().getCMSerialNumber().isEmpty())
                units.add(l.getFarEndPort().getUnit());
        }

        return units.size() > 1;
    }

    private FaultEntry createNewFault(FaultType type, LinkEntry link) {
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
            faultInfo = "Unit misconnection found on link. Mismatch detected on link(s) between " +
                    "connected and configured units of this link. Please check your connection.";
        }

        return new FaultEntry(faultInfo, type);
    }
}
