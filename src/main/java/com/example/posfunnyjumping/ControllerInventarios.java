package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class ControllerInventarios {

    @FXML
    private TableView<ProductoInventario> inventariosTable;

    @FXML
    private TableColumn<ProductoInventario, Integer> productoClaveColumn;

    @FXML
    private TableColumn<ProductoInventario, String> productoDescripcionColumn;

    @FXML
    private TableColumn<ProductoInventario, Double> productoPrecioColumn;

    @FXML
    private TableColumn<ProductoInventario, Double> productoExistenciasColumn;

    @FXML
    private TableColumn<ProductoInventario, Double> productoEntradasColumn;

    @FXML
    private TableColumn<ProductoInventario, Double> productoSalidasColumn;
    @FXML
    private TableColumn<ProductoInventario, Void> productoDetallesColumn;
    private ObservableList<ProductoInventario> productosInventario = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadProductos();
        autoResizeColumns();
    }

    private void setupTableColumns() {
        productoClaveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
        productoDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        productoPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        productoExistenciasColumn.setCellValueFactory(new PropertyValueFactory<>("existencias"));
        productoEntradasColumn.setCellValueFactory(new PropertyValueFactory<>("entradas"));
        productoSalidasColumn.setCellValueFactory(new PropertyValueFactory<>("salidas"));
        productoDetallesColumn.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button("Detalles");

            {
                button.setOnAction(event -> {
                    ProductoInventario producto = getTableView().getItems().get(getIndex());
                    mostrarDetallesProducto(producto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
        inventariosTable.setItems(productosInventario);


    }

    private void autoResizeColumns() {
        inventariosTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        for (TableColumn<ProductoInventario, ?> column : inventariosTable.getColumns()) {
            // Set a minimum width for the column
            column.setMinWidth(100);

            // Resize the column based on its content
            column.setPrefWidth(column.getWidth());

            Text text = new Text(column.getText());
            double max = text.getLayoutBounds().getWidth();
            for (int i = 0; i < inventariosTable.getItems().size(); i++) {
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

    private void mostrarDetallesProducto(ProductoInventario producto) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalles del Producto");
        dialog.setHeaderText(null);

        ButtonType closeButtonType = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Información General
        Tab infoTab = new Tab("Información General");
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(20));

        addDetailField(infoGrid, 0, "Clave:", String.valueOf(producto.getClave()));
        addDetailField(infoGrid, 1, "Descripción:", producto.getDescripcion());
        addDetailField(infoGrid, 2, "Precio:", String.format("$%.2f", producto.getPrecio()));
        addDetailField(infoGrid, 3, "Existencias:", String.format("%.2f", producto.getExistencias()));
        addDetailField(infoGrid, 4, "Entradas totales:", String.format("%.2f", producto.getEntradas()));
        addDetailField(infoGrid, 5, "Salidas totales:", String.format("%.2f", producto.getSalidas()));

        infoTab.setContent(infoGrid);

        // Tab 2: Historial de Movimientos
        Tab historyTab = new Tab("Historial de Movimientos");
        VBox historyBox = new VBox(10);
        historyBox.setPadding(new Insets(20));

        TableView<Partida> partidasTable = new TableView<>();
        partidasTable.setPrefHeight(300);

        TableColumn<Partida, LocalDateTime> fechaColumn = new TableColumn<>("Fecha");
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        fechaColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

        TableColumn<Partida, String> tipoColumn = new TableColumn<>("Tipo");
        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        TableColumn<Partida, Double> cantidadColumn = new TableColumn<>("Cantidad");
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        cantidadColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    setTextFill(item < 0 ? Color.RED : Color.GREEN);
                }
            }
        });


        // New column for cumulative stock
        TableColumn<Partida, Double> stockColumn = new TableColumn<>("Existencia");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockAcumulado"));
        stockColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });


        partidasTable.getColumns().addAll(fechaColumn, tipoColumn, cantidadColumn, stockColumn);

        ObservableList<Partida> partidas = FXCollections.observableArrayList();
        loadPartidas(producto.getClave(), partidas);

        // Calculate cumulative stock
        double runningStock = 0;

        for (Partida partida : partidas) {
            runningStock += partida.getCantidad(); // Add because we're going forwards
            partida.setStockAcumulado(runningStock);
        }

        partidasTable.setItems(partidas);

        Label summaryLabel = new Label(String.format("Total Entradas: %.2f | Total Salidas: %.2f",
                producto.getEntradas(), producto.getSalidas()));
        summaryLabel.setStyle("-fx-font-weight: bold;");


        historyBox.getChildren().addAll(partidasTable, summaryLabel);
        historyTab.setContent(historyBox);

        tabPane.getTabs().addAll(infoTab, historyTab);

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(500);

        // Style the close button
        Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeButtonType);

        dialog.showAndWait();
    }

    private void addDetailField(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");
        grid.add(labelNode, 0, row);

        TextField field = new TextField(value);
        field.setEditable(false);
        grid.add(field, 1, row);
    }


    private void loadPartidas(int productoClave, ObservableList<Partida> partidas) {
        // Load ventas
        List<DatabaseManager.PartidaVenta> ventasPartidas = DatabaseManager.VentaDAO.getPartidasByProducto(productoClave);
        for (DatabaseManager.PartidaVenta partida : ventasPartidas) {
            partidas.add(new Partida(
                    DatabaseManager.VentaDAO.getById(partida.getClaveVenta()).get().getFechaVenta(),
                    "Venta",
                    -partida.getCantidad()
            ));
        }

        // Load compras
        List<DatabaseManager.PartidaCompra> comprasPartidas = DatabaseManager.CompraDAO.getPartidasByProducto(productoClave);
        for (DatabaseManager.PartidaCompra partida : comprasPartidas) {
            partidas.add(new Partida(
                    DatabaseManager.CompraDAO.getById(partida.getClaveCompra()).get().getFecha(),
                    "Compra",
                    partida.getCantidad()
            ));
        }

        // Sort partidas by date
        partidas.sort(Comparator.comparing(Partida::getFecha));
    }


    private void loadProductos() {
        List<DatabaseManager.Producto> productos = DatabaseManager.ProductoDAO.getAll();
        for (DatabaseManager.Producto producto : productos) {
            double entradas = calcularEntradas(producto.getClave());
            double salidas = calcularSalidas(producto.getClave());
            productosInventario.add(new ProductoInventario(
                    producto.getClave(),
                    producto.getDescripcion(),
                    producto.getPrecio(),
                    producto.getExistencia(),
                    entradas,
                    salidas
            ));
        }
    }

    private double calcularEntradas(int claveProducto) {
        String query = "SELECT SUM(cantidad) FROM partidas_compras WHERE clave_producto = ?";
        List<Double> result = DatabaseManager.queryForList(query,
                rs -> rs.getDouble(1),
                claveProducto
        );
        return result.isEmpty() ? 0.0 : result.get(0);
    }

    private double calcularSalidas(int claveProducto) {
        String query = "SELECT SUM(cantidad) FROM partidas_ventas WHERE clave_producto = ?";
        List<Double> result = DatabaseManager.queryForList(query,
                rs -> rs.getDouble(1),
                claveProducto
        );
        return result.isEmpty() ? 0.0 : result.get(0);
    }

    public static class ProductoInventario {
        private final int clave;
        private final String descripcion;
        private final double precio;
        private final double existencias;
        private final double entradas;
        private final double salidas;

        public ProductoInventario(int clave, String descripcion, double precio, double existencias, double entradas, double salidas) {
            this.clave = clave;
            this.descripcion = descripcion;
            this.precio = precio;
            this.existencias = existencias;
            this.entradas = entradas;
            this.salidas = salidas;
        }

        // Getters
        public int getClave() {
            return clave;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public double getPrecio() {
            return precio;
        }

        public double getExistencias() {
            return existencias;
        }

        public double getEntradas() {
            return entradas;
        }

        public double getSalidas() {
            return salidas;
        }
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
