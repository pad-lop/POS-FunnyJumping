package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.function.Consumer;

public class ProductosController {

    @FXML
    private TableColumn<DatabaseConnection.Producto, Integer> productoClaveColumn;
    @FXML
    private TableColumn<DatabaseConnection.Producto, String> productoDescripcionColumn;
    @FXML
    private TableColumn<DatabaseConnection.Producto, Double> productoPrecioColumn;
    @FXML
    private TableColumn<DatabaseConnection.Producto, Double> productoExistenciaColumn;
    @FXML
    private TableColumn<DatabaseConnection.Producto, Void> productoEditarColumn; // Define as Void for buttons
    @FXML
    private TableColumn<DatabaseConnection.Producto, Void> productoEliminarColumn; // Define as Void for buttons

    @FXML
    private TextField productoClaveTextField;
    @FXML
    private TextField productoDescripcionTextField;
    @FXML
    private TextField productoPrecioTextField;
    @FXML
    private TextField productoExistenciaTextField;
    @FXML
    private TableView<DatabaseConnection.Producto> productosTable;


    @FXML
    private void initialize() {
        initializeProductosTableColumns();
    }


    private boolean confirmDeletion(String itemType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este " + itemType + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(content);
        alert.showAndWait();
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
    private void handleAddOrUpdateProduct() {
        if (!validateProductInputs()) {
            showAlert("Please fill in all the fields.");
            return;
        }

        try {
            String descripcion = productoDescripcionTextField.getText();
            double precio = Double.parseDouble(productoPrecioTextField.getText());
            double existencia = Double.parseDouble(productoExistenciaTextField.getText());

            if (productoClaveTextField.getText().isEmpty()) {
                DatabaseConnection.insertProducto(descripcion, precio, existencia);
            } else {
                int clave = Integer.parseUnsignedInt(productoClaveTextField.getText());
                DatabaseConnection.updateProducto(clave, descripcion, precio, existencia);
            }

            initializeProductosTableColumns();

        } catch (NumberFormatException e) {
            showAlert("Invalid number format in input fields.");
        }
    }

    private boolean validateProductInputs() {
        return !productoDescripcionTextField.getText().isEmpty()
                && !productoPrecioTextField.getText().isEmpty()
                && !productoExistenciaTextField.getText().isEmpty();
    }


    private void clearProductInputFields() {
        productoClaveTextField.clear();
        productoDescripcionTextField.clear();
        productoPrecioTextField.clear();
        productoExistenciaTextField.clear();
    }


    private void populateProductoFields(DatabaseConnection.Producto producto) {
        productoClaveTextField.setText(String.valueOf(producto.getClave()));
        productoDescripcionTextField.setText(String.valueOf(producto.getDescripcion()));
        productoPrecioTextField.setText(String.valueOf(producto.getPrecio()));
        productoExistenciaTextField.setText(String.valueOf(producto.getExistencia()));
    }


    private void initializeProductosTableColumns() {
        clearProductInputFields();
        setProductosCellValueFactories();
        setProductosButtonColumns();
        loadProductosData();
    }

    private void setProductosCellValueFactories() {
        productoClaveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
        productoDescripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        productoPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        productoExistenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));
    }

    private void setProductosButtonColumns() {
        productoEditarColumn.setCellFactory(param -> createButtonCell("Editar", this::editProducto));
        productoEliminarColumn.setCellFactory(param -> createButtonCell("Eliminar", this::deleteProducto));
    }

    private void loadProductosData() {
        List<DatabaseConnection.Producto> productosList = DatabaseConnection.getAllProductos();
        productosTable.setItems(FXCollections.observableArrayList(productosList));
        productosTable.refresh();
    }

    private void editProducto(DatabaseConnection.Producto producto) {
        populateProductoFields(producto);
    }

    private void deleteProducto(DatabaseConnection.Producto producto) {
        if (confirmDeletion("producto")) {
            DatabaseConnection.deleteProducto(producto.getClave());
            productosTable.getItems().remove(producto);
        }
    }


}
