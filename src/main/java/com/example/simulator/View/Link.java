package com.example.simulator.View;

import javafx.scene.shape.Line;

public class Link extends Line {
    private String name;
    private ViewType viewType;
    private Port nearEndPort;
    private Port farEndPort;
    private String faultInfo;

    public Link(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
        this.setStrokeWidth(6);
    }

    public String getName() {
        return name;
    }

    public Port getNearEndPort() {
        return nearEndPort;
    }

    public Port getFarEndPort() {
        return farEndPort;
    }

    public String getFaultInfo() { return faultInfo; }

    public void setName(String name) {
        this.name = name;
    }

    public void setNearEndPort(Port nearEndPort) {
        this.nearEndPort = nearEndPort;
    }

    public void setFarEndPort(Port farEndPort) {
        this.farEndPort = farEndPort;
    }

    public void setFaultInfo(String faultInfo) { this.faultInfo = faultInfo; }
}
