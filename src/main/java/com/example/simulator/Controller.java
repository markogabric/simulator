package com.example.simulator;

import java.net.URL;
import java.util.*;

import com.example.simulator.Topology.PortRole;
import com.example.simulator.Topology.UnitType;
import com.example.simulator.View.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class Controller implements Initializable {
    @FXML
    public TabPane tabPane;
    @FXML
    public AnchorPane confPane;
    @FXML
    public AnchorPane connPane;
    @FXML
    public Pane unit;
    @FXML
    public SplitPane splitPane;
    @FXML
    public Label details;
    @FXML
    public Button DUbutton, RUbutton, inputButton, clearButton;
    @FXML
    public Label detailsNameLabel, detailsNearLabel, detailsFarLabel, detailsSerialNumberLabel, detailsAlarmsLabel;
    @FXML
    public TextField detailsNameField, detailsNearField, detailsFarField, detailsSerialNumberField;
    @FXML
    public TextArea detailsAlarmsField;

    private TopologyManager db;
    private EndpointsManager em;
    private ViewManager vm;

    private int unitCounter = 0;

    private List<Port> clickedPorts;
    private Unit selectedUnit;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.clickedPorts = new ArrayList<>();

        db = new TopologyManager();
        vm = new ViewManager(this);
        em = new EndpointsManager(this);

        splitPane.lookupAll(".split-pane-divider").forEach(div -> {
            div.setStyle("-fx-max-width: 0; -fx-max-height: 0; -fx-padding: 0;");
        });

        detailsSerialNumberField.setEditable(false);
        inputButton.setDisable(true);
        inputButton.setOnMouseClicked((event) -> {
            handleSerialNumberInput();
        });
        clearButton.setOnMouseClicked((event) -> {
            clearAll();
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            vm.clearDetails();
        });


        enableUnitButton(DUbutton, UnitType.DIGITAL_UNIT);
        enableUnitButton(RUbutton, UnitType.RADIO_UNIT);

        vm.clearDetails();
    }

    public TopologyManager getDatabase() { return this.db; }

    public void setSize() {
        splitPane.setDividerPositions(0.2, 0.8);
    }

    public void setListeners(ChangeListener<Number> resizeListener) {
        splitPane.widthProperty().addListener(resizeListener);
        splitPane.heightProperty().addListener(resizeListener);
    }

    public void enableUnitButton(Button button, UnitType unitType) {
        button.setDisable(false);
        button.setOnMouseClicked((mouseEvent) -> {
            createUnit(unitType);
        });
    }

    private void createUnit(UnitType unitType) {
        String unitName;
        Unit confUnit;
        Unit connUnit;

        unitCounter++;

        switch (unitType) {
            case DIGITAL_UNIT -> {
                unitName = "DU" + unitCounter;
                confUnit = new DigitalUnit(unitName, ViewType.CONFIGURED, 0, 0);
                connUnit = new DigitalUnit(unitName, ViewType.CONNECTED, 0, 0);
                connUnit.setSerialNumber(createRandomSerialNumber());
                break;
            }
            case RADIO_UNIT -> {
                unitName = "RU" + unitCounter;
                confUnit = new RadioUnit(unitName, ViewType.CONFIGURED, 0, 0);
                connUnit = new RadioUnit(unitName, ViewType.CONNECTED, 0, 0);
                connUnit.setSerialNumber(createRandomSerialNumber());
                break;
            }
            default -> {
                unitName = "";
                confUnit = null;
                connUnit = null;
                break;
            }
        }
        if (confUnit == null) {
            return;
        }

        confUnit.setOnMouseClicked((event) -> {
            handleUnitClick(confUnit, event);
        });

        connUnit.setOnMouseClicked((event) -> {
            handleUnitClick(connUnit, event);
        });

        db.registerUnit(unitName, unitType);

        initializePorts(confUnit);
        initializePorts(connUnit);

        confUnit.getPorts().forEach(port -> {
            db.registerPort(port.getName(), port.getPortRole(), confUnit.getName());
        });

        vm.createUnitInView(confUnit, connUnit);
    }

    private void initializePorts(Unit unit) {
        List<Port> ports = unit.getPorts();
        ports.forEach(port -> {
            port.setOnMouseClicked((event) -> {
                handlePortClick(port);
            });
        });
    }

    private void handleUnitClick(Unit unit, MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            selectedUnit = unit;
            vm.handleDetailsUnit(unit);
            return;
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteOption = new MenuItem("Delete");
        deleteOption.setOnAction(e -> {
            vm.deleteUnitFromView(unit);
        });
        contextMenu.getItems().addAll(deleteOption);

        unit.setOnContextMenuRequested(e -> {
            contextMenu.show(unit, event.getScreenX(), event.getScreenY());
        });
    }

    public void handleSerialNumberInput() {
        if (selectedUnit == null) {
            return;
        }

        String serialNumber = detailsSerialNumberField.getText();
        selectedUnit.setSerialNumber(serialNumber);
        em.handleUnitSNConfigure(selectedUnit.getName(), serialNumber);
        vm.handleDetailsUnit(selectedUnit);
    }

    private void handlePortClick(Port port) {
        if (clickedPorts.isEmpty()) {
            clickedPorts.add(port);
            port.setHighlight(true);
            return;
        }

        clickedPorts.get(0).setHighlight(false);
        clickedPorts.add(port);

        if (!checkIsLinkValid()) {
            System.out.println("Link not valid " + clickedPorts.get(0).getPortRole().name() + clickedPorts.get(1).getPortRole().name());
            clickedPorts.clear();
            return;
        }

        Port nearEndPort = clickedPorts.get(0).getPortRole() == PortRole.PRIMARY ? clickedPorts.get(0) : clickedPorts.get(1);
        Port farEndPort =  clickedPorts.get(0).getPortRole() == PortRole.SECONDARY ? clickedPorts.get(0) : clickedPorts.get(1);
        if (nearEndPort.equals(farEndPort)) {
            System.out.println("Same");
        }

        createLink(nearEndPort, farEndPort);
        clickedPorts.clear();
    }

    private void createLink(Port nearEndPort, Port farEndPort) {
        ViewType viewType = nearEndPort.getHostUnit().getViewType();
        String linkName = String.format("%s_%s-%s_%s",
                nearEndPort.getHostUnit().getName(),
                nearEndPort.getName(), farEndPort.getHostUnit().getName(),
                farEndPort.getName());

        Link link = this.vm.createLinkInView(linkName, nearEndPort, farEndPort);

        if (viewType == ViewType.CONFIGURED) {
            em.handleLinkConfigured(linkName, nearEndPort.getHostUnit().getName(), nearEndPort.getName(), farEndPort.getHostUnit().getName(), farEndPort.getName());
        } else {
            em.handleLinkConnected(nearEndPort.getHostUnit().getName(), nearEndPort.getName(), farEndPort.getHostUnit().getSerialNumber(), farEndPort.getName());
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteOption = new MenuItem("Delete");
        deleteOption.setOnAction(event -> {
            deleteLink(link);
        });
        contextMenu.getItems().addAll(deleteOption);

        link.setOnContextMenuRequested(event -> {
            contextMenu.show(link, event.getScreenX(), event.getScreenY());
        });

        link.setOnMouseClicked(event -> {
           if (event.getButton() == MouseButton.PRIMARY)  {
               vm.handleDetailsLink(link);
           }
        });
    }

    private void deleteLink(Link link) {
        ViewType viewType = link.getNearEndPort().getHostUnit().getViewType();

        if (viewType == ViewType.CONFIGURED) {
            em.handleLinkUnconfigured(link.getNearEndPort().getHostUnit().getName(), link.getNearEndPort().getName());
        } else {
            em.handleLinkDisconnected(link.getNearEndPort().getHostUnit().getName(), link.getNearEndPort().getName());
        }

        vm.deleteLinkFromView(link, viewType);
    }

    private boolean checkIsLinkValid() {
        if (clickedPorts.get(0).hasLink() || clickedPorts.get(0).hasLink()) {
            return false;
        }
        if (clickedPorts.get(0).getPortRole() == clickedPorts.get(1).getPortRole()) {
            return false;
        }
        if (clickedPorts.get(0).getHostUnit() == clickedPorts.get(1).getHostUnit()) {
            return false;
        }
        if (clickedPorts.get(1).getHostUnit().getViewType() != clickedPorts.get(0).getHostUnit().getViewType()) {
            return false;
        }

        return true;
    }

    public void handleLinkFaultChange(String linkName, String faultInfo) {
        vm.setViewLinkFault(linkName, faultInfo);
    }

    public String createRandomSerialNumber() {
        final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(10);
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(CHAR_POOL.length());
            char randomChar = CHAR_POOL.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    void clearAll() {
        db.clearTopology();
        vm.clearView();

        this.unitCounter = 0;
        this.clickedPorts.clear();
    }
}