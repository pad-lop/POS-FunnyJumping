package com.example.posfunnyjumping;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Funny Jumping - Login");

        Login loginScreen = new Login();
        loginScreen.setOnLoginSuccess(user -> loadMainApplication(primaryStage));

        Scene scene = new Scene(loginScreen, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

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
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}