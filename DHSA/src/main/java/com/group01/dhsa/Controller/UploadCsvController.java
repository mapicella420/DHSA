package com.group01.dhsa.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class UploadCsvController {

    @FXML
    private TextField filePathField;

    @FXML
    private Button browseButton;

    @FXML
    private Button uploadButton;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        uploadButton.setDisable(true); // Disabilita il pulsante di upload fino a quando non viene selezionato un file
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        configureFileChooser(fileChooser);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
            uploadButton.setDisable(false);
        }
    }

    @FXML
    private void handleUpload() {
        String filePath = filePathField.getText();
        if (!filePath.isEmpty()) {
            statusLabel.setText("Uploading file: " + filePath);
            // Implementa qui la logica per gestire il file CSV
            processCsvFile(filePath);
            statusLabel.setText("Upload completed successfully.");
        } else {
            statusLabel.setText("Please select a file before uploading.");
        }
    }

    private void configureFileChooser(FileChooser fileChooser) {
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
    }

    private void processCsvFile(String filePath) {
        // Logica per processare il file CSV
        System.out.println("Processing CSV file: " + filePath);
        // Potrebbe includere la lettura del file, la validazione dei dati, ecc.
    }
}
