package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class VentasController {


    @FXML
    private TableColumn<DatabaseConnection.Venta, Integer> ventaFolioColumn;
    @FXML
    private TableColumn<DatabaseConnection.Venta, Date> ventaFechaColumn;
    @FXML
    private TableColumn<DatabaseConnection.Venta, Double> ventaTotalColumn;
    /*
    @FXML
    private TableColumn<DatabaseConnection.Venta, Integer> ventaCorteColumn;
    */
    @FXML
    private TableColumn<DatabaseConnection.Venta, Void> ventaDetallesColumn;

    @FXML
    private TableView<DatabaseConnection.Venta> ventasTable;


    @FXML
    private TableColumn<DatabaseConnection.PartidaVenta, Integer> ventaPartidaCantidadColumn;
    @FXML
    private TableColumn<DatabaseConnection.PartidaVenta, String> ventaPartidaDescripcionColumn;
    @FXML
    private TableColumn<DatabaseConnection.PartidaVenta, Double> ventaPartidaPrecioColumn;
    @FXML
    private TableColumn<DatabaseConnection.PartidaVenta, Double> ventaPartidaSubtotalColumn;
    @FXML
    private TableView<DatabaseConnection.PartidaVenta> ventaDetallesTableView;
    @FXML
    private TextField ventaDetallesFolioTextField;
    @FXML
    private TextField ventaDetallesFechaTextField;
    /*
    @FXML
    private TextField ventaDetallesCorteTextField;
    */
    @FXML
    private TextField ventaDetallesTotalTextField;


    @FXML

    private void initialize() {
        initializeVentasTableColumns();
        loadVentasData();
    }

    private void initializeVentasTableColumns() {
        setVentasCellValueFactories();
        setVentaButtonColumns();
        loadVentasData();
    }

    private void initializeDetallesVentaView() {
        initializeDetallesVentaColumns();
    }

    private void initializeDetallesVentaColumns() {
        if (ventaPartidaCantidadColumn != null) {
            ventaPartidaCantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        }
        if (ventaPartidaDescripcionColumn != null) {
            ventaPartidaDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        }
        if (ventaPartidaPrecioColumn != null) {
            ventaPartidaPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        }
        if (ventaPartidaSubtotalColumn != null) {
            ventaPartidaSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        }
    }


    private void setVentasCellValueFactories() {
        ventaFolioColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));
        ventaFechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        ventaTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

    }


    private void setVentaButtonColumns() {
        ventaDetallesColumn.setCellFactory(param -> createButtonCell("Detalles", this::detallesVenta));
    }

    @FXML
    private void detallesVenta(DatabaseConnection.Venta venta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DetallesVenta.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            initializeDetallesVentaView();
            populateDetallesVenta(venta);

            Stage stage = (Stage) ventasTable.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateDetallesVenta(DatabaseConnection.Venta venta) {
        ventaDetallesFolioTextField.setText(String.valueOf(venta.getClaveVenta()));
        ventaDetallesFechaTextField.setText(venta.getFechaVenta().toString());
        ventaDetallesTotalTextField.setText(String.valueOf(venta.getTotal()));

        // Load the partidas for this venta
        List<DatabaseConnection.PartidaVenta> partidas = DatabaseConnection.getPartidasVentaByClaveVenta(venta.getClaveVenta());
        ventaDetallesTableView.setItems(FXCollections.observableArrayList(partidas));
    }

    private <T> TableCell<T, Void> createButtonCell(String buttonText, Consumer<T> action) {
        return new TableCell<>() {
            private final Button button = new Button(buttonText);

            {
                button.setOnAction(event -> {
                    T item = getTableView().getItems().get(getIndex());
                    action.accept(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        };
    }


    private void loadVentasData() {
        List<DatabaseConnection.Venta> ventasList = DatabaseConnection.getAllVentas();
        ventasTable.setItems(FXCollections.observableArrayList(ventasList));
        ventasTable.refresh();
    }

  @FXML
private void onRegresarVentaClick() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConsultaVentas.fxml"));
        Parent root = loader.load();

        // Get the current stage
        Stage stage = (Stage) ventaDetallesTableView.getScene().getWindow();

        // Create and set the new scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        // Consider showing an error dialog to the user
        showErrorAlert("Error al cargar la vista de Consulta Ventas");
    }
}

private void showErrorAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}


    @FXML
    protected void onRegistrarVentaButtonClick(ActionEvent event) throws IOException {
        navigateTo("RegistrarVenta.fxml", event);
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

}

