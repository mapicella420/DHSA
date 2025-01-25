package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.concurrent.Task;
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

    @FXML
    private ContextMenu patientContextMenu;

    @FXML
    private MenuItem viewCdaMenuItem;

    @FXML
    private MenuItem viewDicomMenuItem;


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

        this.eventManager.subscribe("cda_search_complete", (eventType, file) -> {
            try {
                System.out.println("[DEBUG] CDA search complete event triggered.");
                updateTable(EventManager.getInstance().getCurrentResources());
            } catch (Exception e) {
                System.err.println("Error in CDA_search_complete listener: " + e.getMessage());
                e.printStackTrace();
            }
        });

        this.eventManager.subscribe("dicom_search_complete", (eventType, file) -> {
            try {
                System.out.println("[DEBUG] DICOM search complete event triggered.");
                updateTable(EventManager.getInstance().getCurrentResources());
            } catch (Exception e) {
                System.err.println("Error in dicom_search_complete listener: " + e.getMessage());
                e.printStackTrace();
            }
        });


        this.eventManager.subscribe("error", (eventType, file) -> {
            progressBar.setVisible(false);
            statusLabel.setText("An error occurred: " + (file != null ? file.getName() : "Unknown error"));
        });
    }

    @FXML
    private void initialize() {
        resourceTypeChoiceBox.getItems().addAll(
                "Patient", "Device", "Organization", "Encounter", "Allerg",
                "Careplan", "Condition", "Procedure", "ImagingStudy",
                "Observation", "Immunization", "MedicationRequest"
        );

        progressBar.setVisible(false);
        statusLabel.setText("...");

        // Crea e configura il ContextMenu
        patientContextMenu = new ContextMenu();

        viewCdaMenuItem = new MenuItem("View CDA");
        viewCdaMenuItem.setOnAction(event -> onViewCda());

        viewDicomMenuItem = new MenuItem("View DICOM");
        viewDicomMenuItem.setOnAction(event -> onViewDicom());

        patientContextMenu.getItems().addAll(viewCdaMenuItem, viewDicomMenuItem);

        // Configura il comportamento del ContextMenu per ogni riga della tabella
        fhirResourcesTable.setRowFactory(tv -> {
            TableRow<Map<String, String>> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    Map<String, String> item = row.getItem();
                    System.out.println("[DEBUG] Selected row: " + item);

                    // Ottieni il valore selezionato dalla ChoiceBox
                    String selectedResourceType = resourceTypeChoiceBox.getValue();
                    String rowResourceType = selectedResourceType; // Usa direttamente il valore scelto dall'utente

                    // Mostra il ContextMenu solo se il ResourceType corrisponde al valore della ChoiceBox
                    if (selectedResourceType != null && rowResourceType.equalsIgnoreCase(selectedResourceType)) {
                        System.out.println("[DEBUG] ContextMenu shown: ResourceType matches ChoiceBox (" + selectedResourceType + ")");
                        fhirResourcesTable.getSelectionModel().select(row.getIndex());
                        patientContextMenu.show(row, event.getScreenX(), event.getScreenY());
                    } else {
                        System.out.println("[DEBUG] ContextMenu hidden: ResourceType does not match ChoiceBox (" + selectedResourceType + ")");
                        patientContextMenu.hide();
                    }
                } else {
                    System.out.println("[DEBUG] ContextMenu hidden: row is empty");
                    patientContextMenu.hide();
                }
            });


            // Nascondi il ContextMenu quando si clicca su una riga vuota
            row.setOnMouseClicked(event -> {
                if (event.isSecondaryButtonDown() && row.isEmpty()) {
                    patientContextMenu.hide();
                }
            });

            return row;
        });
    }


    @FXML
    private void onViewCda() {
        Map<String, String> selectedResource = fhirResourcesTable.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            statusLabel.setText("Please select a Patient resource!");
            return;
        }

        String patientName = selectedResource.get("Name");
        if (patientName == null || patientName.isEmpty()) {
            statusLabel.setText("Selected resource does not have a valid patient name!");
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Fetching CDA documents for patient: " + patientName);

        // Notifica l'evento di ricerca dei documenti CDA
        EventManager.getInstance().getEventObservable().notify("fetch_cda_by_patient", new File(patientName));

        // Naviga alla schermata CDAListScreen
        System.out.println("Navigating to CDA List screen...");
        Stage currentStage = (Stage) backButton.getScene().getWindow(); // Recupera lo Stage dalla scena corrente
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreenWithData("/com/group01/dhsa/View/CDAListScreen.fxml", currentStage, "CDA Files", Map.of("patientName", patientName));
    }


    @FXML
    private void onViewDicom() {
        Map<String, String> selectedResource = fhirResourcesTable.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            statusLabel.setText("Please select a Patient resource!");
            return;
        }

        String patientName = selectedResource.get("Name");
        if (patientName == null || patientName.isEmpty()) {
            statusLabel.setText("Selected resource does not have a valid patient name!");
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Fetching DICOM files for patient: " + patientName);

        // Naviga alla schermata DicomListScreen passando il nome del paziente
        System.out.println("Navigating to DICOM List screen...");
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreenWithData(
                "/com/group01/dhsa/View/DicomListScreen.fxml",
                currentStage,
                "DICOM Files",
                Map.of("patientName", patientName) // Passa solo il nome del paziente
        );
        progressBar.setVisible(false);
        statusLabel.setText("");

    }




    @FXML
    private void onLoadResources() {
        String selectedResourceType = resourceTypeChoiceBox.getValue();
        if (selectedResourceType == null || selectedResourceType.isEmpty()) {
            statusLabel.setText("Please select a resource type to load!");
            return;
        }

        // Rendi visibile la progress bar
        progressBar.setVisible(true);
        statusLabel.setText("Loading resources...");

        // Usa un Task per eseguire il caricamento in un thread separato
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simula un ritardo (rimuovi questa parte nel programma reale)
                Thread.sleep(1000);

                // Notifica l'evento di caricamento
                EventManager.getInstance().getEventObservable().notify("load_request", new File(selectedResourceType));

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // Nascondi la progress bar al termine
                progressBar.setVisible(false);
                statusLabel.setText("Resources loaded successfully!");
            }

            @Override
            protected void failed() {
                super.failed();
                // Nascondi la progress bar e mostra l'errore
                progressBar.setVisible(false);
                statusLabel.setText("Failed to load resources!");
            }
        };

        // Esegui il task in un thread separato
        new Thread(loadTask).start();
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
        System.out.println("[DEBUG] Updating table with resources: " + resources); // Debug

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
        System.out.println("[DEBUG] Table updated successfully."); // Debug
    }



}
