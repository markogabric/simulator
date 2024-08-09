package com.example.simulator.Topology;

public class PortEntry {
    private Integer id;
    private String name;
    private PortRole role;
    private UnitEntry unit;
    private LinkEntry link;

    public PortEntry(Integer id, String name, PortRole role, UnitEntry unitId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.unit = unitId;
    }

    public Integer getId() { return this.id; }
    public String getName() { return this.name; }
    public PortRole getRole() { return this.role; }
    public UnitEntry getUnit() { return this.unit; }
    public LinkEntry getLink() { return this.link; }

    public void setName(String name) { this.name = name; }
    public void setRole(PortRole role) { this.role = role; }
    public void setUnit(UnitEntry unit) { this.unit = unit; }
    public void setLink(LinkEntry link) { this.link = link; }
}
