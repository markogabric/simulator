package com.example.simulator.View;

import com.example.simulator.Topology.PortRole;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public abstract class Unit extends Pane {
    private static final int PORT_RADIUS = 8;

    private double mouseX, mouseY;

    protected String name;
    protected String serialNumber;
    protected ViewType viewType;
    protected List<Port> ports = new ArrayList<>();
    protected boolean linksExist;

    public Unit(String name, ViewType type, double x, double y) {
        this.name = name;
        this.viewType = type;
        this.linksExist = false;

        this.setLayoutX(x);
        this.setLayoutY(y);

        this.setPorts();
        this.makeDraggable();
    }

    protected void setBorderShadow() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.BLACK);
        dropShadow.setRadius(10);
        this.setEffect(dropShadow);
    }

    protected abstract void setPorts();
    public List<Port> getPorts() {return this.ports;}
    public String getName() {return this.name;}
    public boolean getLinksExist() {return this.linksExist;}
    public String getSerialNumber() {return this.serialNumber;}
    public ViewType getViewType() {return this.viewType;}

    public void setSerialNumber(String serialNumber) {this.serialNumber = serialNumber;}
    public void setLinksExist(boolean linksExist) {this.linksExist = linksExist;}

    protected void createPrimaryPort(String portName, double x, double y) {
        Label label = new Label(portName);
        Port port = new Port(x, y, this, portName, PortRole.PRIMARY);

        label.setLayoutX(x - 30);
        label.setLayoutY(y - PORT_RADIUS);

        this.ports.add(port);
        this.getChildren().addAll(label, port);
    }

    protected void createSecondaryPort(String portName, double x, double y) {
        Label label = new Label(portName);
        Port port = new Port(x, y, this, portName, PortRole.SECONDARY);

        label.setLayoutX(x + 20);
        label.setLayoutY(y - PORT_RADIUS);

        this.ports.add(port);
        this.getChildren().addAll(label, port);
    }

    protected void makeDraggable() {
        this.setOnMousePressed(event -> {
            this.mouseX = event.getSceneX();
            this.mouseY = event.getSceneY();
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            if (linksExist)
                return;

            double deltaX = event.getSceneX() - mouseX;
            double deltaY = event.getSceneY() - mouseY;

            relocate(getLayoutX() + deltaX, getLayoutY() + deltaY);

            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            event.consume();
        });
    }

}
