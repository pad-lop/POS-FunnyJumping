package com.example.posfunnyjumping;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;

public class Login extends VBox {
    private TextField userTextField;
    private PasswordField pwBox;
    private Label messageLabel;
    private Consumer<DatabaseManager.Usuario> onLoginSuccess;

    public Login() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f0f0f0;");

        Label titleLabel = new Label("Funny Jumping");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        userTextField = new TextField();
        userTextField.setPromptText("Clave de Usuario");
        userTextField.setMaxWidth(250);

        pwBox = new PasswordField();
        pwBox.setPromptText("Contraseña");
        pwBox.setMaxWidth(250);

        Button loginButton = new Button("Ingresar");
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        loginButton.setMaxWidth(250);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        loginButton.setOnAction(e -> attemptLogin());

        getChildren().addAll(titleLabel, userTextField, pwBox, loginButton, messageLabel);
    }

    private void attemptLogin() {
        String username = userTextField.getText();
        String password = pwBox.getText();

        List<DatabaseManager.Usuario> usuarios = DatabaseManager.UsuarioDAO.getAll();
        DatabaseManager.Usuario authenticatedUser = usuarios.stream()
                .filter(user -> user.getNombre().equals(username) && user.getContrasena().equals(password))
                .findFirst()
                .orElse(null);

        if (authenticatedUser != null) {
            messageLabel.setText("Acceso correcto.");
            messageLabel.setTextFill(Color.GREEN);
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(authenticatedUser);
            }
        } else {
            messageLabel.setText("Usuario o contraseña inválido.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    public void setOnLoginSuccess(Consumer<DatabaseManager.Usuario> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }
}