package com.example.posfunnyjumping;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

public class Login extends VBox {
    private TextField claveTextField;
    private PasswordField pwBox;
    private Label usernameLabel;
    private Label messageLabel;
    private Consumer<DatabaseManager.Usuario> onLoginSuccess;
    private static final String SETTINGS_FILE = "settings.txt";

    public Login() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f0f0f0;");

        // Load and add logo
        ImageView logoView = loadLogo();
        if (logoView != null) {
            getChildren().add(logoView);

            // Add a spacer
            Region spacer = new Region();
            spacer.setPrefHeight(20); // Adjust this value to increase or decrease space
            getChildren().add(spacer);
        }

        Label titleLabel = new Label("Funny Jumping");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        usernameLabel = new Label();
        usernameLabel.setTextFill(Color.web("#2c3e50"));
        usernameLabel.setVisible(false);

        claveTextField = new TextField();
        claveTextField.setPromptText("Clave de Usuario (ID)");
        claveTextField.setMaxWidth(250);

        pwBox = new PasswordField();
        pwBox.setPromptText("Contraseña");
        pwBox.setMaxWidth(250);

        Button loginButton = new Button("Ingresar");
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        loginButton.setMaxWidth(250);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        // Add focus listener to claveTextField
        claveTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {  // This means the textfield has lost focus
                lookupUser();
            }
        });

        // Add action listener for Enter key
        claveTextField.setOnAction(e -> lookupUser());

        loginButton.setOnAction(e -> attemptLogin());

        getChildren().addAll(titleLabel, usernameLabel, claveTextField, pwBox, loginButton, messageLabel);
    }

    private ImageView loadLogo() {
        Properties settings = loadSettings();
        String logoPath = settings.getProperty("LogoPath");

        if (logoPath == null || logoPath.isEmpty()) {
            System.out.println("Logo path not set in settings.");
            return null;
        }


        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                Image logo = new Image(new FileInputStream(logoPath));
                ImageView logoView = new ImageView(logo);

                // Set a maximum width and height for the logo
                double maxWidth = 350;  // Adjust as needed
                double maxHeight = 200; // Adjust as needed

                logoView.setPreserveRatio(true);
                logoView.setFitWidth(maxWidth);
                logoView.setFitHeight(maxHeight);

                return logoView;
            } catch (IOException e) {
                System.out.println("Error loading logo: " + e.getMessage());
            }
        }
        return null;
    }

    private static Properties loadSettings() {
        Properties props = new Properties();
        File settingsFile = new File(SETTINGS_FILE);

        if (!settingsFile.exists()) {
            System.out.println("Warning: settings.txt file not found. Using default settings.");
            return props; // Return empty properties
        }

        try (FileInputStream in = new FileInputStream(settingsFile)) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Error loading settings: " + e.getMessage());
        }
        return props;
    }

    private void lookupUser() {
        if (claveTextField.getText().isEmpty()) {
            usernameLabel.setText("");
            return;  // Don't perform lookup if the field is empty
        }

        try {
            int clave = Integer.parseInt(claveTextField.getText());
            Optional<DatabaseManager.Usuario> usuario = DatabaseManager.UsuarioDAO.getById(clave);
            if (usuario.isPresent()) {
                usernameLabel.setText("Usuario: " + usuario.get().getNombre());
                usernameLabel.setVisible(true);
                pwBox.requestFocus();
                messageLabel.setText("");
            } else {
                usernameLabel.setVisible(false);
                messageLabel.setText("Usuario no encontrado.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Por favor, ingrese un ID válido.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    private void attemptLogin() {
        String clave = claveTextField.getText();
        String password = pwBox.getText();

        // Check for admin credentials
        if ("admin".equals(clave) && "admin".equals(password)) {
            messageLabel.setText("¡Inicio de sesión exitoso como administrador!");
            messageLabel.setTextFill(Color.GREEN);
            if (onLoginSuccess != null) {
                // Create a temporary admin user object
                DatabaseManager.Usuario adminUser = new DatabaseManager.Usuario(-1, "Administrador", "admin");
                onLoginSuccess.accept(adminUser);
            }
            return;
        }

        // Existing login logic
        try {
            int claveId = Integer.parseInt(clave);
            Optional<DatabaseManager.Usuario> usuario = DatabaseManager.UsuarioDAO.getById(claveId);
            if (usuario.isPresent() && usuario.get().getContrasena().equals(password)) {
                messageLabel.setText("¡Inicio de sesión exitoso!");
                messageLabel.setTextFill(Color.GREEN);
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(usuario.get());
                }
            } else {
                messageLabel.setText("ID o contraseña inválidos.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Por favor, ingrese un ID válido.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    public void setOnLoginSuccess(Consumer<DatabaseManager.Usuario> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }
}