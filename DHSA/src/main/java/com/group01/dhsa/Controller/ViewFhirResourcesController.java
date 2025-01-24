package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ViewFhirResourcesController {

    @FXML
    private ChoiceBox<String> resourceTypeChoiceBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Map<String, String>> fhirResourcesTable;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button searchButton;

    private final EventObservable eventManager;

    public ViewFhirResourcesController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
        // Registrazione agli eventi
        this.eventManager.subscribe("load_complete", (eventType, file) -> {
            updateTable(EventManager.getInstance().getCurrentResources());
            progressBar.setVisible(false);
            statusLabel.setText("Resources loaded successfully!");
        });

        this.eventManager.subscribe("search_complete", (eventType, file) -> {
            updateTable(EventManager.getInstance().getCurrentResources());
            progressBar.setVisible(false);
            statusLabel.setText("Search completed successfully!");
        });

        this.eventManager.subscribe("error", (eventType, file) -> {
            progressBar.setVisible(false);
            statusLabel.setText("An error occurred: " + (file != null ? file.getName() : "Unknown error"));
        });
    }

    @FXML
    private void initialize() {
        resourceTypeChoiceBox.getItems().addAll("Patient", "Device", "Organization", "Encounter", "Allerg",
                "Careplan", "Condition", "Procedure", "ImagingStudy", "Observation", "Immunization", "MedicationRequest");

        progressBar.setVisible(false);
        statusLabel.setText("...");
    }

    @FXML
    private void onLoadResources() {
        String selectedResourceType = resourceTypeChoiceBox.getValue();
        if (selectedResourceType == null || selectedResourceType.isEmpty()) {
            statusLabel.setText("Please select a resource type to load!");
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Loading resources...");
        // Notifica l'evento di caricamento
        EventManager.getInstance().getEventObservable().notify("load_request", new File(selectedResourceType));
    }

    @FXML
    private void onSearch() {
        String selectedResourceType = resourceTypeChoiceBox.getValue();
        String searchTerm = searchField.getText();

        if (selectedResourceType == null || selectedResourceType.isEmpty()) {
            statusLabel.setText("Please select a resource type to search!");
            return;
        }

        if (searchTerm == null || searchTerm.isEmpty()) {
            statusLabel.setText("Please enter a search term!");
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Searching for: " + searchTerm);
        // Notifica l'evento di ricerca
        EventManager.getInstance().getEventObservable().notify("search_request", new File(selectedResourceType + "_" + searchTerm));
    }

    @FXML
    private void onRefresh() {
        fhirResourcesTable.getItems().clear();
        progressBar.setVisible(false);
        statusLabel.setText("Table cleared and refreshed!");
    }

    @FXML
    private void onDownloadResource() {
        Map<String, String> selectedResource = fhirResourcesTable.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            statusLabel.setText("Please select a resource to download!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File saveFile = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

        if (saveFile != null) {
            try {
                String resourceData = selectedResource.toString();
                Files.writeString(saveFile.toPath(), resourceData);
                statusLabel.setText("Resource downloaded successfully!");
            } catch (Exception e) {
                statusLabel.setText("Error saving resource!");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "Doctor Dashboard");
    }

    private void updateTable(List<Map<String, String>> resources) {
        fhirResourcesTable.getColumns().clear();

        if (resources.isEmpty()) {
            statusLabel.setText("No resources found.");
            return;
        }

        Map<String, String> firstRow = resources.get(0);
        for (String key : firstRow.keySet()) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(data.getValue().get(key))
            );
            fhirResourcesTable.getColumns().add(column);
        }

        fhirResourcesTable.getItems().setAll(resources);
    }
}
