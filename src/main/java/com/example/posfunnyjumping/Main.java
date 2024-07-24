package com.example.posfunnyjumping;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String SETTINGS_FILE = "settings.txt";

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database and create tables
            DatabaseManager.initializeDatabase();

             Properties settings = loadSettings();
            String logoPath = settings.getProperty("LogoPath");

            if (logoPath == null || logoPath.isEmpty()) {
                System.out.println("Logo path not set in settings.");
            } else {
                try {
                    File file = new File(logoPath);
                    if (file.exists()) {
                        Image icon = new Image(new FileInputStream(file));
                        primaryStage.getIcons().add(icon);
                    } else {
                        System.out.println("Logo file does not exist: " + logoPath);
                    }
                } catch (IOException e) {
                    System.out.println("Error loading logo: " + e.getMessage());
                }
            }

            primaryStage.setTitle("Funny Jumping - Login");

            Login loginScreen = new Login();
            loginScreen.setOnLoginSuccess(user -> loadMainApplication(primaryStage));

            Scene scene = new Scene(loginScreen, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            logger.error("Error initializing application", e);
            // You might want to show an error dialog to the user here
        }
    }

    private void loadMainApplication(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Temporizador.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Funny Jumping");
            stage.setWidth(800);
            stage.setHeight(600);

            Properties settings = loadSettings();
            String logoPath = settings.getProperty("LogoPath");

            if (logoPath == null || logoPath.isEmpty()) {
                System.out.println("Logo path not set in settings.");
            } else {
                try {
                    File file = new File(logoPath);
                    if (file.exists()) {
                        Image icon = new Image(new FileInputStream(file));
                        stage.getIcons().add(icon);
                    } else {
                        System.out.println("Logo file does not exist: " + logoPath);
                    }
                } catch (IOException e) {
                    System.out.println("Error loading logo: " + e.getMessage());
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading main application", ex);
            // You might want to show an error dialog to the user here
        }
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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        // Close the database connection when the application stops
        DatabaseManager.closeDataSource();
    }
}