package com.example.posfunnyjumping;

import javafx.application.Platform;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.*;

public class ControllerCortes {

    @FXML
    private TableView<DatabaseManager.Corte> cortesTable;
    @FXML
    private TableColumn<DatabaseManager.Corte, Integer> corteClaveColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, String> corteEstadoColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, LocalDateTime> corteAperturaColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, LocalDateTime> corteCierreColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, Double> corteVentasColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, Void> corteDetallesColumn;

    @FXML
    private TableColumn<DatabaseManager.Corte, Integer> corteReciboInicialColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, Integer> corteReciboFinalColumn;
    @FXML
    private TableColumn<DatabaseManager.Corte, Double> corteFondoAperturaColumn;


    @FXML
    private Button abrirCorteButton;
    @FXML
    private Button cerrarCorteButton;

    @FXML
    private void initialize() {

        initializeCortesTableColumns();
        loadCortesData();
        initializeActionButtons();
    }

    private void initializeCortesTableColumns() {
        setCortesCellValueFactories();
        setCorteButtonColumns();
    }

    private void setCortesCellValueFactories() {
        corteClaveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
        corteEstadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        corteAperturaColumn.setCellValueFactory(new PropertyValueFactory<>("apertura"));
        corteCierreColumn.setCellValueFactory(new PropertyValueFactory<>("cierre"));
        corteVentasColumn.setCellValueFactory(new PropertyValueFactory<>("ventas"));
        corteReciboInicialColumn.setCellValueFactory(new PropertyValueFactory<>("reciboInicial"));
        corteReciboFinalColumn.setCellValueFactory(new PropertyValueFactory<>("reciboFinal"));
        corteFondoAperturaColumn.setCellValueFactory(new PropertyValueFactory<>("fondoApertura"));
    }

    private void setCorteButtonColumns() {
        corteDetallesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button("Detalles");

            {
                button.setOnAction(event -> {
                    DatabaseManager.Corte corte = getTableView().getItems().get(getIndex());
                    detallesCorte(corte);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
    }

    private void initializeActionButtons() {
        DatabaseManager.Corte lastOpenCorte = DatabaseManager.CorteDAO.getLastOpenCorte().orElse(null);

        if (lastOpenCorte != null) {
            cerrarCorteButton.setDisable(false);
            abrirCorteButton.setDisable(true);
        } else {
            cerrarCorteButton.setDisable(true);
            abrirCorteButton.setDisable(false);
        }
    }

    private void detallesCorte(DatabaseManager.Corte corte) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalles del Corte");
        dialog.setHeaderText("Corte #" + corte.getClave());

        ButtonType closeButtonType = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 150, 10, 10));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        addDetailField(grid, 0, "Clave:", String.valueOf(corte.getClave()));
        addDetailField(grid, 1, "Estado:", corte.getEstado());
        addDetailField(grid, 2, "Apertura:", corte.getApertura().toString());
        addDetailField(grid, 3, "Cierre:", corte.getCierre() != null ? corte.getCierre().toString() : "N/A");
        addDetailField(grid, 4, "Recibo Inicial:", String.valueOf(corte.getReciboInicial()));
        addDetailField(grid, 5, "Recibo Final:", String.valueOf(corte.getReciboFinal()));
        addDetailField(grid, 6, "Fondo Apertura:", String.format("%.2f", corte.getFondoApertura()));
        addDetailField(grid, 7, "Ventas:", String.format("%.2f", corte.getVentas()));
        addDetailField(grid, 8, "Total Efectivo:", String.format("%.2f", corte.getTotalEfectivo()));
        addDetailField(grid, 9, "Total Caja:", String.format("%.2f", corte.getTotalCaja()));

        content.getChildren().add(grid);

        // Add sales table
        TableView<DatabaseManager.Venta> salesTable = new TableView<>();
        salesTable.setMaxHeight(200);

        TableColumn<DatabaseManager.Venta, Integer> claveColumn = new TableColumn<>("Clave");
        claveColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));

        TableColumn<DatabaseManager.Venta, LocalDateTime> fechaColumn = new TableColumn<>("Fecha");
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));

        TableColumn<DatabaseManager.Venta, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        salesTable.getColumns().addAll(claveColumn, fechaColumn, totalColumn);

        // Get sales for this corte
        List<DatabaseManager.Venta> ventas = DatabaseManager.VentaDAO.getVentasByCorte(corte.getClave());
        salesTable.setItems(FXCollections.observableArrayList(ventas));

        content.getChildren().addAll(new Label("Ventas del Corte:"), salesTable);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void addDetailField(GridPane grid, int row, String label, String value) {
        grid.add(new Label(label), 0, row);
        TextField field = new TextField(value);
        field.setEditable(false);
        grid.add(field, 1, row);
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

    private void loadCortesData() {
        try {
            List<DatabaseManager.Corte> cortesList = DatabaseManager.CorteDAO.getAllCortes();
            cortesTable.setItems(FXCollections.observableArrayList(cortesList));
            cortesTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            showErrorAlert("Error al cargar los datos de cortes: " + e.getMessage());
        }
    }


    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
    }


    @FXML
    protected void onIniciarCorteButtonClick(ActionEvent event) throws IOException {
        DatabaseManager.Corte lastOpenCorte = DatabaseManager.CorteDAO.getLastOpenCorte().orElse(null);
        if (lastOpenCorte == null) {
            // Create a custom dialog
            Dialog<DatabaseManager.Corte> dialog = new Dialog<>();
            dialog.setTitle("Abrir Corte");
            dialog.setHeaderText("Ingrese los datos para abrir el corte");

            // Set the button types
            ButtonType abrirButtonType = new ButtonType("Abrir", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(abrirButtonType, ButtonType.CANCEL);

            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField fondoAperturaField = new TextField();

            grid.add(new Label("Fondo de Apertura:"), 0, 1);
            grid.add(fondoAperturaField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to a Corte object when the abrir button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == abrirButtonType) {
                    try {
                        double fondoApertura = Double.parseDouble(fondoAperturaField.getText());
                        return new DatabaseManager.Corte(0, "Abierto", LocalDateTime.now(), null, 0, 0, fondoApertura, 0, 0, 0);
                    } catch (NumberFormatException e) {
                        showErrorAlert("Datos inválidos. Por favor, ingrese números válidos.");
                        return null;
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<DatabaseManager.Corte> result = dialog.showAndWait();
            result.ifPresent(newCorte -> {
                DatabaseManager.CorteDAO.insertCorte(newCorte);
                initialize();
            });
        } else {
            showErrorAlert("Ya hay un corte abierto");
        }
    }

    // Add these helper methods to the class:
    private void updateTotalCaja(TextField totalEfectivoField, TextField totalTarjetaField, TextField totalCajaField) {
        try {
            double efectivo = 0.0;

            if (!totalEfectivoField.getText().isEmpty()) {
                efectivo = Double.parseDouble(totalEfectivoField.getText());
            }

            double tarjeta = 0.0;

            if (!totalTarjetaField.getText().isEmpty()) {
                tarjeta = Double.parseDouble(totalTarjetaField.getText());
            }

            double totalCaja = efectivo + tarjeta;

            totalCajaField.setText(String.format("%.2f", totalCaja));
        } catch (NumberFormatException e) {
            System.out.println("Error en el formato de número");
            totalCajaField.setText("0.00");
        }
    }

    private void updateTotalEsperado(TextField totalVentasField, TextField fondoAperturaField, TextField totalEsperadoField) {
        try {

        } catch (NumberFormatException e) {
            totalEsperadoField.setText("0.00");
        }
    }

    @FXML
    protected void onCerrarCorteButtonClick(ActionEvent event) throws IOException {
        System.out.println("onCerrarCorteButtonClick method called");
        DatabaseManager.Corte lastOpenCorte = DatabaseManager.CorteDAO.getLastOpenCorte().orElse(null);
        if (lastOpenCorte != null) {
            List<DatabaseManager.Venta> ventasSinCorte = DatabaseManager.VentaDAO.getVentasSinCorte();
            double totalVentas = DatabaseManager.VentaDAO.getTotalVentasSinCorte();

            Platform.runLater(() -> {
                Dialog<DatabaseManager.Corte> dialog = new Dialog<>();
                dialog.setTitle("Cerrar Corte");
                dialog.setHeaderText("Ingrese los datos para cerrar el corte");

                ButtonType cerrarButtonType = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(cerrarButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                TextField totalVentasField = new TextField(String.format("%.2f", totalVentas));
                totalVentasField.setEditable(false);

                TextField fondoAperturaField = new TextField(String.format("%.2f", lastOpenCorte.getFondoApertura()));
                fondoAperturaField.setEditable(false);

                TextField totalEfectivoField = new TextField();
                totalEfectivoField.setEditable(false);


                TextField totalTarjetaField = new TextField();


                TextField totalCajaField = new TextField();
                totalCajaField.setEditable(false);

                TextField totalEsperadoField = new TextField();
                totalEsperadoField.setEditable(false);


                // Add listeners for updating totalCajaField
                totalEfectivoField.textProperty().addListener((observable, oldValue, newValue) -> updateTotalCaja(totalEfectivoField, totalTarjetaField, totalCajaField));
                totalTarjetaField.textProperty().addListener((observable, oldValue, newValue) -> updateTotalCaja(totalEfectivoField, totalTarjetaField, totalCajaField));


                // Money counting grid
                GridPane moneyGrid = new GridPane();
                moneyGrid.setHgap(10);
                moneyGrid.setVgap(5);
                moneyGrid.setPadding(new Insets(10));

                String[] denominations = {"1000", "500", "200", "100", "50", "20", "10", "5", "2", "1", "0.5"};
                TextField[] quantityFields = new TextField[denominations.length];
                TextField[] totalFields = new TextField[denominations.length];

                moneyGrid.add(new Label("Cantidad"), 0, 0);
                moneyGrid.add(new Label("Denominación"), 1, 0);
                moneyGrid.add(new Label("Total"), 2, 0);

                for (int i = 0; i < denominations.length; i++) {
                    quantityFields[i] = new TextField();
                    quantityFields[i].setPrefWidth(60);
                    Label denominationLabel = new Label(denominations[i]);
                    totalFields[i] = new TextField();
                    totalFields[i].setEditable(false);
                    totalFields[i].setPrefWidth(80);

                    moneyGrid.add(quantityFields[i], 0, i + 1);
                    moneyGrid.add(denominationLabel, 1, i + 1);
                    moneyGrid.add(totalFields[i], 2, i + 1);

                    int index = i;
                    quantityFields[i].textProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            int quantity = Integer.parseInt(newValue);
                            double denomination = Double.parseDouble(denominations[index]);
                            double total = quantity * denomination;
                            totalFields[index].setText(String.format("%.2f", total));
                            updateTotalEfectivo(totalEfectivoField, totalFields);
                        } catch (NumberFormatException e) {
                            totalFields[index].setText("0.00");
                            updateTotalEfectivo(totalEfectivoField, totalFields);
                        }
                    });
                }

                grid.add(moneyGrid, 0, 0, 2, 1);
                grid.add(new Label("Efectivo:"), 0, 1);
                grid.add(totalEfectivoField, 1, 1);

                grid.add(new Label("Tarjeta:"), 0, 2);
                grid.add(totalTarjetaField, 1, 2);

                grid.add(new Label("Total:"), 0, 3);
                grid.add(totalCajaField, 1, 3);

                TableView<DatabaseManager.Venta> ventasTable = new TableView<>();
                ventasTable.setItems(FXCollections.observableArrayList(ventasSinCorte));

                TableColumn<DatabaseManager.Venta, Integer> claveColumn = new TableColumn<>("Clave");
                claveColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));

                TableColumn<DatabaseManager.Venta, LocalDateTime> fechaColumn = new TableColumn<>("Fecha");
                fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));

                TableColumn<DatabaseManager.Venta, Double> totalColumn = new TableColumn<>("Total");
                totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

                ventasTable.getColumns().addAll(claveColumn, fechaColumn, totalColumn);
                ventasTable.setMaxHeight(350);

                grid.add(ventasTable, 3, 0, 2, 1);

                grid.add(new Label("Ventas:"), 3, 1);
                grid.add(totalVentasField, 4, 1);


                grid.add(new Label("Fondo Apertura:"), 3, 2);
                grid.add(fondoAperturaField, 4, 2);

                grid.add(new Label("Total:"), 3, 3);
                grid.add(totalEsperadoField, 4, 3);


                double ventas = 0.0;

                if (!totalVentasField.getText().isEmpty()) {
                    ventas = Double.parseDouble(totalVentasField.getText());
                }

                double fondoApertura = 0.0;

                if (!fondoAperturaField.getText().isEmpty()) {
                    fondoApertura = Double.parseDouble(fondoAperturaField.getText());
                }

                double totalEsperado = ventas + fondoApertura;
                totalEsperadoField.setText(String.format("%.2f", totalEsperado));


                dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == cerrarButtonType) {
                        try {
                            lastOpenCorte.setEstado("Cerrado");
                            lastOpenCorte.setCierre(LocalDateTime.now());

                            lastOpenCorte.setTotalEfectivo(Double.parseDouble(totalEfectivoField.getText()));
                            lastOpenCorte.setTotalTarjeta(Double.parseDouble(totalTarjetaField.getText()));
                            lastOpenCorte.setTotalCaja(Double.parseDouble(totalCajaField.getText()));


                            lastOpenCorte.setVentas(totalVentas);
                            lastOpenCorte.setTotalEsperado(Double.parseDouble(totalEsperadoField.getText()));


                            return lastOpenCorte;
                        } catch (NumberFormatException e) {
                            showErrorAlert("Datos inválidos. Por favor, ingrese números válidos.");
                            return null;
                        }
                    }
                    return null;
                });

                Optional<DatabaseManager.Corte> result = dialog.showAndWait();
                result.ifPresent(updatedCorte -> {

                    DatabaseManager.CorteDAO.updateCorte(updatedCorte);
                    DatabaseManager.VentaDAO.asignarCorteAVentas(updatedCorte.getClave(), ventasSinCorte);
                    initialize();
                });
            });
        } else {
            showErrorAlert("No hay un corte abierto para cerrar");
        }
    }

    private void updateTotalEfectivo(TextField totalEfectivoField, TextField[] totalFields) {
        double sum = 0;
        for (TextField field : totalFields) {
            try {
                sum += Double.parseDouble(field.getText());
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        totalEfectivoField.setText(String.format("%.2f", sum));
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

    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        Stage stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}