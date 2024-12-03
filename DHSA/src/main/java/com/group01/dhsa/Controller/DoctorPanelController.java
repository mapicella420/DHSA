package com.group01.dhsa.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class DoctorPanelController {

    @FXML
    private Button dischargePatientButton;

    @FXML
    public void initialize() {
        // Initial setup logic for the screen, if needed
    }

    // Action for "Discharge Patient" button
    @FXML
    private void onDischargePatientClick() {
        System.out.println("Discharge Patient button clicked!");
        // Logic for discharging a patient
    }

    // Action for "Close App" menu item
    @FXML
    private void onCloseApp() {
        System.out.println("Closing application...");
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        stage.close();
    }

    // Action for "Upload CSV File" menu item
    @FXML
    private void onUploadCsvMenuClick() {
        System.out.println("Upload CSV menu item clicked!");
        // Open File Chooser to select CSV file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println("Selected CSV file: " + selectedFile.getAbsolutePath());
            // Logic to process the CSV file
        } else {
            System.out.println("CSV file selection cancelled.");
        }
    }

    // Action for "Import DICOM File" menu item
    @FXML
    private void onImportDicomMenuClick() {
        System.out.println("Import DICOM menu item clicked!");
        // Open File Chooser to select DICOM file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DICOM File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("DICOM Files", "*.dcm")
        );
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println("Selected DICOM file: " + selectedFile.getAbsolutePath());
            // Logic to process the DICOM file
        } else {
            System.out.println("DICOM file selection cancelled.");
        }
    }

    // Action for "Convert CSV to FHIR" menu item
    @FXML
    private void onConvertCsvClick() {
        System.out.println("Convert CSV to FHIR menu item clicked!");
        // Logic to convert CSV data to FHIR resources
    }
}
