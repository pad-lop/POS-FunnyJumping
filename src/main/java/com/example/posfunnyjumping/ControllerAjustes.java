package com.example.posfunnyjumping;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.*;
import java.util.Properties;

public class ControllerAjustes {

    @FXML
    private ComboBox<String> impresorasComboBox;

    @FXML
    private TextField ubicacionLogo;

    @FXML
    private Button buscarLogoButton;

    @FXML
    private Button guardarButton;

    @FXML
    private TextField regularFontPath;

    @FXML
    private TextField boldFontPath;

    @FXML
    private Button buscarRegularFontButton;

    @FXML
    private Button buscarBoldFontButton;

    @FXML
    private TextField precioTodoElDiaField;

    private static final String SETTINGS_FILE = "settings.txt";

    @FXML
    public void initialize() {
        loadPrinters();
        setupBuscarLogoButton();
        setupBuscarRegularFontButton();
        setupBuscarBoldFontButton();
        setupGuardarButton();
        loadSettings();

    }

    private void loadPrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer : printServices) {
            impresorasComboBox.getItems().add(printer.getName());
        }
    }

    private void setupBuscarRegularFontButton() {
        buscarRegularFontButton.setOnAction(event -> {
            File selectedFile = chooseFontFile("Seleccionar Fuente Regular");
            if (selectedFile != null) {
                regularFontPath.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void setupBuscarBoldFontButton() {
        buscarBoldFontButton.setOnAction(event -> {
            File selectedFile = chooseFontFile("Seleccionar Fuente Negrita");
            if (selectedFile != null) {
                boldFontPath.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private File chooseFontFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TTF Files", "*.ttf")
        );
        return fileChooser.showOpenDialog(new Stage());
    }

    private void setupBuscarLogoButton() {
        buscarLogoButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Logo");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                ubicacionLogo.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void setupGuardarButton() {
        guardarButton.setOnAction(event -> {
            String selectedPrinter = impresorasComboBox.getValue();
            String logoPath = ubicacionLogo.getText();
            String precioTodoElDia = precioTodoElDiaField.getText();

            if (selectedPrinter == null || logoPath.isEmpty() || precioTodoElDia.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Por favor, complete todos los campos.");
                return;
            }

            if (!isValidPrice(precioTodoElDia)) {
                showAlert(Alert.AlertType.ERROR, "Error", "El precio debe ser un número entero o decimal válido.");
                return;
            }

            saveSettings(selectedPrinter, logoPath, precioTodoElDia);
        });
    }

    private boolean isValidPrice(String price) {
        try {
            Double.parseDouble(price);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void saveSettings(String printer, String logoPath, String precioTodoElDia) {
        Properties props = new Properties();
        props.setProperty("Printer", printer);
        props.setProperty("LogoPath", logoPath);
        props.setProperty("RegularFontPath", regularFontPath.getText());
        props.setProperty("BoldFontPath", boldFontPath.getText());
        props.setProperty("PrecioTodoElDia", precioTodoElDia);

        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            props.store(out, "POS Funny Jumping Settings");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Configuración guardada exitosamente.");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al guardar la configuración: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);
        Properties props = new Properties();

        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
                System.out.println("Created new settings file: " + SETTINGS_FILE);
            } catch (IOException e) {
                System.out.println("Error creating settings file: " + e.getMessage());
                return;
            }
        }

        try (FileInputStream in = new FileInputStream(settingsFile)) {
            props.load(in);

            String savedPrinter = props.getProperty("Printer");
            if (savedPrinter != null && impresorasComboBox.getItems().contains(savedPrinter)) {
                impresorasComboBox.setValue(savedPrinter);
            }

            String savedLogoPath = props.getProperty("LogoPath");
            if (savedLogoPath != null) {
                ubicacionLogo.setText(savedLogoPath);
            }

            String savedRegularFontPath = props.getProperty("RegularFontPath");
            if (savedRegularFontPath != null) {
                regularFontPath.setText(savedRegularFontPath);
            }

            String savedBoldFontPath = props.getProperty("BoldFontPath");
            if (savedBoldFontPath != null) {
                boldFontPath.setText(savedBoldFontPath);
            }

            String savedPrecioTodoElDia = props.getProperty("PrecioTodoElDia");
            if (savedPrecioTodoElDia != null) {
                precioTodoElDiaField.setText(savedPrecioTodoElDia);
            }

            System.out.println("Settings loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error loading settings: " + e.getMessage());
        }
    }
}