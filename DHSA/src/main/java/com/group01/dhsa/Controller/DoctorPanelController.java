package com.group01.dhsa.Controller;

import com.group01.dhsa.Model.CsvImporter;
import com.group01.dhsa.Model.DicomImporter;
import com.group01.dhsa.Model.ModelObserver;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class DoctorPanelController implements ModelObserver {

    @FXML
    private Button dischargePatientButton;

    private final CsvImporter csvImporter = new CsvImporter();
    private final DicomImporter dicomImporter = new DicomImporter();

    @FXML
    public void initialize() {
        // Registra il controller come observer per i model
        csvImporter.addObserver(this);
        dicomImporter.addObserver(this);
    }

    // Azione per il bottone "Discharge Patient"
    @FXML
    private void onDischargePatientClick() {
        System.out.println("Discharge Patient button clicked!");
        // Logica per dimettere il paziente
    }

    // Azione per il menu "Close App"
    @FXML
    private void onCloseApp() {
        System.out.println("Closing application...");
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        stage.close();
    }

    // Azione per il menu "Upload CSV File"
    @FXML
    private void onUploadCsvMenuClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Selected CSV file: " + selectedFile.getAbsolutePath());
            csvImporter.importCsv(selectedFile);
        } else {
            System.out.println("CSV file selection cancelled.");
        }
    }

    // Azione per il menu "Import DICOM File"
    @FXML
    private void onImportDicomMenuClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DICOM File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DICOM Files", "*.dcm"));

        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Selected DICOM file: " + selectedFile.getAbsolutePath());
            dicomImporter.importDicom(selectedFile);
        } else {
            System.out.println("DICOM file selection cancelled.");
        }
    }

    // Azione per il menu "Convert CSV to FHIR"
    @FXML
    private void onConvertCsvClick() {
        System.out.println("Convert CSV to FHIR menu item clicked!");
        // Implementa la logica per la conversione CSV-FHIR
    }

    // Implementazione del metodo dell'interfaccia ModelObserver
    @Override
    public void onModelUpdate(String message) {
        // Notifica all'utente l'evento avvenuto nel modello
        showAlert(message);
    }

    // Mostra un'alert all'utente
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
