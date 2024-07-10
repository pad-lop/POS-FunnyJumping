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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ControllerRegistrarVenta {

    @FXML
    private TextField agregarNombreTextField;
    @FXML
    private ComboBox<DatabaseManager.Tiempo> minutosComboBox;
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
    private TableView<OrdenItem> ordenTableView;
    @FXML
    private TableColumn<OrdenItem, String> ordenDescripcionColumn;
    @FXML
    private TableColumn<OrdenItem, Double> ordenPrecioColumn;
    @FXML
    private TableColumn<OrdenItem, Integer> ordenCantidadColumn;
    @FXML
    private TableColumn<OrdenItem, Double> ordenSubtotalColumn;
    @FXML
    private TableColumn<OrdenItem, Void> ordenRemoverColumn;
    @FXML
    private TextField totalTextField;

    private Stage stage;
    private Scene scene;
    private ObservableList<OrdenItem> ordenItems = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        initializeTiemposComboBox();
        initializeProductosTable();
        initializeOrdenTable();
        updateTotal();
    }

    private void initializeTiemposComboBox() {
        List<DatabaseManager.Tiempo> tiemposList = DatabaseManager.TiempoDAO.getAll();
        ObservableList<DatabaseManager.Tiempo> observableTiempos = FXCollections.observableArrayList(tiemposList);
        minutosComboBox.setItems(observableTiempos);

        minutosComboBox.setCellFactory(new Callback<ListView<DatabaseManager.Tiempo>, ListCell<DatabaseManager.Tiempo>>() {
            @Override
            public ListCell<DatabaseManager.Tiempo> call(ListView<DatabaseManager.Tiempo> param) {
                return new ListCell<DatabaseManager.Tiempo>() {
                    @Override
                    protected void updateItem(DatabaseManager.Tiempo item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getMinutos() + " minutos - $" + item.getPrecio());
                        }
                    }
                };
            }
        });

        minutosComboBox.setConverter(new StringConverter<DatabaseManager.Tiempo>() {
            @Override
            public String toString(DatabaseManager.Tiempo tiempo) {
                if (tiempo == null) {
                    return null;
                }
                return tiempo.getMinutos() + " minutos - $" + tiempo.getPrecio();
            }

            @Override
            public DatabaseManager.Tiempo fromString(String string) {
                return null;
            }
        });
    }

    private void initializeProductosTable() {
        productoDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        productoExistenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        productoPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        productoAgregarColumn.setCellFactory(param -> createButtonCell("Agregar", this::agregarProducto));

        List<DatabaseManager.Producto> productosList = DatabaseManager.ProductoDAO.getAll();
        productosTableView.setItems(FXCollections.observableArrayList(productosList));
    }

    private void initializeOrdenTable() {
        ordenDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        ordenPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        ordenCantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        ordenSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        ordenRemoverColumn.setCellFactory(param -> createButtonCell("Remover", this::removerOrdenItem));

        ordenTableView.setItems(ordenItems);
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

    @FXML
    private void agregarTrampolin() {
        DatabaseManager.Tiempo selectedTiempo = minutosComboBox.getValue();
        String nombre = agregarNombreTextField.getText().trim();

        if (selectedTiempo != null && !nombre.isEmpty()) {
            String descripcion = nombre + " " + String.valueOf(selectedTiempo.getMinutos()) + " Mins.";
            OrdenItem trampolinItem = new OrdenItem(selectedTiempo.getClave(), descripcion, selectedTiempo.getPrecio(), 1, true, nombre, selectedTiempo.getMinutos());
            ordenItems.add(trampolinItem);
            agregarNombreTextField.clear();
            minutosComboBox.getSelectionModel().clearSelection();
            updateTotal();
        } else {
            showAlert("Por favor, seleccione el tiempo y ingrese un nombre.");
        }
    }

    private void agregarProducto(DatabaseManager.Producto producto) {
        OrdenItem existingItem = ordenItems.stream().filter(item -> item.getDescripcion().equals(producto.getDescripcion())).findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.incrementCantidad();
            ordenTableView.refresh();  // Refresh the TableView to show updated quantities
        } else {
            OrdenItem newItem = new OrdenItem(producto.getClave(), producto.getDescripcion(), producto.getPrecio(), 1, false, "", 0);
            ordenItems.add(newItem);
        }
        updateTotal();
    }

    private void removerOrdenItem(OrdenItem item) {
        if (item.getCantidad() > 1) {
            item.decrementCantidad();
            ordenTableView.refresh(); // Refresh the TableView to show updated quantities
        } else {
            ordenItems.remove(item);
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = ordenItems.stream().mapToDouble(OrdenItem::getSubtotal).sum();
        totalTextField.setText(String.format("%.2f", total));
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

 // Inner class for order items
    public static class OrdenItem {
        private int clave;
        private final String descripcion;
        private final double precio;
        private int cantidad;


        private boolean isTrampolinTiempo;
        private String nombreTrampolin;
        private int minutosTrampolin;

        public OrdenItem(Integer clave, String descripcion, double precio, int cantidad, boolean isTrampolinTiempo, String nombreTrampolin, int minutosTrampolin) {
            this.clave = clave;
            this.descripcion = descripcion;
            this.precio = precio;
            this.cantidad = cantidad;


            this.isTrampolinTiempo = isTrampolinTiempo;
            this.nombreTrampolin = nombreTrampolin;
            this.minutosTrampolin = minutosTrampolin;

        }

        public Integer getClave() {
            return clave;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public double getPrecio() {
            return precio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getSubtotal() {
            return precio * cantidad;
        }


        public boolean getIsTrampolinTiempo() {
            return this.isTrampolinTiempo;
        }


        public String getNombreTrampolin() {
            return this.nombreTrampolin;
        }


        public int getMinutosTrampolin() {
            return this.minutosTrampolin;
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

  @FXML
private void onProcesarVentaClick() {
    if (ordenItems.isEmpty()) {
        showAlert("No hay items en la orden para procesar.");
        return;
    }

    // Calculate total
    double total = ordenItems.stream().mapToDouble(OrdenItem::getSubtotal).sum();

    // Create venta object
    DatabaseManager.Venta newVenta = new DatabaseManager.Venta(0, LocalDateTime.now(), total, 0);

    // Create partidas list
    List<DatabaseManager.PartidaVenta> partidas = new ArrayList<>();

    for (OrdenItem item : ordenItems) {
        DatabaseManager.PartidaVenta partida;
        if (item.getIsTrampolinTiempo()) {
            partida = new DatabaseManager.PartidaVenta(
                0, 0, 0, item.getCantidad(), item.getPrecio(),
                item.getSubtotal(), item.getDescripcion(),  item.getClave(),true,
                item.getNombreTrampolin(), item.getMinutosTrampolin()
            );
        } else {
            partida = new DatabaseManager.PartidaVenta(
                0, 0, item.getClave(), item.getCantidad(), item.getPrecio(),
                item.getSubtotal(), item.getDescripcion(), 0,false, "", 0
            );
        }
        partidas.add(partida);
    }

    try {
        // Insert venta with partidas
        DatabaseManager.VentaDAO.insertVentaWithPartidas(newVenta, partidas);

        // Clear the order after processing
        ordenItems.clear();
        ordenTableView.refresh();
        updateTotal();

        showSuccessAlert("Venta procesada exitosamente.");
    } catch (DatabaseManager.DatabaseException e) {
        showAlert("Error al procesar la venta: " + e.getMessage());
    }
}

    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onRegresarVentasClick(ActionEvent event) throws IOException {
        navigateTo("Temporizador.fxml", event);
    }
}