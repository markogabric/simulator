package com.example.simulator.View;

import com.example.simulator.Topology.PortRole;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

public class Port extends Circle {
    private final String name;
    private final Unit hostUnit;
    private final PortRole portRole;
    private Link link;

    public Port(double x, double y, Unit unit, String name, PortRole role) {
        this.name = name;
        this.hostUnit = unit;
        this.portRole = role;

        this.setCenterX(x);
        this.setCenterY(y);
        this.setRadius(4);

        this.setAppearence();
    }

    public String getName() {
        return this.name;
    }

    public Unit getHostUnit() {
        return this.hostUnit;
    }

    public PortRole getPortRole() {
        return this.portRole;
    }

    public boolean hasLink() {
        return this.link != null;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public void removeLink() {
        this.link = null;
    }

    public void setAppearence() {
        Stop[] stops = new Stop[] { new Stop(0, Color.BLACK), new Stop(1, Color.web("#eeeeee"))};
        this.setRadius(8);
        this.setStroke(Color.BLACK);
        this.setStrokeType(StrokeType.INSIDE);
        RadialGradient radialGradient = new RadialGradient(0, 0.1, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE, stops);
        this.setFill(radialGradient);
    }

    public void setHighlight(boolean isSet) {
        if (isSet) {
            this.setStroke(Color.RED);
        } else {
            this.setStroke(Color.BLACK);
        }
    }
}
