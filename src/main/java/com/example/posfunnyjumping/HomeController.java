package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class HomeController {

    @FXML
    private TextField ProductoDescripcionTextField;
    @FXML
    private TextField ProductoPrecioTextField;
    @FXML
    private Label welcomeText;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<DatabaseConnection.Producto> productosTable;

    @FXML
    private TableColumn<DatabaseConnection.Producto, Integer> claveColumn;

    @FXML
    private TableColumn<DatabaseConnection.Producto, String> descripcionColumn;

    @FXML
    private TableColumn<DatabaseConnection.Producto, Double> precioColumn;

    @FXML
    private TableColumn<DatabaseConnection.Producto, Double> existenciaColumn;

    @FXML
    private void handleTabOpen(Event event) {
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected()) {
            switch (tab.getId()) {

                case "Productos": {
                    claveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
                    descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
                    precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
                    existenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));

                    List<DatabaseConnection.Producto> productosList = DatabaseConnection.getAllProductos();
                    ObservableList<DatabaseConnection.Producto> data = FXCollections.observableArrayList(productosList);
                    productosTable.setItems(data);

                    break;
                }

                case "Tiempos": {
                    System.out.println("Opened tab tiempos");
                    break;
                }

                case "Usuarios": {
                    System.out.println("Opened tab Usuarios");
                    break;
                }

            }
        }
    }

    @FXML
    private void handleRegistrarProducto(Event event) {

   }

    @FXML
    protected void onRegistrarVentaButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("RegistrarVenta.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaVentasButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ConsultaVentas.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaComprasButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ConsultaCompras.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConsultaCortesButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ConsultaCortes.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onConfiguracionButtonClick(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Configuracion.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onRegresarAlHomeButtonClick(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Temporizador.fxml")));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}

