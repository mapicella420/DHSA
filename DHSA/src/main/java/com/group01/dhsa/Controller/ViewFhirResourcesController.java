package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.Model.FhirResources.FhirExporterFactoryManager;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
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

    private Task<List<Map<String, String>>> currentTask;



    public ViewFhirResourcesController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    @FXML
    private void initialize() {
        // Popola il ChoiceBox con i tipi di risorse disponibili
        resourceTypeChoiceBox.getItems().addAll("Patient", "Device",  "Organization", "Encounter", "Allerg", "Careplan", "Condition", "Procedure", "ImagingStudy", "Observation", "Immunization", "MedicationRequest");

        // Configura stato iniziale
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

        // Annulla il task corrente se esiste
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel();
        }

        progressBar.setVisible(true);
        statusLabel.setText("Loading resources...");

        currentTask = new Task<>() {
            @Override
            protected List<Map<String, String>> call() throws Exception {
                return loadResourcesForType(selectedResourceType);
            }
        };

        currentTask.setOnSucceeded(event -> {
            List<Map<String, String>> resources = currentTask.getValue();
            updateTable(resources);
            progressBar.setVisible(false);
        });

        currentTask.setOnFailed(event -> {
            statusLabel.setText("Error loading resources!");
            progressBar.setVisible(false);
        });

        new Thread(currentTask).start();
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

        // Annulla il task corrente se esiste
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel();
        }

        progressBar.setVisible(true);
        statusLabel.setText("Searching for: " + searchTerm);

        currentTask = new Task<>() {
            @Override
            protected List<Map<String, String>> call() throws Exception {
                return searchResources(selectedResourceType, searchTerm);
            }
        };

        currentTask.setOnSucceeded(event -> {
            List<Map<String, String>> resources = currentTask.getValue();
            updateTable(resources);
            progressBar.setVisible(false);
        });

        currentTask.setOnFailed(event -> {
            statusLabel.setText("Error during search!");
            progressBar.setVisible(false);
        });

        new Thread(currentTask).start();
    }


    @FXML
    private void onRefresh() {
        // Annulla il task corrente se esiste
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel();
            statusLabel.setText("Loading/Searching canceled.");
        }

        // Cancella i dati dalla tabella
        fhirResourcesTable.getItems().clear();

        // Reset della barra di avanzamento e dello stato
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

    private List<Map<String, String>> loadResourcesForType(String resourceType) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> resources = exporter.exportResources();
            System.out.println("Loaded resources for type " + resourceType + ": " + resources);

            return resources;
        } catch (Exception e) {
            System.err.println("Error loading resources for type: " + resourceType);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Map<String, String>> searchResources(String resourceType, String searchTerm) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> resources = exporter.searchResources(searchTerm);
            System.out.println("Search results for " + resourceType + " with term '" + searchTerm + "': " + resources);

            return resources;
        } catch (Exception e) {
            System.err.println("Error searching resources for type: " + resourceType + " with term: " + searchTerm);
            e.printStackTrace();
            return new ArrayList<>();
        }
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
            column.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(key)));
            fhirResourcesTable.getColumns().add(column);
        }

        fhirResourcesTable.getItems().setAll(resources);
        statusLabel.setText("Resources loaded successfully!");
    }
}
