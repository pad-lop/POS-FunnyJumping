package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ControllerProductos {
    private static final Logger logger = LoggerFactory.getLogger(ControllerProductos.class);

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
    private TableView<DatabaseManager.Producto> productosTable;

    @FXML
    private void initialize() {
        initializeProductosTableColumns();
    }

    private void editProducto(DatabaseManager.Producto producto) {
        showProductDialog(producto);
    }

    @FXML
    private void onAddProducto() {
        DatabaseManager.Producto newProducto = new DatabaseManager.Producto(0, "", 0.0, 0.0);
        showProductDialog(newProducto);
    }

    private boolean confirmDeletion(String itemType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este " + itemType + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showProductDialog(DatabaseManager.Producto producto) {
        Dialog<DatabaseManager.Producto> dialog = createProductDialog(producto);
        Optional<DatabaseManager.Producto> result = dialog.showAndWait();
        result.ifPresent(this::saveProduct);
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private GridPane createDialogGrid(DatabaseManager.Producto producto) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField claveField = new TextField(String.valueOf(producto.getClave()));
        claveField.setEditable(false);
        TextField descripcionField = new TextField(producto.getDescripcion());
        descripcionField.setId("descripcionField");
        TextField precioField = new TextField(String.valueOf(producto.getPrecio()));
        precioField.setId("precioField");
        TextField existenciaField = new TextField(String.valueOf(producto.getExistencia()));
        existenciaField.setId("existenciaField");

        grid.add(new Label("Clave:"), 0, 0);
        grid.add(claveField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionField, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(precioField, 1, 2);
        grid.add(new Label("Existencia:"), 0, 3);
        grid.add(existenciaField, 1, 3);

        // Add tooltips for accessibility
        descripcionField.setTooltip(new Tooltip("Ingrese la descripción del producto"));
        precioField.setTooltip(new Tooltip("Ingrese el precio del producto"));
        existenciaField.setTooltip(new Tooltip("Ingrese la cantidad en existencia del producto"));

        return grid;
    }

    private Dialog<DatabaseManager.Producto> createProductDialog(DatabaseManager.Producto producto) {
        Dialog<DatabaseManager.Producto> dialog = new Dialog<>();
        dialog.setTitle(producto.getClave() == 0 ? "Agregar Producto" : "Editar Producto");
        dialog.setHeaderText("Ingrese los detalles del producto");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createDialogGrid(producto);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return createProductFromInput(grid, producto.getClave());
                } catch (IllegalArgumentException e) {
                    showAlert(e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void saveProduct(DatabaseManager.Producto producto) {
        try {
            if (producto.getClave() == 0) {
                DatabaseManager.ProductoDAO.insert(producto);
            } else {
                DatabaseManager.ProductoDAO.update(producto);
            }
            loadProductosData();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Database error", e);
            showAlert("Ocurrió un error al guardar el producto.");
        }
    }

    private DatabaseManager.Producto createProductFromInput(GridPane grid, int clave) {
        TextField descripcionField = (TextField) grid.lookup("#descripcionField");
        TextField precioField = (TextField) grid.lookup("#precioField");
        TextField existenciaField = (TextField) grid.lookup("#existenciaField");

        String descripcion = descripcionField.getText();
        if (descripcion.isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía.");
        }

        double precio;
        try {
            precio = Double.parseDouble(precioField.getText());
            if (precio < 0) throw new IllegalArgumentException();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número positivo.");
        }

        double existencia;
        try {
            existencia = Double.parseDouble(existenciaField.getText());
            if (existencia < 0) throw new IllegalArgumentException();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La existencia debe ser un número positivo.");
        }

        return new DatabaseManager.Producto(clave, descripcion, precio, existencia);
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

    private void initializeProductosTableColumns() {
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