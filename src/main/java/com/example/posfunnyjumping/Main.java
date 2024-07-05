package com.example.posfunnyjumping;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Temporizador.fxml")));
            Scene scene = new Scene(root);
            stage.setTitle("Funny Jumping");
            stage.setWidth(800);
            stage.setHeight(600);

            stage.setScene(scene);
            stage.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}