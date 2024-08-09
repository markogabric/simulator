package com.example.simulator.Topology;

public class UnitEntry {
    private Integer id;
    private String name;
    private UnitType type;
    private String CMSerialNumber;

    public UnitEntry(Integer id, String name, UnitType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.CMSerialNumber = "";
    }

    public Integer getId() { return this.id; }
    public String getName() { return this.name; }
    public UnitType getType() { return this.type; }
    public String getCMSerialNumber() { return this.CMSerialNumber; }

    public void setCMSerialNumber(String CMSerialNumber) { this.CMSerialNumber = CMSerialNumber; }
}
