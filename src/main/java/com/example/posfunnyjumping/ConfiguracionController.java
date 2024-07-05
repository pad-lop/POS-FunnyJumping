package com.example.posfunnyjumping;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ConfiguracionController {

    private Stage stage;
    private Scene scene;


    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaVentasButtonClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaVentas.fxml", event);
    }




    @FXML
    protected void onConsultaCortesButtonClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaCortes.fxml", event);

    }

    @FXML
    protected void onConfiguracionButtonClick(ActionEvent event) throws IOException {
        navigateTo("Configuracion.fxml", event);

    }

    @FXML
    protected void onTemporizadorButtonClick(ActionEvent event) throws IOException {
        navigateTo("Temporizador.fxml", event);

    }


}

