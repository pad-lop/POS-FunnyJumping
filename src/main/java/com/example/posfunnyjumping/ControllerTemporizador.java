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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;


public class ControllerTemporizador {
    private static final Logger logger = LoggerFactory.getLogger(ControllerTemporizador.class);

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
    private TableColumn<DatabaseManager.TemporizadorDAO.Temporizador, Void> temporizadorRestanteColumn;
    @FXML
    private TableView<DatabaseManager.TemporizadorDAO.Temporizador> temporizadoresTable;

    private Timeline timeline;

    @FXML
    private void initialize() {
        initializeTemporizadoresTableColumns();
        startPeriodicUpdate();
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
        setTemporizadorRestanteColumn();
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
            String remainingTime = calculateRemainingTime(temporizador.getFecha(), temporizador.getMinutos());
            DatabaseManager.TemporizadorDAO.stop(temporizador.getClave(), remainingTime);
            initializeTemporizadoresTableColumns();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Error stopping temporizador", e);
            showAlert("An error occurred while stopping the temporizador.");
        }
    }
}


    private String calculateRemainingTime(LocalDateTime startTime, float totalMinutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusMinutes((long) totalMinutes);

        java.time.Duration remainingDuration = java.time.Duration.between(now, endTime);
        long hours = remainingDuration.toHours();
        long minutes = remainingDuration.toMinutesPart();
        long seconds = remainingDuration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
private void setTemporizadorRestanteColumn() {
    temporizadorRestanteColumn.setCellFactory(column -> new TableCell<>() {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
            } else {
                DatabaseManager.TemporizadorDAO.Temporizador temporizador = getTableView().getItems().get(getIndex());
                if (temporizador.isActivo()) {
                    setText(calculateRemainingTime(temporizador.getFecha(), temporizador.getMinutos()));
                } else {
                    setText(temporizador.getTiempoRestante() != null ? temporizador.getTiempoRestante() : "Detenido");
                }
            }
        }
    });
}
    private void startPeriodicUpdate() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            temporizadoresTable.refresh();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stopPeriodicUpdate() {
        if (timeline != null) {
            timeline.stop();
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