package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class RefactoredProductosController {
    private static final Logger logger = LoggerFactory.getLogger(RefactoredProductosController.class);

    @FXML
    private TableColumn<DatabaseManager.Producto, Integer> productoClaveColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, String> productoDescripcionColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Double> productoPrecioColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Double> productoExistenciaColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Void> productoEditarColumn;
    @FXML
    private TableColumn<DatabaseManager.Producto, Void> productoEliminarColumn;
    @FXML
    private TextField productoClaveTextField;
    @FXML
    private TextField productoDescripcionTextField;
    @FXML
    private TextField productoPrecioTextField;
    @FXML
    private TextField productoExistenciaTextField;
    @FXML
    private TableView<DatabaseManager.Producto> productosTable;

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
        alert.setHeaderText(content);
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

            DatabaseManager.Producto producto = new DatabaseManager.Producto(
                productoClaveTextField.getText().isEmpty() ? 0 : Integer.parseInt(productoClaveTextField.getText()),
                descripcion,
                precio,
                existencia
            );

            if (productoClaveTextField.getText().isEmpty()) {
                DatabaseManager.ProductoDAO.insert(producto);
            } else {
                DatabaseManager.ProductoDAO.update(producto);
            }

            initializeProductosTableColumns();

        } catch (NumberFormatException e) {
            showAlert("Invalid number format in input fields.");
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Database error", e);
            showAlert("An error occurred while saving the product.");
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

    private void populateProductoFields(DatabaseManager.Producto producto) {
        productoClaveTextField.setText(String.valueOf(producto.getClave()));
        productoDescripcionTextField.setText(producto.getDescripcion());
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
        try {
            List<DatabaseManager.Producto> productosList = DatabaseManager.ProductoDAO.getAll();
            productosTable.setItems(FXCollections.observableArrayList(productosList));
            productosTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Error loading productos data", e);
            showAlert("An error occurred while loading the products.");
        }
    }

    private void editProducto(DatabaseManager.Producto producto) {
        populateProductoFields(producto);
    }

    private void deleteProducto(DatabaseManager.Producto producto) {
        if (confirmDeletion("producto")) {
            try {
                DatabaseManager.ProductoDAO.delete(producto.getClave());
                productosTable.getItems().remove(producto);
            } catch (DatabaseManager.DatabaseException e) {
                logger.error("Error deleting producto", e);
                showAlert("An error occurred while deleting the product.");
            }
        }
    }
}