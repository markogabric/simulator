package com.example.simulator.Topology;

public class LinkEntry {
    private Integer id;
    private String name;
    private boolean isLinkConfigured;
    private boolean isLinkConnected;
    private PortEntry nearEndPort;
    private PortEntry farEndPort;
    private String endpointSerialNumber;
    private String endpointPortName;

    private FaultEntry fault;

    public LinkEntry(Integer id, String name, PortEntry nearEndPort, PortEntry farEndPort) {
        this.id = id;
        this.name = name;
        this.isLinkConfigured = true;
        this.isLinkConnected = false;
        this.nearEndPort = nearEndPort;
        this.farEndPort = farEndPort;
        this.endpointSerialNumber = "";
        this.endpointPortName = "";
    }

    public LinkEntry(Integer id, PortEntry nearEndPort, String endpointSerialNumber, String endpointPortName) {
        this.id = id;
        this.name = "";
        this.isLinkConfigured = false;
        this.isLinkConnected = true;
        this.nearEndPort = nearEndPort;
        this.farEndPort = null;
        this.endpointSerialNumber = endpointSerialNumber;
        this.endpointPortName = endpointPortName;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public boolean isLinkConfigured() { return isLinkConfigured; }
    public boolean isLinkConnected() { return isLinkConnected; }
    public PortEntry getNearEndPort() { return nearEndPort; }
    public PortEntry getFarEndPort() { return farEndPort; }
    public String getEndpointSerialNumber() { return endpointSerialNumber; }
    public String getEndpointPortName() { return endpointPortName; }

    public void setName(String name) { this.name = name; }
    public void setLinkConfigured(boolean isLinkConfigured) { this.isLinkConfigured = isLinkConfigured; }
    public void setLinkConnected(boolean isLinkConnected) { this.isLinkConnected = isLinkConnected; }
    public void setNearEndPort(PortEntry nearEndPort) {this.nearEndPort = nearEndPort; }
    public void setFarEndPort(PortEntry farEndPort) {this.farEndPort = farEndPort; }
    public void setEndpoints(String endpointSerialNumber, String endpointPortName) {
        this.endpointSerialNumber = endpointSerialNumber;
        this.endpointPortName = endpointPortName;
    }

    public void setFault(FaultEntry fault) { this.fault = fault; }
}
