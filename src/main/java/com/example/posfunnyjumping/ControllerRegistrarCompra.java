package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.*;
import java.util.stream.Collectors;

public class ControllerRegistrarCompra {

    @FXML
    private TextField buscarProductoTextField;
    @FXML
    private TableView<DatabaseManager.Producto> productosTableView;
    @FXML
    private TableColumn<DatabaseManager.Producto, String> productoDescripcionColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Double> productoPrecioColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Double> productoExistenciaColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Void> productoAgregarColumn;
    @FXML
    private TableView<CompraItem> compraTableView;
    @FXML
    private TableColumn<CompraItem, String> compraDescripcionColumn;
    @FXML
    private TableColumn<CompraItem, Double> compraCantidadColumn;
    @FXML
    private TableColumn<CompraItem, Void> compraRemoverColumn;
    @FXML
    private TextField totalTextField;

    private Stage stage;
    private Scene scene;
    private ObservableList<CompraItem> compraItems = FXCollections.observableArrayList();
    private List<DatabaseManager.Producto> originalProductosList;

    @FXML
    private void initialize() {
        initializeProductosTable();
        initializeCompraTable();

        buscarProductoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProductos(newValue);
        });
    }

    private void initializeProductosTable() {
        productoDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        productoExistenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        productoPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        productoAgregarColumn.setCellFactory(param -> createButtonCell("Agregar", this::agregarProducto));

        try {
            originalProductosList = DatabaseManager.ProductoDAO.getAll();
            if (!originalProductosList.isEmpty()) {
                productosTableView.setItems(FXCollections.observableArrayList(originalProductosList));
            } else {
                showAlert("No products found.");
            }
        } catch (DatabaseManager.DatabaseException e) {
            showAlert("Error loading products: " + e.getMessage());
        }

    }

    private void filterProductos(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            productosTableView.setItems(FXCollections.observableArrayList(originalProductosList));
        } else {
            List<DatabaseManager.Producto> filteredList = originalProductosList.stream()
                    .filter(producto ->
                            producto.getDescripcion().toLowerCase().contains(searchText.toLowerCase())
                    )
                    .collect(Collectors.toList());
            productosTableView.setItems(FXCollections.observableArrayList(filteredList));
        }
    }

    private void initializeCompraTable() {
        compraDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        compraCantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        compraRemoverColumn.setCellFactory(param -> createButtonCell("Remover", this::removerCompraItem));

        compraTableView.setItems(compraItems);
    }

    private <T> TableCell<T, Void> createButtonCell(String buttonText, java.util.function.Consumer<T> action) {
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

    private void agregarProducto(DatabaseManager.Producto producto) {
        CompraItem existingItem = compraItems.stream()
                .filter(item -> item.getDescripcion().equals(producto.getDescripcion()))
                .findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.incrementCantidad();
            compraTableView.refresh();
        } else {
            CompraItem newItem = new CompraItem(producto.getClave(), producto.getDescripcion(), producto.getPrecio(), 1);
            compraItems.add(newItem);
        }
    }

    private void removerCompraItem(CompraItem item) {
        if (item.getCantidad() > 1) {
            item.decrementCantidad();
            compraTableView.refresh();
        } else {
            compraItems.remove(item);
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(content);
        alert.showAndWait();
    }


    @FXML
    private void onProcesarCompraClick() {
        if (compraItems.isEmpty()) {
            showAlert("No hay items en la compra para procesar.");
            return;
        }



        Optional<DatabaseManager.Corte> lastOpenCorte = DatabaseManager.CorteDAO.getLastOpenCorte();
        if (!lastOpenCorte.isPresent()) {
            showAlert("No hay un corte abierto. No se puede procesar la compra.");
            return;
        }

        DatabaseManager.Compra newCompra = new DatabaseManager.Compra(
                0, // The database will assign the actual clave
                LocalDateTime.now(),
                lastOpenCorte.get().getClave(),
                lastOpenCorte.get().getClaveEncargado(),
                lastOpenCorte.get().getNombreEncargado()
        );


        List<DatabaseManager.PartidaCompra> partidas = new ArrayList<>();

        for (CompraItem item : compraItems) {
            DatabaseManager.PartidaCompra partida = new DatabaseManager.PartidaCompra(
                    0, // The database will assign the actual clavePartida
                    0, // The claveCompra will be set after the compra is inserted
                    item.getClave(),
                    item.getDescripcion(), // Add the description
                    item.getCantidad()
            );
            partidas.add(partida);
        }

        try {
            DatabaseManager.CompraDAO.insertCompraWithPartidas(newCompra, partidas);

            compraItems.clear();
            compraTableView.refresh();

            showSuccessAlert("Compra procesada exitosamente.");
        } catch (DatabaseManager.DatabaseException e) {
            showAlert("Error al procesar la compra: " + e.getMessage());
        }
    }


    @FXML
    private void clearProductSearch() {
        buscarProductoTextField.clear();
    }

    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onRegresarClick(ActionEvent event) throws IOException {
        navigateTo("ConsultaCompras.fxml", event);
    }

    public static class CompraItem {
        private final int clave;
        private final String descripcion;
        private double cantidad;

        public CompraItem(int clave, String descripcion, double precio, double cantidad) {
            this.clave = clave;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
        }

        public int getClave() {
            return clave;
        }

        public String getDescripcion() {
            return descripcion;
        }


        public double getCantidad() {
            return cantidad;
        }

        public void incrementCantidad() {
            this.cantidad++;
        }

        public void decrementCantidad() {
            if (this.cantidad > 0) {
                this.cantidad--;
            }
        }
    }
}