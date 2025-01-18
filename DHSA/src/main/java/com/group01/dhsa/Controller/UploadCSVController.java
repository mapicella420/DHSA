package com.group01.dhsa.Controller;

import com.group01.dhsa.Model.CsvImporter;
import com.group01.dhsa.Model.FhirResources.FhirImporterFactoryManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class UploadCSVController {

    @FXML
    private Button organizationFileChooser;
    @FXML
    private Button patientFileChooser;
    @FXML
    private Button providersFileChooser;
    @FXML
    private Button encountersFileChooser;
    @FXML
    private Button devicesFileChooser;

    @FXML
    private Button allergieFileChooser;
    @FXML
    private Button carePlanFileChooser;
    @FXML
    private Button procedureFileChooser;

    @FXML
    private Button observationFileChooser;
    @FXML
    private Button imagingStudiesFileChooser;

    @FXML
    private Button medicationsFileChooser;
    @FXML
    private Button immunizationsFileChooser;

    @FXML
    private GridPane level5Pane;
    @FXML
    private Button uploadButton;

    // Map to store selected files in the specified order
    private final Map<String, File> selectedFiles = new LinkedHashMap<>();

    /**
     * Initializes the controller by disabling Level 5 and upload button by default.
     */
    @FXML
    private void initialize() {
        level5Pane.setDisable(true);
        uploadButton.setDisable(true);
    }

    // File chooser handlers for Level 4
    @FXML
    private void onChooseOrganizationFile() {
        handleFileChooser("organization", organizationFileChooser);
    }

    @FXML
    private void onChoosePatientFile() {
        handleFileChooser("patient", patientFileChooser);
    }

    @FXML
    private void onChooseProvidersFile() {
        handleFileChooser("providers", providersFileChooser);
    }

    @FXML
    private void onChooseEncountersFile() {
        handleFileChooser("encounters", encountersFileChooser);
    }

    @FXML
    private void onChooseDevicesFile() {
        handleFileChooser("devices", devicesFileChooser);
    }

    // File chooser handlers for Level 5
    @FXML
    private void onChooseAllergieFile() {
        handleFileChooser("allergie", allergieFileChooser);
    }

    @FXML
    private void onChooseCarePlanFile() {
        handleFileChooser("carePlan", carePlanFileChooser);
    }

    @FXML
    private void onChooseProcedureFile() {
        handleFileChooser("procedure", procedureFileChooser);
    }

    @FXML
    private void onChooseObservationFile() {
        handleFileChooser("observation", observationFileChooser);
    }

    @FXML
    private void onChooseImagingStudiesFile() {
        handleFileChooser("imagingStudies", imagingStudiesFileChooser);
    }

    @FXML
    private void onChooseMedicationsFile() {
        handleFileChooser("medications", medicationsFileChooser);
    }

    @FXML
    private void onChooseImmunizationsFile() {
        handleFileChooser("immunizations", immunizationsFileChooser);
    }

    /**
     * Handles file selection for the given key and button.
     *
     * @param key    The key representing the file type.
     * @param button The button triggering the file chooser.
     */
    private void handleFileChooser(String key, Button button) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) button.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedFiles.put(key, file);
            System.out.println("Selected file for " + key + ": " + file.getAbsolutePath());
        }

        // Check if all Level 4 files are selected to enable Level 5
        if (areLevel4FilesSelected()) {
            level5Pane.setDisable(false);
        }

        // Enable upload button if at least one file is selected
        uploadButton.setDisable(selectedFiles.isEmpty());
    }

    /**
     * Checks if all Level 4 files are selected.
     *
     * @return True if all Level 4 files are selected, false otherwise.
     */
    private boolean areLevel4FilesSelected() {
        return selectedFiles.containsKey("organization") &&
                selectedFiles.containsKey("patient") &&
                selectedFiles.containsKey("providers") &&
                selectedFiles.containsKey("encounters") &&
                selectedFiles.containsKey("devices");
    }

    /**
     * Handles the upload action using FhirImporterFactoryManager.
     */
    @FXML
    private void onUpload() {
        System.out.println("Uploading selected files...");

        // Upload Level 4 files in order
        String[] level4Order = {"organization", "patient", "providers", "encounters", "devices"};
        for (String key : level4Order) {
            uploadFile(key);
        }

        // Upload Level 5 files (if selected)
        String[] level5Order = {"allergie", "carePlan", "procedure", "observation", "imagingStudies", "medications", "immunizations"};
        for (String key : level5Order) {
            if (selectedFiles.containsKey(key)) {
                uploadFile(key);
            }
        }

        System.out.println("Upload completed.");
    }

    /**
     * Uploads a file using FhirImporterFactoryManager.
     *
     * @param key The key representing the file type.
     */
    private void uploadFile(String key) {
        File file = selectedFiles.get(key);
        if (file != null) {
            try {
                System.out.println("Uploading " + key + ": " + file.getAbsolutePath());
                // Use the FhirImporterFactoryManager to process the file
                CsvImporter importer = new CsvImporter();
                importer.importCsv(file);
            } catch (Exception e) {
                System.err.println("Error uploading " + key + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected for " + key + ".");
        }
    }
}
