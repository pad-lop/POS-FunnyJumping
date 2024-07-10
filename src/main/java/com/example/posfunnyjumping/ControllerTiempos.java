package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.function.Consumer;

public class ControllerTiempos {

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
    public TextField tiempoClaveTextField;
    @FXML
    private TextField tiempoMinutosTextField;
    @FXML
    private TextField tiempoPrecioTextField;

    @FXML
    private TableView<DatabaseManager.Tiempo> tiemposTable;

    @FXML
    private void initialize() {
        initializeTiemposTableColumns();
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private boolean confirmDeletion(String itemType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este " + itemType + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
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
    private void handleAddOrUpdateTiempo(ActionEvent event) {
        if (!validateTiempoInputs()) {
            showAlert("Please fill in all the fields.");
            return;
        }

        try {
            int minutos = Integer.parseInt(tiempoMinutosTextField.getText());
            double precio = Double.parseDouble(tiempoPrecioTextField.getText());

            if (tiempoClaveTextField.getText().isEmpty()) {
                DatabaseManager.Tiempo nuevoTiempo = new DatabaseManager.Tiempo(0, minutos, precio);
                DatabaseManager.TiempoDAO.insert(nuevoTiempo);
            } else {
                int clave = Integer.parseInt(tiempoClaveTextField.getText());
                DatabaseManager.Tiempo tiempoActualizado = new DatabaseManager.Tiempo(clave, minutos, precio);
                DatabaseManager.TiempoDAO.update(tiempoActualizado);
            }

            initializeTiemposTableColumns();
            clearTiempoInputFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid number format in input fields.");
        } catch (DatabaseManager.DatabaseException e) {
            showAlert("Database error: " + e.getMessage());
        }
    }

    private boolean validateTiempoInputs() {
        return !tiempoMinutosTextField.getText().isEmpty()
                && !tiempoPrecioTextField.getText().isEmpty();
    }

    private void clearTiempoInputFields() {
        tiempoClaveTextField.clear();
        tiempoMinutosTextField.clear();
        tiempoPrecioTextField.clear();
    }

    private void populateTiempoFields(DatabaseManager.Tiempo tiempo) {
        tiempoClaveTextField.setText(String.valueOf(tiempo.getClave()));
        tiempoMinutosTextField.setText(String.valueOf(tiempo.getMinutos()));
        tiempoPrecioTextField.setText(String.valueOf(tiempo.getPrecio()));
    }

    private void initializeTiemposTableColumns() {
        clearTiempoInputFields();
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
            showAlert("Error loading tiempos: " + e.getMessage());
        }
    }

    private void editTiempo(DatabaseManager.Tiempo tiempo) {
        populateTiempoFields(tiempo);
    }

    private void deleteTiempo(DatabaseManager.Tiempo tiempo) {
        if (confirmDeletion("tiempo")) {
            try {
                DatabaseManager.TiempoDAO.delete(tiempo.getClave());
                tiemposTable.getItems().remove(tiempo);
            } catch (DatabaseManager.DatabaseException e) {
                showAlert("Error deleting tiempo: " + e.getMessage());
            }
        }
    }
}