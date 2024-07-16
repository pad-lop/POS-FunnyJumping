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

public class ControllerCompras {

    @FXML
    private TableColumn<DatabaseManager.Compra, Integer> compraFolioColumn;
    @FXML
    private TableColumn<DatabaseManager.Compra, LocalDateTime> compraFechaColumn;
    @FXML
    private TableColumn<DatabaseManager.Compra, Integer> compraCorteColumn;
    @FXML
    private TableColumn<DatabaseManager.Compra, String> compraNombreEncargadoColumn;
    @FXML
    private TableColumn<DatabaseManager.Compra, Void> compraDetallesColumn;

    @FXML
    private TableView<DatabaseManager.Compra> comprasTable;

    @FXML
    private TableColumn<DatabaseManager.PartidaCompra, Integer> compraPartidaCantidadColumn;
    @FXML
    private TableColumn<DatabaseManager.PartidaCompra, String> compraPartidaDescripcionColumn;

    @FXML
    private TableView<DatabaseManager.PartidaCompra> compraDetallesTableView;
    @FXML
    private TextField compraDetallesFolioTextField;
    @FXML
    private TextField compraDetallesFechaTextField;
    @FXML
    private TextField compraDetallesTotalTextField;
    @FXML
    private TextField compraDetallesCorteTextField;
    @FXML
    private TextField compraDetallesNombreEncargadoTextField;

    @FXML
    private void initialize() {
        initializeComprasTableColumns();
        loadComprasData();
    }

    private void initializeComprasTableColumns() {
        setComprasCellValueFactories();
        setCompraButtonColumns();
        loadComprasData();
    }

    private void initializeDetallesCompraView() {
        initializeDetallesCompraColumns();
    }

    private void initializeDetallesCompraColumns() {
        if (compraPartidaCantidadColumn != null) {
            compraPartidaCantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        }
        if (compraPartidaDescripcionColumn != null) {
            compraPartidaDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        }
    }

    private void setComprasCellValueFactories() {
        compraFolioColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        compraFechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        compraFechaColumn.setCellFactory(column -> new TableCell<DatabaseManager.Compra, LocalDateTime>() {
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

        compraCorteColumn.setCellValueFactory(new PropertyValueFactory<>("clave_corte"));
        compraNombreEncargadoColumn.setCellValueFactory(new PropertyValueFactory<>("nombreEncargado"));
    }

    private void setCompraButtonColumns() {
        compraDetallesColumn.setCellFactory(param -> createButtonCell("Detalles", this::detallesCompra));
    }

    @FXML
    private void detallesCompra(DatabaseManager.Compra compra) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DetallesCompra.fxml"));
            loader.setController(this);
            Parent root = loader.load();

            initializeDetallesCompraView();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String formattedDateTime = compra.getFecha().format(formatter);

            compraDetallesFolioTextField.setText(String.valueOf(compra.getClave()));
            compraDetallesFechaTextField.setText(formattedDateTime);
            compraDetallesCorteTextField.setText(String.valueOf(compra.getClave_corte()));
            compraDetallesNombreEncargadoTextField.setText(String.valueOf(compra.getNombreEncargado()));

            List<DatabaseManager.PartidaCompra> partidas = DatabaseManager.CompraDAO.getPartidasByCompra(compra.getClave());
            compraDetallesTableView.setItems(FXCollections.observableArrayList(partidas));

            Stage detailsStage = new Stage();
            detailsStage.setTitle("Detalles de Compra");
            detailsStage.setScene(new Scene(root));
            detailsStage.setResizable(true);
            detailsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error al cargar los detalles de la compra");
        }
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

    private void loadComprasData() {
        try {
            List<DatabaseManager.Compra> comprasList = DatabaseManager.CompraDAO.getAllCompras();
            comprasTable.setItems(FXCollections.observableArrayList(comprasList));
            comprasTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            showErrorAlert("Error al cargar los datos de compras: " + e.getMessage());
        }
    }

    @FXML
    private void onRegresarCompraClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onImprimirCompraClick() {
        try {
            // Get the folio from the text field
            int folio = Integer.parseInt(compraDetallesFolioTextField.getText());

            // Retrieve the venta from the database
            Optional<DatabaseManager.Compra> optionalCompra = DatabaseManager.CompraDAO.getById(folio);

            if (optionalCompra.isPresent()) {
                DatabaseManager.Compra compra = optionalCompra.get();

                // Retrieve the partidas for this venta from the database
                List<DatabaseManager.PartidaCompra> partidas = DatabaseManager.CompraDAO.getPartidasByCompra(folio);

                // Print the ticket
                PrinterCompra.printCompra(compra, partidas);
                showInfoAlert("Recibo compra impreso correctamente");
            } else {
                showErrorAlert("No se encontró la venta con el folio: " + folio);
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Folio inválido: " + compraDetallesFolioTextField.getText());
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
    protected void onRegistrarCompraButtonClick(ActionEvent event) throws IOException {
        navigateTo("RegistrarCompra.fxml", event);
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