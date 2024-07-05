package com.example.posfunnyjumping;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RefactoredTemporizadorController {
    private static final Logger logger = LoggerFactory.getLogger(RefactoredTemporizadorController.class);

    @FXML
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, Integer> temporizadorClaveVentaColumn;
    @FXML
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, String> temporizadorNombreColumn;
    @FXML
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, LocalDateTime> temporizadorFechaColumn;
    @FXML
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, Float> temporizadorMinutosColumn;
    @FXML
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, Boolean> temporizadorActivoColumn;
    @FXML
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, Void> temporizadorDetenerColumn;

    @FXML
    private TableView<DatabaseManager.TemporizadorDAO.Temporizador> temporizadoresTable;

    @FXML
    private void initialize() {
        initializeTemporizadoresTableColumns();
    }

    private boolean confirmDeletion(String itemType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Detener");
        alert.setHeaderText("¿Está seguro que desea detener este " + itemType + "?");
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

    private void initializeTemporizadoresTableColumns() {
        setTemporizadoresCellValueFactories();
        setTemporizadoresButtonColumns();
        loadTemporizadoresData();
    }

    private void setTemporizadoresCellValueFactories() {
        temporizadorClaveVentaColumn.setCellValueFactory(new PropertyValueFactory<>("claveVenta"));
        temporizadorNombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        temporizadorFechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        temporizadorMinutosColumn.setCellValueFactory(new PropertyValueFactory<>("minutos"));
        temporizadorActivoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));
    }

    private void setTemporizadoresButtonColumns() {
        temporizadorDetenerColumn.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button("Detener");

            {
                button.setOnAction(event -> {
                    DatabaseManager.TemporizadorDAO.Temporizador temporizador = getTableView().getItems().get(getIndex());
                    stopTemporizador(temporizador);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    DatabaseManager.TemporizadorDAO.Temporizador temporizador = getTableView().getItems().get(getIndex());
                    if (temporizador.isActivo()) {
                        setGraphic(button);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadTemporizadoresData() {
        try {
            List<DatabaseManager.TemporizadorDAO.Temporizador> temporizadoresList = DatabaseManager.TemporizadorDAO.getAll();
            temporizadoresTable.setItems(FXCollections.observableArrayList(temporizadoresList));
            temporizadoresTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Error loading temporizadores data", e);
            showAlert("An error occurred while loading the temporizadores.");
        }
    }

    private void stopTemporizador(DatabaseManager.TemporizadorDAO.Temporizador temporizador) {
        if (confirmDeletion("temporizador")) {
            try {
                DatabaseManager.TemporizadorDAO.stop(temporizador.getClave());
                initializeTemporizadoresTableColumns();
            } catch (DatabaseManager.DatabaseException e) {
                logger.error("Error stopping temporizador", e);
                showAlert("An error occurred while stopping the temporizador.");
            }
        }
    }

    @FXML
    protected void onRegistrarVentaButtonClick(ActionEvent event) throws IOException {
        navigateTo("RegistrarVenta.fxml", event);
    }

    private void navigateTo(String fxmlFile, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
        Stage stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
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