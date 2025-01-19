package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UploadCSVController {

    @FXML
    private Button organizationFileChooser;
    @FXML
    private CheckBox organizationCheckBox;
    @FXML
    private Button patientFileChooser;
    @FXML
    private CheckBox patientCheckBox;
    @FXML
    private Button providersFileChooser;
    @FXML
    private CheckBox providersCheckBox;
    @FXML
    private Button encountersFileChooser;
    @FXML
    private CheckBox encountersCheckBox;
    @FXML
    private Button devicesFileChooser;

    @FXML
    private Button allergieFileChooser;
    @FXML
    private Button carePlanFileChooser;
    @FXML
    private Button procedureFileChooser;
    @FXML
    private Button conditionsFileChooser;

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
    @FXML
    private Button backButton;

    @FXML
    private Label fileCountLabel; // Conteggio file caricati
    @FXML
    private Label statusLabel; // Stato attuale

    private final Map<String, File> selectedFiles = new HashMap<>();
    private final EventObservable eventManager;

    /**
     * Constructor for dependency injection of the EventManager.
     */
    public UploadCSVController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    @FXML
    private void initialize() {
        level5Pane.setDisable(true);
        uploadButton.setDisable(true);
        fileCountLabel.setText("0/0 files uploaded");
        statusLabel.setText("...");
    }

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
    private void onChooseConditionsFile() {
        handleFileChooser("conditions", conditionsFileChooser);
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

    private void handleFileChooser(String key, Button button) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File for " + key );
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) button.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedFiles.put(key, file);
            System.out.println("Selected file for " + key + ": " + file.getAbsolutePath());

            button.setText(file.getName());
        }

        updateUploadState();
    }


    private void updateUploadState() {
        boolean mandatoryFilesSelected = (organizationCheckBox.isSelected() || selectedFiles.containsKey("organization")) &&
                (patientCheckBox.isSelected() || selectedFiles.containsKey("patient")) &&
                (providersCheckBox.isSelected() || selectedFiles.containsKey("providers")) &&
                (encountersCheckBox.isSelected() || selectedFiles.containsKey("encounters"));
        boolean mandatoryFilesSelected2 = (organizationCheckBox.isSelected() || selectedFiles.containsKey("organization")) ||
                (patientCheckBox.isSelected() || selectedFiles.containsKey("patient")) ||
                (providersCheckBox.isSelected() || selectedFiles.containsKey("providers")) ||
                (encountersCheckBox.isSelected() || selectedFiles.containsKey("encounters"));

        uploadButton.setDisable(!mandatoryFilesSelected2);
        level5Pane.setDisable(!mandatoryFilesSelected);
    }

    @FXML
    private void onCheckBoxChange(ActionEvent actionEvent) {
        CheckBox source = (CheckBox) actionEvent.getSource();
        String key = null;

        if (source.equals(organizationCheckBox)) {
            key = "organization";
        } else if (source.equals(patientCheckBox)) {
            key = "patient";
        } else if (source.equals(providersCheckBox)) {
            key = "providers";
        } else if (source.equals(encountersCheckBox)) {
            key = "encounters";
        }

        if (key != null && source.isSelected()) {
            selectedFiles.remove(key);
        }

        updateUploadState();
    }

    @FXML
    private void onUpload() {
        statusLabel.setVisible(true);
        fileCountLabel.setVisible(true);
        uploadButton.setDisable(true);
        fileCountLabel.setText("0/" + selectedFiles.size() + " files uploaded");
        statusLabel.setText("Uploading...");

        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int totalFiles = selectedFiles.size();
                int[] completed = {0};

                for (Map.Entry<String, File> entry : selectedFiles.entrySet()) {
                    String key = entry.getKey();
                    File file = entry.getValue();

                    if (file != null) {
                        updateMessage("Uploading: " + key + " (" + file.getName() + ")");
                        eventManager.notify("csv_upload", file);
                        completed[0]++;
                        updateProgress(completed[0], totalFiles);

                        javafx.application.Platform.runLater(() -> {
                            fileCountLabel.setText(completed[0] + "/" + totalFiles + " files uploaded");
                        });

                        Thread.sleep(500); // Simulating delay
                    }
                }

                return null;
            }
        };

        statusLabel.textProperty().bind(uploadTask.messageProperty());

        uploadTask.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Upload Completed!");
            uploadButton.setDisable(false);
        });

        Thread thread = new Thread(uploadTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onBack() {
        System.out.println("Returning to the previous screen...");
        Stage stage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "Doctor Dashboard");
    }
}
