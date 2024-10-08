package com.example.simulator;

import com.example.simulator.Topology.*;

import java.util.ArrayList;
import java.util.List;

public class TopologyManager {
    private List<UnitEntry> units;
    private List<PortEntry> ports;
    private List<LinkEntry> links;

    private Integer unitCounter;
    private Integer portCounter;
    private Integer linkCounter;

    public TopologyManager() {
        this.unitCounter = 0;
        this.portCounter = 0;
        this.linkCounter = 0;

        this.units = new ArrayList<UnitEntry>();
        this.ports = new ArrayList<PortEntry>();
        this.links = new ArrayList<LinkEntry>();
    }

    public UnitEntry getUnitById(Integer id) {
        for (UnitEntry unit : units) {
            if (unit.getId().equals(id)) {
                return unit;
            }
        }

         return null;
    }

    public PortEntry getPortById(Integer id) {
        for (PortEntry port : ports) {
            if (port.getId().equals(id)) {
                return port;
            }
        }

        return null;
    }

    public LinkEntry getLinkById(Integer id) {
        for (LinkEntry link : links) {
            if (link.getId().equals(id)) {
                return link;
            }
        }

        return null;
    }

    public UnitEntry getUnitByName(String unitName) {
        for (UnitEntry unit : this.units) {
            if (unit.getName().equals(unitName))
                return unit;
        }

        return null;
    }

    public LinkEntry getLinkByName(String linkName) {
        for (LinkEntry link : this.links) {
            if (link.getName().equals(linkName))
                return link;
        }

        return null;
    }

    public PortEntry getPortByUnitAndName(UnitEntry unit, String portName) {
        for (PortEntry port : this.ports) {
            if (port.getUnit().getId().equals(unit.getId()) && port.getName().equals(portName))
                return port;
        }

        return null;
    }

    public LinkEntry getLinkByUnitAndPortNames(String unitName, String portName) {
        UnitEntry unit = getUnitByName(unitName);
        if (unit == null)
            return null;

        PortEntry port = getPortByUnitAndName(unit, portName);
        if (port == null)
            return null;

        LinkEntry link = port.getLink();
        return link;
    }

    public LinkEntry getLinkByPort(UnitEntry unit, String portName) {
        PortEntry port = getPortByUnitAndName(unit, portName);
        if (port == null)
            return null;

        LinkEntry link = port.getLink();
        return link;
    }

    public List<LinkEntry> getLinksOfUnit(UnitEntry unit, PortRole role) {
        List<LinkEntry> links = new ArrayList<LinkEntry>();

        for (PortEntry port : this.ports) {
            if (!port.getUnit().getId().equals(unit.getId())) //TODO
                continue;

            if (port.getLink() != null)
                links.add(port.getLink());
        }

        return links;
    }

    public List<LinkEntry> getLinksByEndpointSerialNumber(String serialNumber) {
        List<LinkEntry> links = new ArrayList<LinkEntry>();

        for (LinkEntry link : this.links) {
            if (!link.isLinkConfigured())
                continue;
            if (link.getEndpointSerialNumber().equals(serialNumber))
                links.add(link);
        }

        return links;
    }

    public void registerUnit(String unitName, UnitType type) {
        Integer id = ++this.unitCounter;
        UnitEntry unit = new UnitEntry(id, unitName, type);
        units.add(unit);
    }

    public void registerPort(String portName, PortRole portRole, String unitName) {
        UnitEntry unit = getUnitByName(unitName);
        if (unit == null) {
            System.err.println("Unit " + unitName + " not found");
            return;
        }

        Integer id = ++this.portCounter;
        PortEntry port = new PortEntry(id, portName, portRole, unit);
        ports.add(port);
    }

    public void registerLinkConfigured(String linkName, String nearEndUnitName, String nearEndPortName, String farEndUnitName, String farEndPortName) {
        UnitEntry nearEndUnit = getUnitByName(nearEndUnitName);
        if (nearEndUnit == null) {
            System.err.println("Unit " + nearEndUnitName + " not found");
            return;
        }

        UnitEntry farEndUnit = getUnitByName(farEndUnitName);
        if (farEndUnit == null) {
            System.err.println("Unit " + nearEndUnitName + " not found");
            return;
        }

        PortEntry nearEndPort = getPortByUnitAndName(nearEndUnit, nearEndPortName);
        if (nearEndPort == null) {
            System.err.println("Port " + nearEndPortName + " on unit " + nearEndUnit.getName() + " not found");
            return;
        }

        PortEntry farEndPort = getPortByUnitAndName(farEndUnit, farEndPortName);
        if (farEndPort == null) {
            System.err.println("Port " + farEndPortName + " on unit " + farEndUnit.getName() + " not found");
            return;
        }

        LinkEntry link = getLinkByPort(nearEndUnit, nearEndPortName);
        if (link != null) {
            link.setName(linkName);
            link.setLinkConfigured(true);
            link.setFarEndPort(farEndPort);
            farEndPort.setLink(link);
            return;
        }

        Integer id = ++this.linkCounter;
        LinkEntry newLinkEntry = new LinkEntry(id, linkName, nearEndPort, farEndPort);
        nearEndPort.setLink(newLinkEntry);
        farEndPort.setLink(newLinkEntry);
        links.add(newLinkEntry);
    }

    public void registerLinkConnected(String nearEndUnitName, String nearEndPortName, String endpointSerialNumber, String endpointPortName) {
        UnitEntry nearEndUnit = getUnitByName(nearEndUnitName);
        if (nearEndUnit == null) {
            System.err.println("Unit " + nearEndUnitName + " not found");
            return;
        }

        PortEntry nearEndPort = getPortByUnitAndName(nearEndUnit, nearEndPortName);
        if (nearEndPort == null) {
            System.err.println("Port " + nearEndPortName + " on unit " + nearEndUnit.getName() + " not found");
            return;
        }

        LinkEntry link = getLinkByPort(nearEndUnit, nearEndPortName);
        if (link != null) {
            link.setEndpoints(endpointSerialNumber, endpointPortName);
            link.setLinkConnected(true);
            return;
        }

        Integer id = ++this.linkCounter;
        LinkEntry newLinkEntry = new LinkEntry(id, nearEndPort,endpointSerialNumber,endpointPortName);
        nearEndPort.setLink(newLinkEntry);
        links.add(newLinkEntry);
    }

    public void unconfigureLink(LinkEntry link) {
        if (link.isLinkConnected()) {
            link.setName("");
            link.setFarEndPort(null);
            link.setLinkConfigured(false);
            return;
        }

        link.getNearEndPort().setLink(null);
        link.getFarEndPort().setLink(null);

        links.remove(link);
    }

    public void disconnectLink(LinkEntry link) {
        if (link.isLinkConfigured()) {
            link.setEndpoints("", "");
            link.setLinkConnected(false);
            return;
        }

        link.getNearEndPort().setLink(null);
        link.getFarEndPort().setLink(null);

        links.remove(link);
    }

    public UnitEntry findUnitByCMSerialNumber(String serialNumber) {
        for (UnitEntry unit : units) {
            if (unit.getCMSerialNumber().equals(serialNumber)) {
                return unit;
            }
        }

        return null;
    }

    public void clearTopology() {
        this.links.clear();
        this.linkCounter = 0;

        this.ports.clear();
        this.portCounter = 0;

        this.units.clear();
        this.unitCounter = 0;
    }
}
