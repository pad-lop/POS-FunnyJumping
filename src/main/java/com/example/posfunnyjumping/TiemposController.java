package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.function.Consumer;

public class TiemposController  {

    @FXML
    private TableColumn<DatabaseConnection.Tiempo, Integer> tiempoClaveColumn;
    @FXML
    private TableColumn<DatabaseConnection.Tiempo, Integer> tiempoMinutosColumn;
    @FXML
    private TableColumn<DatabaseConnection.Tiempo, Double> tiempoPrecioColumn;
    @FXML
    private TableColumn<DatabaseConnection.Tiempo, Void> tiempoEditarColumn;
    @FXML
    private TableColumn<DatabaseConnection.Tiempo, Void> tiempoEliminarColumn;

    @FXML
    public TextField tiempoClaveTextField;
    @FXML
    private TextField tiempoMinutosTextField;
    @FXML
    private TextField tiempoPrecioTextField;

    @FXML
    private TableView<DatabaseConnection.Tiempo> tiemposTable;

    @FXML
    private void initialize() {
        initializeTiemposTableColumns();
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(content);
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
                DatabaseConnection.insertTiempo(minutos, precio);
            } else {
                int clave = Integer.parseInt(tiempoClaveTextField.getText());
                DatabaseConnection.updateTiempo(clave, minutos, precio);
            }

            initializeTiemposTableColumns();
            clearTiempoInputFields();
        } catch (NumberFormatException e) {
            showAlert("Invalid number format in input fields.");
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

    private void populateTiempoFields(DatabaseConnection.Tiempo tiempo) {
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
        List<DatabaseConnection.Tiempo> tiemposList = DatabaseConnection.getAllTiempos();
        tiemposTable.setItems(FXCollections.observableArrayList(tiemposList));
        tiemposTable.refresh();

    }
    private void editTiempo(DatabaseConnection.Tiempo tiempo) {
        populateTiempoFields(tiempo);
    }

    private void deleteTiempo(DatabaseConnection.Tiempo tiempo) {
        if (confirmDeletion("tiempo")) {
            DatabaseConnection.deleteTiempo(tiempo.getClave());
            tiemposTable.getItems().remove(tiempo);
        }
    }

}


