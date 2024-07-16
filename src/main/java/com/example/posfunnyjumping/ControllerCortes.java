package com.example.posfunnyjumping;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private TableColumn<DatabaseManager.Corte, Double> corteDiferenciaColumn;


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

        // Format the apertura column
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        corteAperturaColumn.setCellValueFactory(new PropertyValueFactory<>("apertura"));
        corteAperturaColumn.setCellFactory(column -> new TableCell<DatabaseManager.Corte, LocalDateTime>() {
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

        // Format the cierre column
        corteCierreColumn.setCellValueFactory(new PropertyValueFactory<>("cierre"));
        corteCierreColumn.setCellFactory(column -> new TableCell<DatabaseManager.Corte, LocalDateTime>() {
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

        corteVentasColumn.setCellValueFactory(new PropertyValueFactory<>("ventas"));
        corteReciboInicialColumn.setCellValueFactory(new PropertyValueFactory<>("reciboInicial"));
        corteReciboFinalColumn.setCellValueFactory(new PropertyValueFactory<>("reciboFinal"));
        corteFondoAperturaColumn.setCellValueFactory(new PropertyValueFactory<>("fondoApertura"));
        corteDiferenciaColumn.setCellValueFactory(new PropertyValueFactory<>("diferencia"));
    }

    private void autoResizeColumns(TableView<?> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        for (TableColumn<?, ?> column : table.getColumns()) {
            // Set a minimum width for the column
            column.setMinWidth(20);

            // Resize the column based on its content
            column.setPrefWidth(column.getWidth());

            Text text = new Text(column.getText());
            double max = text.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                // Check if the cell value is not null
                if (column.getCellData(i) != null) {
                    text = new Text(column.getCellData(i).toString());
                    double calcWidth = text.getLayoutBounds().getWidth();
                    if (calcWidth > max) {
                        max = calcWidth;
                    }
                }
            }
            column.setPrefWidth(max + 10.0d); // Add some padding
        }
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
        ButtonType printButtonType = new ButtonType("Imprimir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(printButtonType, closeButtonType);

        // Main layout
        HBox mainLayout = new HBox(50);
        mainLayout.setPadding(new Insets(10));

        // Left side - fields and labels in a GridPane
        VBox leftSide = new VBox(10);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String boldStyle = "-fx-font-weight: bold;";

        addDetailField(grid, 0, "Clave:", String.valueOf(corte.getClave()));
        addDetailField(grid, 1, "Estado:", corte.getEstado());
        grid.add(new Separator(), 0, 2, 2, 1);
        addDetailField(grid, 3, "Apertura:", corte.getApertura().format(formatter));
        addDetailField(grid, 4, "Cierre:", corte.getCierre() != null ? corte.getCierre().format(formatter) : "");
        addDetailField(grid, 5, "Recibo Inicial:", String.valueOf(corte.getReciboInicial()));
        addDetailField(grid, 6, "Recibo Final:", String.valueOf(corte.getReciboFinal()));

        grid.add(new Separator(), 0, 7, 2, 1);

        addDetailField(grid, 8, "+ Ventas Efectivo:", String.format("%.2f", corte.getVentasEfectivo()));
        addDetailField(grid, 9, "+ Ventas Tarjeta:", String.format("%.2f", corte.getVentasTarjeta()));
        addDetailField(grid, 10, "Total:", String.format("%.2f", corte.getVentas()), boldStyle);

        grid.add(new Separator(), 0, 11, 2, 1);

        addDetailField(grid, 12, "+ Total Efectivo:", String.format("%.2f", corte.getTotalEfectivo()));
        addDetailField(grid, 13, "+ Total Terminal:", String.format("%.2f", corte.getTotalTarjeta()));
        addDetailField(grid, 14, "- Fondo Apertura:", String.format("%.2f", corte.getFondoApertura()));
        addDetailField(grid, 15, "Total:", String.format("%.2f", corte.getTotalCaja()), boldStyle);

        grid.add(new Separator(), 0, 16, 2, 1);

        addDetailField(grid, 17, "Diferencia:", String.format("%.2f", corte.getDiferencia()), boldStyle);

        leftSide.getChildren().add(grid);

        // Right side - tables
        VBox rightSide = new VBox(10);
        rightSide.setPrefWidth(300);

        // Create sales tables
        TableView<DatabaseManager.Venta> ventasEfectivoTable = createVentasTableDetallesCorte();
        TableView<DatabaseManager.Venta> ventasTarjetaTable = createVentasTableDetallesCorte();

        // Get sales for this corte
        List<DatabaseManager.Venta> ventas = DatabaseManager.VentaDAO.getVentasByCorte(corte.getClave());

        for (DatabaseManager.Venta venta : ventas) {
            System.out.println(venta.getTotal());
            System.out.println(venta.getMetodoPago());
            System.out.println(venta.getClaveVenta());
            System.out.println(venta.getFechaVenta());
        }

        // Filter ventas
        List<DatabaseManager.Venta> ventasEfectivoList = ventas.stream()
                .filter(v -> "Efectivo".equals(v.getMetodoPago()))
                .collect(Collectors.toList());

        List<DatabaseManager.Venta> ventasTarjetaList = ventas.stream()
                .filter(v -> "Tarjeta".equals(v.getMetodoPago()))
                .collect(Collectors.toList());

        ventasEfectivoTable.setItems(FXCollections.observableArrayList(ventasEfectivoList));
        ventasTarjetaTable.setItems(FXCollections.observableArrayList(ventasTarjetaList));

        // Create labels for totals
        Label efectivoLabel = new Label("Ventas en Efectivo - Total: $" + String.format("%.2f", corte.getVentasEfectivo()));
        Label tarjetaLabel = new Label("Ventas con Tarjeta - Total: $" + String.format("%.2f", corte.getVentasTarjeta()));
        efectivoLabel.setStyle(boldStyle);
        tarjetaLabel.setStyle(boldStyle);

        // Add tables and labels to the right side
        rightSide.getChildren().addAll(
                efectivoLabel, ventasEfectivoTable,
                tarjetaLabel, ventasTarjetaTable
        );

        // Add both sides to the main layout
        mainLayout.getChildren().addAll(leftSide, rightSide);

        // Use a ScrollPane to allow scrolling if the content is too large
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        dialog.getDialogPane().setContent(scrollPane);

        // Get the print button from the dialog pane
        Button printButton = (Button) dialog.getDialogPane().lookupButton(printButtonType);
        printButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume(); // prevent dialog from closing
            CortePrinter.printCorte(corte, ventas);
        });

        dialog.showAndWait();
    }

    private TableView<DatabaseManager.Venta> createVentasTableDetallesCorte() {
        TableView<DatabaseManager.Venta> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DatabaseManager.Venta, Integer> claveColumn = new TableColumn<>("Clave");
        claveColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));

        TableColumn<DatabaseManager.Venta, LocalDateTime> fechaColumn = new TableColumn<>("Fecha");
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        fechaColumn.setCellFactory(column -> new TableCell<DatabaseManager.Venta, LocalDateTime>() {
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

        TableColumn<DatabaseManager.Venta, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(column -> new TableCell<DatabaseManager.Venta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        table.getColumns().addAll(claveColumn, fechaColumn, totalColumn);
        table.setPrefHeight(200);

        return table;
    }

    private void addDetailField(GridPane grid, int row, String label, String value) {
        addDetailField(grid, row, label, value, null);
    }

    private void addDetailField(GridPane grid, int row, String label, String value, String style) {
        Label labelNode = new Label(label);
        TextField field = new TextField(value);
        field.setEditable(false);
        field.setPrefWidth(120);
        if (style != null) {
            labelNode.setStyle(style);
            field.setStyle(style);
        }
        grid.add(labelNode, 0, row);
        grid.add(field, 1, row);
    }

    private void addDetailField(VBox container, String label, String value) {
        HBox hbox = new HBox(10);
        Label labelNode = new Label(label);
        labelNode.setMinWidth(120);
        TextField field = new TextField(value);
        field.setEditable(false);
        hbox.getChildren().addAll(labelNode, field);
        container.getChildren().add(hbox);
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
            autoResizeColumns(cortesTable); // Add this line
        } catch (DatabaseManager.DatabaseException e) {
            showErrorAlert("Error al cargar los datos de cortes: " + e.getMessage());
        }
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
            TextField claveEncargadoField = new TextField();
            Label nombreEncargadoLabel = new Label();

            grid.add(new Label("Fondo de Apertura:"), 0, 0);
            grid.add(fondoAperturaField, 1, 0);
            grid.add(new Label("Clave de Encargado:"), 0, 1);
            grid.add(claveEncargadoField, 1, 1);
            grid.add(new Label("Nombre de Encargado:"), 0, 2);
            grid.add(nombreEncargadoLabel, 1, 2);

            // Add listener to claveEncargadoField
            claveEncargadoField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    try {
                        int claveEncargado = Integer.parseInt(newValue);
                        Optional<DatabaseManager.Usuario> usuario = DatabaseManager.UsuarioDAO.getById(claveEncargado);
                        if (usuario.isPresent()) {
                            nombreEncargadoLabel.setText(usuario.get().getNombre());
                        } else {
                            nombreEncargadoLabel.setText("Usuario no encontrado");
                        }
                    } catch (NumberFormatException e) {
                        nombreEncargadoLabel.setText("Clave inválida");
                    }
                } else {
                    nombreEncargadoLabel.setText("");
                }
            });

            dialog.getDialogPane().setContent(grid);

            // Convert the result to a Corte object when the abrir button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == abrirButtonType) {
                    try {
                        double fondoApertura = Double.parseDouble(fondoAperturaField.getText());
                        int claveEncargado = Integer.parseInt(claveEncargadoField.getText());

                        // Validate user
                        Optional<DatabaseManager.Usuario> usuario = DatabaseManager.UsuarioDAO.getById(claveEncargado);
                        if (usuario.isPresent()) {
                            return new DatabaseManager.Corte(0, "Abierto", LocalDateTime.now(), null, 0, 0, fondoApertura, 0, 0, 0, 0, 0, 0, 0, claveEncargado, usuario.get().getNombre());
                        } else {
                            showErrorAlert("Usuario no encontrado. Por favor, ingrese una clave de encargado válida.");
                            return null;
                        }
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


    @FXML
    protected void onCerrarCorteButtonClick(ActionEvent event) throws IOException {
        DatabaseManager.Corte lastOpenCorte = DatabaseManager.CorteDAO.getLastOpenCorte().orElse(null);
        if (lastOpenCorte != null) {
            List<DatabaseManager.Venta> ventasSinCorte = DatabaseManager.VentaDAO.getVentasSinCorte();

            double ventasEfectivo = DatabaseManager.VentaDAO.getTotalVentasEfectivoSinCorte();
            double ventasTarjeta = DatabaseManager.VentaDAO.getTotalVentasTarjetaSinCorte();

            double totalVentas = ventasEfectivo + ventasTarjeta;

            if (ventasSinCorte.isEmpty() || totalVentas == 0) {
                showWarningAlert("No hay ventas", "No se puede cerrar el corte", "No se han registrado ventas desde la apertura del corte.");
                return;
            }

            Platform.runLater(() -> {
                Dialog<DatabaseManager.Corte> dialog = new Dialog<>();

                dialog.setTitle("Cerrar Corte");
                dialog.setHeaderText("Ingrese los datos para cerrar el corte");

                ButtonType cerrarButtonType = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
                ButtonType contarEfectivoButtonType = new ButtonType("Contar Efectivo", ButtonBar.ButtonData.LEFT);

                dialog.getDialogPane().getButtonTypes().addAll(cerrarButtonType, contarEfectivoButtonType, ButtonType.CANCEL);

                String boldStyle = "-fx-font-weight: bold;";

                // Main layout
                HBox mainLayout = new HBox(50);
                mainLayout.setPadding(new Insets(10));

                // Left side - fields and labels in a GridPane
                VBox leftSide = new VBox(10);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);

                Label totalVentasLabel = new Label("Total:");
                totalVentasLabel.setStyle(boldStyle);
                TextField totalVentasField = new TextField(String.format("%.2f", totalVentas));
                totalVentasField.setEditable(false);
                totalVentasField.setStyle(boldStyle);

                TextField ventasEfectivoField = new TextField(String.format("%.2f", ventasEfectivo));
                ventasEfectivoField.setEditable(false);

                TextField ventasTarjetaField = new TextField(String.format("%.2f", ventasTarjeta));
                ventasTarjetaField.setEditable(false);

                Label totalCajaLabel = new Label("Total:");
                totalCajaLabel.setStyle(boldStyle);
                TextField totalCajaField = new TextField();
                totalCajaField.setEditable(false);
                totalCajaField.setStyle(boldStyle);

                Label diferenciaLabel = new Label("Diferencia:");
                diferenciaLabel.setStyle(boldStyle);
                TextField diferenciaField = new TextField();
                diferenciaField.setEditable(false);
                diferenciaField.setStyle(boldStyle);

                TextField fondoAperturaField = new TextField(String.format("%.2f", lastOpenCorte.getFondoApertura()));
                fondoAperturaField.setEditable(false);

                TextField totalEfectivoField = new TextField();
                totalEfectivoField.setEditable(false);

                TextField totalTarjetaField = new TextField();

                TextField totalEsperadoField = new TextField();
                totalEsperadoField.setEditable(false);

                // Add components to the grid
                int row = 0;
                grid.add(new Label("+ Ventas Efectivo:"), 0, row);
                grid.add(ventasEfectivoField, 1, row++);

                grid.add(new Label("+ Ventas Tarjeta:"), 0, row);
                grid.add(ventasTarjetaField, 1, row++);

                grid.add(totalVentasLabel, 0, row);
                grid.add(totalVentasField, 1, row++);

                grid.add(new Separator(), 0, row++, 2, 1);

                grid.add(new Label("+ Total Efectivo:"), 0, row);
                grid.add(totalEfectivoField, 1, row++);

                grid.add(new Label("+ Total Terminal:"), 0, row);
                grid.add(totalTarjetaField, 1, row++);

                grid.add(new Label("- Fondo Apertura:"), 0, row);
                grid.add(fondoAperturaField, 1, row++);

                grid.add(totalCajaLabel, 0, row);
                grid.add(totalCajaField, 1, row++);

                grid.add(new Separator(), 0, row++, 2, 1);

                grid.add(diferenciaLabel, 0, row);
                grid.add(diferenciaField, 1, row);

                leftSide.getChildren().add(grid);

                // Right side - tables
                VBox rightSide = new VBox(10);
                rightSide.setPrefWidth(250);

                // Create sales tables
                TableView<DatabaseManager.Venta> ventasEfectivoTable = createVentasTableCloseCorte();
                TableView<DatabaseManager.Venta> ventasTarjetaTable = createVentasTableCloseCorte();


                // Filter ventas
                List<DatabaseManager.Venta> ventasEfectivoList = ventasSinCorte.stream()
                        .filter(v -> "Efectivo".equals(v.getMetodoPago()))
                        .collect(Collectors.toList());

                List<DatabaseManager.Venta> ventasTarjetaList = ventasSinCorte.stream()
                        .filter(v -> "Tarjeta".equals(v.getMetodoPago()))
                        .collect(Collectors.toList());

                ventasEfectivoTable.setItems(FXCollections.observableArrayList(ventasEfectivoList));
                ventasTarjetaTable.setItems(FXCollections.observableArrayList(ventasTarjetaList));

                // Create labels for totals
                Label efectivoLabel = new Label("Ventas en Efectivo - Total: $" + String.format("%.2f", ventasEfectivo));
                Label tarjetaLabel = new Label("Ventas con Tarjeta - Total: $" + String.format("%.2f", ventasTarjeta));

                // Add tables and labels to the right side
                rightSide.getChildren().addAll(
                        efectivoLabel, ventasEfectivoTable,
                        tarjetaLabel, ventasTarjetaTable
                );

                // Add both sides to the main layout
                mainLayout.getChildren().addAll(leftSide, rightSide);

                // Use a ScrollPane to allow scrolling if the content is too large
                ScrollPane scrollPane = new ScrollPane(mainLayout);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);

                dialog.getDialogPane().setContent(scrollPane);

                totalEfectivoField.textProperty().addListener((observable, oldValue, newValue) ->
                        updateTotalCaja(totalEfectivoField, totalTarjetaField, totalCajaField, totalEsperadoField, diferenciaField, fondoAperturaField));

                totalTarjetaField.textProperty().addListener((observable, oldValue, newValue) ->
                        updateTotalCaja(totalEfectivoField, totalTarjetaField, totalCajaField, totalEsperadoField, diferenciaField, fondoAperturaField));

                totalCajaField.textProperty().addListener((observable, oldValue, newValue) ->
                        updateDiferencia(totalCajaField, totalEsperadoField, diferenciaField));

                totalEsperadoField.textProperty().addListener((observable, oldValue, newValue) ->
                        updateDiferencia(totalCajaField, totalEsperadoField, diferenciaField));

                double ventas = Double.parseDouble(totalVentasField.getText());
                double fondoApertura = Double.parseDouble(fondoAperturaField.getText());
                double totalEsperado = ventas;
                totalEsperadoField.setText(String.format("%.2f", totalEsperado));

                Button contarEfectivoButton = (Button) dialog.getDialogPane().lookupButton(contarEfectivoButtonType);
                contarEfectivoButton.addEventFilter(ActionEvent.ACTION, e -> {
                    e.consume(); // prevent dialog from closing
                    showContarEfectivoDialog(totalEfectivoField);
                });

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == cerrarButtonType) {
                        try {
                            lastOpenCorte.setEstado("Cerrado");
                            lastOpenCorte.setCierre(LocalDateTime.now());

                            lastOpenCorte.setTotalEfectivo(Double.parseDouble(totalEfectivoField.getText()));
                            lastOpenCorte.setTotalTarjeta(totalTarjetaField.getText().isEmpty() ? 0.0 : Double.parseDouble(totalTarjetaField.getText()));
                            lastOpenCorte.setTotalCaja(Double.parseDouble(totalCajaField.getText()));

                            lastOpenCorte.setVentasEfectivo(Double.parseDouble(ventasEfectivoField.getText()));
                            lastOpenCorte.setVentasTarjeta(Double.parseDouble(ventasTarjetaField.getText()));
                            lastOpenCorte.setVentas(totalVentas);
                            lastOpenCorte.setDiferencia(Double.parseDouble(diferenciaField.getText()));

                            if (!ventasSinCorte.isEmpty()) {
                                lastOpenCorte.setReciboInicial(ventasSinCorte.get(0).getClaveVenta());
                                lastOpenCorte.setReciboFinal(ventasSinCorte.get(ventasSinCorte.size() - 1).getClaveVenta());
                            }

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
                    DatabaseManager.CorteDAO.printCorte(updatedCorte); // Add this line
                    initialize();
                });
            });
        } else {
            showErrorAlert("No hay un corte abierto para cerrar");
        }
    }

    private void showContarEfectivoDialog(TextField totalEfectivoField) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Contar Efectivo");
        dialog.setHeaderText("Ingrese la cantidad de billetes y monedas");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(20, 150, 10, 10));

        String[] denominations = {"1000", "500", "200", "100", "50", "20", "10", "5", "2", "1", "0.5"};
        TextField[] quantityFields = new TextField[denominations.length];
        TextField[] totalFields = new TextField[denominations.length];

        // Center the header labels
        Label cantidadLabel = new Label("Cantidad");
        Label denominacionLabel = new Label("Denominación");
        Label totalLabel = new Label("Total");

        cantidadLabel.setAlignment(Pos.CENTER);
        denominacionLabel.setAlignment(Pos.CENTER);
        totalLabel.setAlignment(Pos.CENTER);

        grid.add(cantidadLabel, 0, 0);
        grid.add(denominacionLabel, 1, 0);
        grid.add(totalLabel, 2, 0);

        GridPane.setHalignment(cantidadLabel, HPos.CENTER);
        GridPane.setHalignment(denominacionLabel, HPos.CENTER);
        GridPane.setHalignment(totalLabel, HPos.CENTER);

        // Add a total field at the bottom
        TextField totalField = new TextField();
        totalField.setEditable(false);
        totalField.setPrefWidth(100);

        grid.add(new Separator(), 0, denominations.length + 1, 3, 1);

        Label totalEfectivoLabel = new Label("Total Efectivo:");
        grid.add(totalEfectivoLabel, 0, denominations.length + 2);
        grid.add(totalField, 1, denominations.length + 2, 2, 1);
        GridPane.setHalignment(totalEfectivoLabel, HPos.RIGHT);

        for (int i = 0; i < denominations.length; i++) {
            quantityFields[i] = new TextField();
            quantityFields[i].setPrefWidth(60);
            Label denominationLabel = new Label(denominations[i]);
            totalFields[i] = new TextField();
            totalFields[i].setEditable(false);
            totalFields[i].setPrefWidth(80);

            grid.add(quantityFields[i], 0, i + 1);
            grid.add(denominationLabel, 1, i + 1);
            grid.add(totalFields[i], 2, i + 1);

            // Center the denomination labels
            GridPane.setHalignment(denominationLabel, HPos.CENTER);

            int index = i;
            quantityFields[i].textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int quantity = Integer.parseInt(newValue);
                    double denomination = Double.parseDouble(denominations[index]);
                    double total = quantity * denomination;
                    totalFields[index].setText(String.format("%.2f", total));
                    updateTotalEfectivo(totalField, totalFields);
                } catch (NumberFormatException e) {
                    totalFields[index].setText("0.00");
                    updateTotalEfectivo(totalField, totalFields);
                }
            });
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                totalEfectivoField.setText(totalField.getText());
            }
            return null;
        });

        dialog.showAndWait();
    }


    private TableView<DatabaseManager.Venta> createVentasTableCloseCorte() {
        TableView<DatabaseManager.Venta> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DatabaseManager.Venta, Integer> claveColumn = new TableColumn<>("Clave");
        claveColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));

        TableColumn<DatabaseManager.Venta, LocalDateTime> fechaColumn = new TableColumn<>("Fecha");
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaVenta"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        fechaColumn.setCellFactory(column -> new TableCell<DatabaseManager.Venta, LocalDateTime>() {
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

        TableColumn<DatabaseManager.Venta, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(column -> new TableCell<DatabaseManager.Venta, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        table.getColumns().addAll(claveColumn, fechaColumn, totalColumn);
        table.setPrefHeight(200);

        return table;
    }

    private void updateDiferencia(TextField totalCajaField, TextField totalEsperadoField, TextField diferenciaField) {
        double totalCaja = totalCajaField.getText().isEmpty() ? 0 : Double.parseDouble(totalCajaField.getText());
        double totalEsperado = totalEsperadoField.getText().isEmpty() ? 0 : Double.parseDouble(totalEsperadoField.getText());
        double diferencia = totalCaja - totalEsperado;
        diferenciaField.setText(String.format("%.2f", diferencia));
    }


    private void updateTotalCaja(TextField totalEfectivoField, TextField totalTarjetaField, TextField
            totalCajaField, TextField totalEsperadoField, TextField diferenciaField, TextField fondoAperturaField) {


        double efectivo = totalEfectivoField.getText().isEmpty() ? 0 : Double.parseDouble(totalEfectivoField.getText());
        double tarjeta = totalTarjetaField.getText().isEmpty() ? 0 : Double.parseDouble(totalTarjetaField.getText());
        double fondoApertura = fondoAperturaField.getText().isEmpty() ? 0 : Double.parseDouble(fondoAperturaField.getText());

        double total = efectivo + tarjeta - fondoApertura;

        totalCajaField.setText(String.format("%.2f", total));
        updateDiferencia(totalCajaField, totalEsperadoField, diferenciaField);
    }

    private void updateTotalEfectivo(TextField totalField, TextField[] totalFields) {
        double sum = 0;
        for (TextField field : totalFields) {
            try {
                sum += Double.parseDouble(field.getText());
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        totalField.setText(String.format("%.2f", sum));
    }

    private void showWarningAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.showAndWait();
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

    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        Stage stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}