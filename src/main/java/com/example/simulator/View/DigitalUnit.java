package com.example.simulator.View;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.ArrayList;

public class DigitalUnit extends Unit {
    private static final double UNIT_WIDTH = 200;
    private static final double UNIT_HEIGHT = 350;

    private static final int PORT_COUNT = 8;

    private static final double PORT_OFFSET_X = UNIT_WIDTH;
    private static final double PORT_OFFSET_Y = UNIT_HEIGHT/(PORT_COUNT+1);

    List<Character> portNames = new ArrayList<>();


    public DigitalUnit(String name, ViewType type, double x, double y) {
        super(name, type, x, y);
        this.setPrefSize(UNIT_WIDTH, UNIT_HEIGHT);
        this.setBorderShadow();

        this.setStyle("-fx-background-color: #A8D5BA;");

        this.setLabel(name);
    }

    protected void setLabel(String name) {
        Label label = new Label(name);

        label.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, 20));

        label.setLayoutX(UNIT_WIDTH/2 - 20);
        label.setLayoutY(5);

        this.getChildren().add(label);
    }

    @Override
    protected void setPorts() {
        double yStep = (UNIT_HEIGHT - PORT_OFFSET_Y*2) / (PORT_COUNT - 1);
        char p = 'A';

        for (int i = 0; i < PORT_COUNT; i++, p++) {
            double yPosition = PORT_OFFSET_Y + i * yStep;
            createPrimaryPort(Character.toString(p), PORT_OFFSET_X, yPosition);
        }
    }
}
