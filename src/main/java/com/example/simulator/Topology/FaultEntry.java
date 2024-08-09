package com.example.simulator.Topology;

public class FaultEntry {
    private String info;
    private FaultType type;

    public FaultEntry(String info, FaultType type) {
        this.info = info;
        this.type = type;
    }

    public String getInfo() { return info; }
    public FaultType getType() { return type; }

    public void setInfo(String info) { this.info = info; }
    public void setType(FaultType type) { this.type = type; }
}
