package com.example.posfunnyjumping;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database and create tables
            DatabaseManager.initializeDatabase();

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
        } catch (Exception ex) {
            logger.error("Error loading main application", ex);
            // You might want to show an error dialog to the user here
        }
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