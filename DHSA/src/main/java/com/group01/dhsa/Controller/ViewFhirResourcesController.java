package com.group01.dhsa.Controller;


import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.application.Platform;
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
            Platform.runLater(() -> {
                updateTable(EventManager.getInstance().getCurrentResources());
                progressBar.setVisible(false);
                statusLabel.setText("Resources loaded successfully!");
            });
        });

        this.eventManager.subscribe("search_complete", (eventType, file) -> {
            Platform.runLater(() -> {
                updateTable(EventManager.getInstance().getCurrentResources());
                progressBar.setVisible(false);
                statusLabel.setText("Search completed successfully!");
            });
        });

        this.eventManager.subscribe("cda_search_complete", (eventType, file) -> {
            Platform.runLater(() -> {
                try {
                    System.out.println("[DEBUG] CDA search complete event triggered.");
                    updateTable(EventManager.getInstance().getCurrentResources());
                } catch (Exception e) {
                    System.err.println("Error in CDA_search_complete listener: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

        this.eventManager.subscribe("dicom_search_complete", (eventType, file) -> {
            Platform.runLater(() -> {
                try {
                    System.out.println("[DEBUG] DICOM search complete event triggered.");
                    updateTable(EventManager.getInstance().getCurrentResources());
                } catch (Exception e) {
                    System.err.println("Error in dicom_search_complete listener: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

        this.eventManager.subscribe("error", (eventType, file) -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                statusLabel.setText("An error occurred: " + (file != null ? file.getName() : "Unknown error"));
            });
        });
    }

    @FXML
    private void initialize() {
        // Popolare il ChoiceBox con i tipi di risorse
        resourceTypeChoiceBox.getItems().addAll(
                "Patient", "Device", "Organization", "Encounter", "Allerg", "Provider",
                "CarePlan", "Condition", "Procedure", "ImagingStudy",
                "Observation", "Immunization", "MedicationRequest"
        );

        progressBar.setVisible(false);
        statusLabel.setText("...");

        // Configura il comportamento dinamico per ogni riga della tabella
        fhirResourcesTable.setRowFactory(tv -> {
            TableRow<Map<String, String>> row = new TableRow<>();
            ContextMenu dynamicContextMenu = new ContextMenu();

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    Map<String, String> item = row.getItem();
                    dynamicContextMenu.getItems().clear();

                    String resourceType = resourceTypeChoiceBox.getValue();
                    if ("Patient".equalsIgnoreCase(resourceType)) {
                        // Mostra il menu contestuale statico
                        dynamicContextMenu.getItems().addAll(viewCdaMenuItem, viewDicomMenuItem);
                    } else {
                        // Popola dinamicamente il menu contestuale
                        populateDynamicContextMenu(dynamicContextMenu, resourceType, item);
                    }

                    dynamicContextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton().name().equalsIgnoreCase("PRIMARY")) {
                    // Mostra il menu contestuale alla posizione del mouse anche per il clic sinistro
                    dynamicContextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            return row;
        });

        // Configura il menu contestuale per il tipo "Patient"
        patientContextMenu = new ContextMenu();
        viewCdaMenuItem = new MenuItem("View CDA");
        viewDicomMenuItem = new MenuItem("View DICOM");

        viewCdaMenuItem.setOnAction(event -> onViewCda());
        viewDicomMenuItem.setOnAction(event -> onViewDicom());

        patientContextMenu.getItems().addAll(viewCdaMenuItem, viewDicomMenuItem);
    }

    private String cleanReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            return reference;
        }
        // Rimuovi "Reference[" e "]"
        if (reference.contains("Reference[")) {
            reference = reference.replace("Reference[", "").replace("]", "").trim();
        }
        return reference;
    }


    /**
     * Metodo per aggiornare la tabella con i dati ricevuti dal modello.
     */
    private void updateTable(List<Map<String, String>> resources) {
        Platform.runLater(() -> {
            fhirResourcesTable.getColumns().clear();

            if (resources == null || resources.isEmpty()) {
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
            statusLabel.setText("Table updated with linked resource data.");
        });
    }

    /**
     * Popola il menu contestuale dinamico in base al tipo di risorsa
     */
    private void populateDynamicContextMenu(ContextMenu contextMenu, String resourceType, Map<String, String> item) {
        switch (resourceType) {
            case "Device":
                // Device è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("PATIENT"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("ENCOUNTER"));
                break;
            case "Encounter":
                // Encounter è associato a Organization e Patient
                addDynamicMenuItem(contextMenu, "Organization", item.get("Organization"));
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Practitioner", item.get("Practitioner"));
                break;
            case "Provider":
                // Encounter è associato a Organization e Patient
                addDynamicMenuItem(contextMenu, "Organization", item.get("Organization"));
                break;
            case "Allerg":
                // Allergy è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "CarePlan":
                // CarePlan è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "Condition":
                // Condition è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "Procedure":
                // Procedure è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "ImagingStudy":
                // ImagingStudy è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "Observation":
                // Observation è associato a Patient (Subject) e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Subject"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "Immunization":
                // Immunization è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "MedicationRequest":
                // MedicationRequest è associato a Patient e Encounter
                addDynamicMenuItem(contextMenu, "Patient", item.get("Patient"));
                addDynamicMenuItem(contextMenu, "Encounter", item.get("Encounter"));
                break;
            case "Organization":
                // Organization non ha associazioni dirette nei dati forniti
                statusLabel.setText("No linked resources available.");
                break;
            default:
                statusLabel.setText("No linked resources available.");
                break;
        }
    }


    /**
     * Aggiunge un elemento al menu contestuale dinamico
     */
    private void addDynamicMenuItem(ContextMenu contextMenu, String label, String reference) {
        if (reference != null && !reference.isEmpty()) {
            // Pulire il riferimento
            reference = cleanReference(reference);

            MenuItem menuItem = new MenuItem(label + ": " + reference);

            // Configurare l'azione al click dell'opzione nel menu a tendina
            String finalReference = reference;
            menuItem.setOnAction(event -> {
                String[] parts = finalReference.split("/"); // Dividi il riferimento in tipo e ID
                if (parts.length == 2) {
                    String resourceType = parts[0];
                    String resourceId = parts[1];

                    // Avvia la ricerca per tipo di risorsa e ID
                    System.out.println("[DEBUG] Menu selected - ResourceType: " + resourceType + ", ResourceId: " + resourceId);
                    EventManager.getInstance().getEventObservable().notify(
                            "linked_resource_selected",
                            new File(resourceType + "_" + resourceId) // Simula un file con tipo e ID
                    );
                } else {
                    System.err.println("[ERROR] Invalid reference format: " + finalReference);
                }
            });

            contextMenu.getItems().add(menuItem);
        }
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

        EventManager.getInstance().getEventObservable().notify("fetch_cda_by_patient", new File(patientName));
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

        EventManager.getInstance().getEventObservable().notify("fetch_dicom_by_patient", new File(patientName));
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
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Resources loaded successfully!");
                });
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Failed to load resources!");
                });
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
        statusLabel.setText("Searching for: " + selectedResourceType + " with term: " + searchTerm);

        // Notifica il modello per avviare la ricerca
        EventManager.getInstance().getEventObservable().notify(
                "search_request",
                new File(selectedResourceType + "_" + searchTerm) // File fittizio per includere il tipo e il termine
        );
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



}
