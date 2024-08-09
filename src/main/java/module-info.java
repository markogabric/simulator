module com.example.simulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.simulator to javafx.fxml;
    exports com.example.simulator;
    exports com.example.simulator.Topology;
    opens com.example.simulator.Topology to javafx.fxml;
    exports com.example.simulator.View;
    opens com.example.simulator.View to javafx.fxml;
}