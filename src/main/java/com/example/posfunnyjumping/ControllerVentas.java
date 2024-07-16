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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ControllerVentas {

    @FXML
    private TableColumn<DatabaseManager.Venta, Integer> ventaFolioColumn;
    @FXML
    private TableColumn<DatabaseManager.Venta, LocalDateTime> ventaFechaColumn;
    @FXML
    private TableColumn<DatabaseManager.Venta, Double> ventaTotalColumn;
    @FXML
    private TableColumn<DatabaseManager.Venta, String> ventaMetodoPagoColumn;
    @FXML
    private TableColumn<DatabaseManager.Venta, Void> ventaDetallesColumn;
    @FXML
    private TableColumn<DatabaseManager.Venta, Integer> ventaNombreEncargadoColumn;

    @FXML
    private TableView<DatabaseManager.Venta> ventasTable;

    @FXML
    private TableColumn<DatabaseManager.PartidaVenta, Integer> ventaPartidaCantidadColumn;
    @FXML
    private TableColumn<DatabaseManager.PartidaVenta, String> ventaPartidaDescripcionColumn;
    @FXML
    private TableColumn<DatabaseManager.PartidaVenta, Double> ventaPartidaPrecioColumn;
    @FXML
    private TableColumn<DatabaseManager.PartidaVenta, Double> ventaPartidaSubtotalColumn;

    @FXML
    private TableColumn<DatabaseManager.PartidaVenta, Integer> ventaCorteColumn;
    @FXML
    private TableView<DatabaseManager.PartidaVenta> ventaDetallesTableView;
    @FXML
    private TextField ventaDetallesFolioTextField;
    @FXML
    private TextField ventaDetallesFechaTextField;
    @FXML
    private TextField ventaDetallesTotalTextField;

    @FXML
    private TextField ventaDetallesCorteTextField;


    @FXML
    private TextField ventaDetallesMetodoPagoTextField;

    @FXML
    private TextField ventaDetallesNombreEncargadoTextField;


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
            ventaPartidaPrecioColumn.setCellFactory(tc -> new TableCell<DatabaseManager.PartidaVenta, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", price));
                    }
                }
            });
        }
        if (ventaPartidaSubtotalColumn != null) {
            ventaPartidaSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
            ventaPartidaSubtotalColumn.setCellFactory(tc -> new TableCell<DatabaseManager.PartidaVenta, Double>() {
                @Override
                protected void updateItem(Double subtotal, boolean empty) {
                    super.updateItem(subtotal, empty);
                    if (empty || subtotal == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", subtotal));
                    }
                }
            });
        }
    }


    private void setVentasCellValueFactories() {
        ventaFolioColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ventaFechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        ventaFechaColumn.setCellFactory(column -> new TableCell<DatabaseManager.Venta, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        ventaTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        ventaMetodoPagoColumn.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        ventaCorteColumn.setCellValueFactory(new PropertyValueFactory<>("clave_corte"));
        ventaNombreEncargadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEncargado"));
    }

    private void setVentaButtonColumns() {
        ventaDetallesColumn.setCellFactory(param -> createButtonCell("Detalles", this::detallesVenta));
    }

    @FXML
    private void detallesVenta(DatabaseManager.Venta venta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DetallesVenta.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            initializeDetallesVentaView();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String formattedDateTime = venta.getFechaVenta().format(formatter);

            ventaDetallesFolioTextField.setText(String.valueOf(venta.getClaveVenta()));
            ventaDetallesFechaTextField.setText(formattedDateTime);
            ventaDetallesTotalTextField.setText(String.format("%.2f", venta.getTotal()));
            ventaDetallesCorteTextField.setText(String.valueOf(venta.getClave_corte()));
            ventaDetallesMetodoPagoTextField.setText(String.valueOf(venta.getMetodoPago()));
            ventaDetallesNombreEncargadoTextField.setText(String.valueOf(venta.getNombreEncargado()));

            List<DatabaseManager.PartidaVenta> partidas = DatabaseManager.VentaDAO.getPartidasByVenta(venta.getClaveVenta());
            ventaDetallesTableView.setItems(FXCollections.observableArrayList(partidas));

            Stage detailsStage = new Stage();
            detailsStage.setTitle("Detalles de Venta");
            detailsStage.setScene(new Scene(root));
            detailsStage.setResizable(true);
            detailsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error al cargar los detalles de la venta");
        }
    }

    private void populateDetallesVenta(DatabaseManager.Venta venta) {
        ventaDetallesFolioTextField.setText(String.valueOf(venta.getClaveVenta()));
        ventaDetallesFechaTextField.setText(venta.getFechaVenta().toString());
        ventaDetallesTotalTextField.setText(String.valueOf(venta.getTotal()));
        ventaDetallesCorteTextField.setText(String.valueOf(venta.getClave_corte()));
        ventaDetallesMetodoPagoTextField.setText(String.valueOf(venta.getMetodoPago()));
        ventaDetallesNombreEncargadoTextField.setText(String.valueOf(venta.getNombreEncargado()));

        List<DatabaseManager.PartidaVenta> partidas = DatabaseManager.VentaDAO.getPartidasByVenta(venta.getClaveVenta());
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
        try {
            List<DatabaseManager.Venta> ventasList = DatabaseManager.VentaDAO.getAllVentas();
            ventasTable.setItems(FXCollections.observableArrayList(ventasList));
            ventasTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            showErrorAlert("Error al cargar los datos de ventas: " + e.getMessage());
        }
    }

    @FXML
    private void onRegresarVentaClick(ActionEvent event) {
        // Close the details stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onImprimirTicketClick() {
        try {
            // Get the folio from the text field
            int folio = Integer.parseInt(ventaDetallesFolioTextField.getText());

            // Retrieve the venta from the database
            Optional<DatabaseManager.Venta> optionalVenta = DatabaseManager.VentaDAO.getById(folio);

            if (optionalVenta.isPresent()) {
                DatabaseManager.Venta venta = optionalVenta.get();

                // Retrieve the partidas for this venta from the database
                List<DatabaseManager.PartidaVenta> partidas = DatabaseManager.VentaDAO.getPartidasByVenta(folio);

                // Print the ticket
                TicketPrinter.printTicket(venta, partidas);
                showInfoAlert("Ticket impreso correctamente");
            } else {
                showErrorAlert("No se encontró la venta con el folio: " + folio);
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Folio inválido: " + ventaDetallesFolioTextField.getText());
        } catch (Exception e) {
            showErrorAlert("Error al imprimir el ticket: " + e.getMessage());
        }
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
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
    protected void onConsultaInventariosButtonClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaInventarios.fxml", event);
    }

    @FXML
    protected void onConsultaComprasButtonClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaCompras.fxml", event);
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