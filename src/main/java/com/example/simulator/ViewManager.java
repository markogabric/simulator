package com.example.simulator;

import com.example.simulator.View.Link;
import com.example.simulator.View.Port;
import com.example.simulator.View.Unit;
import com.example.simulator.View.ViewType;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class ViewManager {
    private double offsetX;
    private double offsetY;
    private double xOffset = 0;

    List<Unit> units;
    List<Link> links;

    private final Controller con;

    public ViewManager (Controller controller) {
        this.con = controller;
        this.units = new ArrayList<>();
        this.links = new ArrayList<>();
    }

    public void createUnitInView(Unit confUnit, Unit connUnit) {
        units.add(confUnit);
        units.add(connUnit);

        confUnit.setLayoutX(100 + xOffset);
        confUnit.setLayoutY(100);
        connUnit.setLayoutX(100 + xOffset);
        connUnit.setLayoutY(100);

        xOffset += 20;

        con.confPane.getChildren().add(confUnit);
        con.connPane.getChildren().add(connUnit);
    }

    public Link createLinkInView(String linkName, Port nearEndPort, Port farEndPort) {
        AnchorPane view;

        double startX = nearEndPort.localToScene(nearEndPort.getCenterX(), nearEndPort.getCenterY()).getX();
        double startY = nearEndPort.localToScene(nearEndPort.getCenterX(), nearEndPort.getCenterY()).getY();
        double endX = farEndPort.localToScene(farEndPort.getCenterX(), farEndPort.getCenterY()).getX();
        double endY = farEndPort.localToScene(farEndPort.getCenterX(), farEndPort.getCenterY()).getY();

        view = nearEndPort.getHostUnit().getViewType() == ViewType.CONFIGURED ? con.confPane : con.connPane;

        Point2D startInView = view.sceneToLocal(startX, startY);
        Point2D endInView = view.sceneToLocal(endX, endY);

        Link link = new Link(startInView.getX(), startInView.getY(), endInView.getX(), endInView.getY());
        link.setName(linkName);
        link.setNearEndPort(nearEndPort);
        link.setFarEndPort(farEndPort);

        nearEndPort.setLink(link);
        farEndPort.setLink(link);

        nearEndPort.getHostUnit().setLinksExist(true);
        farEndPort.getHostUnit().setLinksExist(true);

        links.add(link);

        view.getChildren().add(link);
        return link;
    }

    public void deleteUnitFromView(Unit unit) {
        Unit confUnit = null, connUnit = null;

        for (Unit u : units) {
            if (!u.getName().equals(unit.getName())) {
                continue;
            }

            if (u.getViewType() == ViewType.CONFIGURED) {
                confUnit = u;
            } else if (u.getViewType() == ViewType.CONNECTED) {
                connUnit = u;
            }
        }

        if (confUnit == null || connUnit == null) {
            return;
        }

        if (confUnit.getLinksExist() || connUnit.getLinksExist()) {
            return;
        }

        con.confPane.getChildren().remove(confUnit);
        con.connPane.getChildren().remove(connUnit);

        units.remove(unit);
        this.xOffset -= 20;
    }

    public void deleteLinkFromView(Link link, ViewType viewType) {
        if (viewType == ViewType.CONFIGURED) {
            con.confPane.getChildren().remove(link);
        } else {
            con.connPane.getChildren().remove(link);
        }

        link.getNearEndPort().setLink(null);
        link.getFarEndPort().setLink(null);

        links.remove(link);

        Unit nearEndUnit = link.getNearEndPort().getHostUnit();
        nearEndUnit.setLinksExist(checkLinksExist(nearEndUnit));

        Unit farEndUnit = link.getFarEndPort().getHostUnit();
        farEndUnit.setLinksExist(checkLinksExist(farEndUnit));
    }

    public boolean checkLinksExist(Unit unit) {
        for (Link link : links) {
            if (link.getNearEndPort().getHostUnit().equals(unit) || link.getFarEndPort().getHostUnit().equals(unit)) {
                return true;
            }
        }
        return false;
    }

    public void updateDetails(TextField serialNumberField, ViewType viewType, String serialNumber) {
        serialNumberField.setEditable(viewType != ViewType.CONNECTED);
        serialNumberField.setText(serialNumber);
    }

    public void clearDetails() {
        con.detailsNameLabel.setText("Name");
        con.detailsNameField.setText("");

        con.detailsNearLabel.setVisible(false);
        con.detailsNearField.setVisible(false);

        con.detailsFarLabel.setVisible(false);
        con.detailsFarField.setVisible(false);

        con.detailsSerialNumberLabel.setVisible(false);
        con.detailsSerialNumberField.setVisible(false);
        con.detailsSerialNumberField.setEditable(false);
        con.inputButton.setVisible(false);

        con.detailsAlarmsLabel.setVisible(false);
        con.detailsAlarmsField.setVisible(false);
    }

    public void handleDetailsUnit(Unit unit) {
        clearDetails();

        con.detailsNameLabel.setText("Unit name");
        con.detailsNameField.setText(unit.getName());

        con.detailsSerialNumberLabel.setVisible(true);
        con.detailsSerialNumberField.setVisible(true);
        con.inputButton.setVisible(true);

        if (unit.getViewType() == ViewType.CONFIGURED) {
            con.detailsSerialNumberLabel.setText("Custom serial number");
            con.detailsSerialNumberField.setEditable(true);
            con.detailsSerialNumberField.setText(unit.getSerialNumber());
            con.inputButton.setDisable(false);
        } else {
            con.detailsSerialNumberLabel.setText("Hardware serial number");
            con.detailsSerialNumberField.setEditable(false);
            con.detailsSerialNumberField.setText(unit.getSerialNumber());
            con.inputButton.setDisable(true);
        }
    }

    public void handleDetailsLink(Link link) {
        clearDetails();

        con.detailsNameLabel.setText("Link name");
        con.detailsNameField.setText(link.getName());

        con.detailsNearLabel.setVisible(true);
        con.detailsNearField.setVisible(true);
        con.detailsNearField.setText(link.getNearEndPort().getHostUnit().getName());

        con.detailsFarLabel.setVisible(true);
        con.detailsFarField.setVisible(true);
        con.detailsFarField.setText(link.getFarEndPort().getHostUnit().getName());

        con.detailsAlarmsLabel.setVisible(true);
        con.detailsAlarmsField.setVisible(true);
        con.detailsAlarmsField.setWrapText(true);

        if (!link.getFaultInfo().isEmpty()) {
            con.detailsAlarmsField.setText(link.getFaultInfo());
            con.detailsAlarmsField.setStyle("-fx-text-fill: red;");
        } else {
            con.detailsAlarmsField.setText("");
        }
    }

    public Link getLinkByName(String linkName) {
        for (Link link: links) {
            if (link.getName().equals(linkName))
                return link;
        }

        return null;
    }

    public void setViewLinkFault(String linkName, String faultInfo) {
        Link link = getLinkByName(linkName);
        if (link == null) {
            System.out.println("Link not found: " + linkName);
            return;
        }

        link.setFaultInfo(faultInfo);

        if (!faultInfo.isEmpty()) {
            link.setStyle("-fx-stroke: red;");
        } else {
            link.setStyle("-fx-stroke: black;");
        }
    }

    public void clearView() {
        con.connPane.getChildren().clear();
        con.confPane.getChildren().clear();

        this.links.clear();
        this.units.clear();

        this.xOffset = 0;
    }
}
