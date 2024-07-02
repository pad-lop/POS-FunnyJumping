module com.example.posfunnyjumping {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;


    opens com.example.posfunnyjumping to javafx.fxml;
    exports com.example.posfunnyjumping;
}