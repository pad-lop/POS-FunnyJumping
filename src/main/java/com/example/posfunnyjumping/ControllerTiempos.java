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

public class ControllerTiempos {
    private static final Logger logger = LoggerFactory.getLogger(ControllerTiempos.class);

    @FXML
    private TableColumn<DatabaseManager.Tiempo, Integer> tiempoClaveColumn;
    @FXML
    private TableColumn<DatabaseManager.Tiempo, Integer> tiempoMinutosColumn;
    @FXML
    private TableColumn<DatabaseManager.Tiempo, Double> tiempoPrecioColumn;
    @FXML
    private TableColumn<DatabaseManager.Tiempo, Void> tiempoEditarColumn;
    @FXML
    private TableColumn<DatabaseManager.Tiempo, Void> tiempoEliminarColumn;

    @FXML
    private TableView<DatabaseManager.Tiempo> tiemposTable;

    @FXML
    private void initialize() {
        initializeTiemposTableColumns();
    }

    private void editTiempo(DatabaseManager.Tiempo tiempo) {
        showTiempoDialog(tiempo);
    }

    private boolean confirmDeletion(String itemType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este " + itemType + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showTiempoDialog(DatabaseManager.Tiempo tiempo) {
        Dialog<DatabaseManager.Tiempo> dialog = createTiempoDialog(tiempo);
        Optional<DatabaseManager.Tiempo> result = dialog.showAndWait();
        result.ifPresent(this::saveTiempo);
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private GridPane createDialogGrid(DatabaseManager.Tiempo tiempo) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField claveField = new TextField(String.valueOf(tiempo.getClave()));
        claveField.setEditable(false);
        TextField minutosField = new TextField(String.valueOf(tiempo.getMinutos()));
        minutosField.setId("minutosField");
        TextField precioField = new TextField(String.valueOf(tiempo.getPrecio()));
        precioField.setId("precioField");

        grid.add(new Label("Clave:"), 0, 0);
        grid.add(claveField, 1, 0);
        grid.add(new Label("Minutos:"), 0, 1);
        grid.add(minutosField, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(precioField, 1, 2);

        minutosField.setTooltip(new Tooltip("Ingrese los minutos del tiempo"));
        precioField.setTooltip(new Tooltip("Ingrese el precio del tiempo"));

        return grid;
    }

    private Dialog<DatabaseManager.Tiempo> createTiempoDialog(DatabaseManager.Tiempo tiempo) {
        Dialog<DatabaseManager.Tiempo> dialog = new Dialog<>();
        dialog.setTitle(tiempo.getClave() == 0 ? "Agregar Tiempo" : "Editar Tiempo");
        dialog.setHeaderText("Ingrese los detalles del tiempo");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createDialogGrid(tiempo);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return createTiempoFromInput(grid, tiempo.getClave());
                } catch (IllegalArgumentException e) {
                    showAlert(e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void saveTiempo(DatabaseManager.Tiempo tiempo) {
        try {
            if (tiempo.getClave() == 0) {
                DatabaseManager.TiempoDAO.insert(tiempo);
            } else {
                DatabaseManager.TiempoDAO.update(tiempo);
            }
            loadTiemposData();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Database error", e);
            showAlert("Ocurrió un error al guardar el tiempo.");
        }
    }

    private DatabaseManager.Tiempo createTiempoFromInput(GridPane grid, int clave) {
        TextField minutosField = (TextField) grid.lookup("#minutosField");
        TextField precioField = (TextField) grid.lookup("#precioField");

        int minutos;
        try {
            minutos = Integer.parseInt(minutosField.getText());
            if (minutos <= 0) throw new IllegalArgumentException();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los minutos deben ser un número entero positivo.");
        }

        double precio;
        try {
            precio = Double.parseDouble(precioField.getText());
            if (precio < 0) throw new IllegalArgumentException();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número positivo.");
        }

        return new DatabaseManager.Tiempo(clave, minutos, precio);
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

    private void initializeTiemposTableColumns() {
        setTiemposCellValueFactories();
        setTiemposButtonColumns();
        loadTiemposData();
    }

    private void setTiemposCellValueFactories() {
        tiempoClaveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
        tiempoMinutosColumn.setCellValueFactory(new PropertyValueFactory<>("minutos"));
        tiempoPrecioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
    }

    private void setTiemposButtonColumns() {
        tiempoEditarColumn.setCellFactory(param -> createButtonCell("Editar", this::editTiempo));
        tiempoEliminarColumn.setCellFactory(param -> createButtonCell("Eliminar", this::deleteTiempo));
    }

    private void loadTiemposData() {
        try {
            List<DatabaseManager.Tiempo> tiemposList = DatabaseManager.TiempoDAO.getAll();
            tiemposTable.setItems(FXCollections.observableArrayList(tiemposList));
            tiemposTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Error loading tiempos data", e);
            showAlert("An error occurred while loading the tiempos.");
        }
    }

    private void deleteTiempo(DatabaseManager.Tiempo tiempo) {
        if (confirmDeletion("tiempo")) {
            try {
                DatabaseManager.TiempoDAO.delete(tiempo.getClave());
                tiemposTable.getItems().remove(tiempo);
            } catch (DatabaseManager.DatabaseException e) {
                logger.error("Error deleting tiempo", e);
                showAlert("An error occurred while deleting the tiempo.");
            }
        }
    }

    @FXML
    private void onAddTiempo() {
        DatabaseManager.Tiempo newTiempo = new DatabaseManager.Tiempo(0, 0, 0.0);
        showTiempoDialog(newTiempo);
    }
}