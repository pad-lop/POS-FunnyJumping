package com.example.posfunnyjumping;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ControllerConfiguracion {

    private Stage stage;
    private Scene scene;

    @FXML
    private VBox contentVBox;

    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        showLoginScreen();
    }

    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        Stage stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        Scene scene = new Scene(root);
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

    @FXML
    protected void onConsultaInventariosButtonClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaInventarios.fxml", event);
    }

    @FXML
    protected void onConsultaComprasButtonClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaCompras.fxml", event);
    }

    private void showLoginScreen() {
        LoginConfiguracion loginScreen = new LoginConfiguracion();
        loginScreen.setOnLoginSuccess(user -> {
            contentVBox.getChildren().remove(loginScreen);
            tabPane.setVisible(true);
        });

        tabPane.setVisible(false);
        contentVBox.getChildren().add(0, loginScreen);
    }
}