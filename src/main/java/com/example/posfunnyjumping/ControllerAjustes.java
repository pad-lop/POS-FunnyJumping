package com.example.posfunnyjumping;

import javafx.fxml.FXML;
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

            if (selectedPrinter != null && !logoPath.isEmpty()) {
                saveSettings(selectedPrinter, logoPath);
            } else {
                System.out.println("Please select a printer and logo file.");
            }
        });
    }

  private void saveSettings(String printer, String logoPath) {
        Properties props = new Properties();
        props.setProperty("Printer", printer);
        props.setProperty("LogoPath", logoPath);
        props.setProperty("RegularFontPath", regularFontPath.getText());
        props.setProperty("BoldFontPath", boldFontPath.getText());

        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            props.store(out, "POS Funny Jumping Settings");
            System.out.println("Settings saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving settings: " + e.getMessage());
        }
    }

    private void loadSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
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

            System.out.println("Settings loaded successfully.");
        } catch (IOException e) {
            System.out.println("No existing settings file found. Starting with default values.");
        }
    }
}