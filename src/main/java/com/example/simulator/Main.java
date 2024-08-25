package com.example.simulator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("simulator.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Misconfiguration Simulator");
        stage.setScene(scene);

        Controller controller = fxmlLoader.getController();

        stage.setMinWidth(1280);
        stage.setMinHeight(720);

        stage.show();
        Platform.runLater(() -> {
            ChangeListener<Number> resizeListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    controller.setSize();
                }
            };
            controller.setListeners(resizeListener);
            Platform.runLater(controller::setSize);
        });

    }

    public static void main(String[] args) {
        launch();
    }
}