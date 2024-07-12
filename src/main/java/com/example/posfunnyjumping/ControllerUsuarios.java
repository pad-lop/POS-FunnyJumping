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

public class ControllerUsuarios {
    private static final Logger logger = LoggerFactory.getLogger(ControllerUsuarios.class);

    @FXML
    private TableColumn<DatabaseManager.Usuario, Integer> usuarioClaveColumn;
    @FXML
    private TableColumn<DatabaseManager.Usuario, String> usuarioNombreColumn;
    @FXML
    private TableColumn<DatabaseManager.Usuario, String> usuarioContrasenaColumn;
    @FXML
    private TableColumn<DatabaseManager.Usuario, Void> usuarioEditarColumn;
    @FXML
    private TableColumn<DatabaseManager.Usuario, Void> usuarioEliminarColumn;

    @FXML
    private TableView<DatabaseManager.Usuario> usuariosTable;

    @FXML
    private void initialize() {
        initializeUsuariosTableColumns();
    }

    private void editUsuario(DatabaseManager.Usuario usuario) {
        showUsuarioDialog(usuario);
    }

    private boolean confirmDeletion(String itemType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro que desea eliminar este " + itemType + "?");
        alert.setContentText("Esta acción no se puede deshacer.");

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showUsuarioDialog(DatabaseManager.Usuario usuario) {
        Dialog<DatabaseManager.Usuario> dialog = createUsuarioDialog(usuario);
        Optional<DatabaseManager.Usuario> result = dialog.showAndWait();
        result.ifPresent(this::saveUsuario);
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private GridPane createDialogGrid(DatabaseManager.Usuario usuario) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField claveField = new TextField(String.valueOf(usuario.getClave()));
        claveField.setEditable(false);
        TextField nombreField = new TextField(usuario.getNombre());
        nombreField.setId("nombreField");
        PasswordField contrasenaField = new PasswordField();
        contrasenaField.setId("contrasenaField");
        if (usuario.getClave() != 0) {
            contrasenaField.setPromptText("Deje en blanco para no cambiar");
        }

        grid.add(new Label("Clave:"), 0, 0);
        grid.add(claveField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nombreField, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2);
        grid.add(contrasenaField, 1, 2);

        nombreField.setTooltip(new Tooltip("Ingrese el nombre del usuario"));
        contrasenaField.setTooltip(new Tooltip("Ingrese la contrasena del usuario"));

        return grid;
    }

    private Dialog<DatabaseManager.Usuario> createUsuarioDialog(DatabaseManager.Usuario usuario) {
        Dialog<DatabaseManager.Usuario> dialog = new Dialog<>();
        dialog.setTitle(usuario.getClave() == 0 ? "Agregar Usuario" : "Editar Usuario");
        dialog.setHeaderText("Ingrese los detalles del usuario");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createDialogGrid(usuario);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return createUsuarioFromInput(grid, usuario.getClave());
                } catch (IllegalArgumentException e) {
                    showAlert(e.getMessage());
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private void saveUsuario(DatabaseManager.Usuario usuario) {
        try {
            if (usuario.getClave() == 0) {
                DatabaseManager.UsuarioDAO.insert(usuario);
            } else {
                DatabaseManager.UsuarioDAO.update(usuario);
            }
            loadUsuariosData();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Database error", e);
            showAlert("Ocurrió un error al guardar el usuario.");
        }
    }

    private DatabaseManager.Usuario createUsuarioFromInput(GridPane grid, int clave) {
        TextField nombreField = (TextField) grid.lookup("#nombreField");
        PasswordField contrasenaField = (PasswordField) grid.lookup("#contrasenaField");

        String nombre = nombreField.getText();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }

        String contrasena = contrasenaField.getText();
        if (clave == 0 && contrasena.isEmpty()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacía para un nuevo usuario.");
        }

        return new DatabaseManager.Usuario(clave, nombre, contrasena);
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

    private void initializeUsuariosTableColumns() {
        setUsuariosCellValueFactories();
        setUsuariosButtonColumns();
        loadUsuariosData();
    }

    private void setUsuariosCellValueFactories() {
        usuarioClaveColumn.setCellValueFactory(new PropertyValueFactory<>("clave"));
        usuarioNombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        usuarioContrasenaColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("********");
        });
    }

    private void setUsuariosButtonColumns() {
        usuarioEditarColumn.setCellFactory(param -> createButtonCell("Editar", this::editUsuario));
        usuarioEliminarColumn.setCellFactory(param -> createButtonCell("Eliminar", this::deleteUsuario));
    }

    private void loadUsuariosData() {
        try {
            List<DatabaseManager.Usuario> usuariosList = DatabaseManager.UsuarioDAO.getAll();
            usuariosTable.setItems(FXCollections.observableArrayList(usuariosList));
            usuariosTable.refresh();
        } catch (DatabaseManager.DatabaseException e) {
            logger.error("Error loading usuarios data", e);
            showAlert("An error occurred while loading the usuarios.");
        }
    }

    private void deleteUsuario(DatabaseManager.Usuario usuario) {
        if (confirmDeletion("usuario")) {
            try {
                DatabaseManager.UsuarioDAO.delete(usuario.getClave());
                usuariosTable.getItems().remove(usuario);
            } catch (DatabaseManager.DatabaseException e) {
                logger.error("Error deleting usuario", e);
                showAlert("An error occurred while deleting the usuario.");
            }
        }
    }

    @FXML
    private void onAddUsuario() {
        DatabaseManager.Usuario newUsuario = new DatabaseManager.Usuario(0, "", "");
        showUsuarioDialog(newUsuario);
    }
}