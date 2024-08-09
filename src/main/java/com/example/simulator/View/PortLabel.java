package com.example.simulator.View;

import javafx.scene.control.Label;

public class PortLabel extends Label {
    private Integer labelID;
    private Unit unit;

    public PortLabel(double x, double y, Unit unit, Integer labelID){
        this.setLayoutX(x);
        this.setLayoutY(y);
        this.unit = unit;
        this.labelID = labelID;
    }

    public Integer getLabelID(){
        return this.labelID;
    }
}
