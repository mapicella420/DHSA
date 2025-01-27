package com.group01.dhsa.Controller;


import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

            // Gestione del menu contestuale
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    Map<String, String> item = row.getItem();
                    System.out.println("[DEBUG] Row Context Menu - Item: " + item);

                    dynamicContextMenu.getItems().clear();
                    String resourceType = resourceTypeChoiceBox.getValue();

                    if ("Patient".equalsIgnoreCase(resourceType)) {
                        dynamicContextMenu.getItems().addAll(viewCdaMenuItem, viewDicomMenuItem);
                        populateDynamicContextMenu(dynamicContextMenu, resourceType, item);

                    } else {
                        populateDynamicContextMenu(dynamicContextMenu, resourceType, item);
                    }

                    dynamicContextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    System.out.println("[DEBUG] Row Context Menu - Empty row selected.");
                }
            });

            // Gestione del clic sulla riga
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton().name().equalsIgnoreCase("PRIMARY")) {
                    Map<String, String> item = row.getItem();
                    System.out.println("[DEBUG] Row Clicked - Item: " + item);

                    // Mostra il menu contestuale per il clic sinistro
                    dynamicContextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    System.out.println("[DEBUG] Empty row clicked.");
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

    /**
     * Metodo per aggiornare la tabella con i dati ricevuti dal modello.
     */
    private void updateTable(List<Map<String, String>> resources) {
        Platform.runLater(() -> {
            fhirResourcesTable.getColumns().clear();
            statusLabel.setVisible(false); // Nascondi la label durante l'aggiornamento

            if (resources == null || resources.isEmpty()) {
                statusLabel.setText("No resources found.");
                statusLabel.setText("No resources available to display.");
                statusLabel.setVisible(true); // Mostra la label se non ci sono risorse
                return;
            }

            Map<String, String> firstRow = resources.get(0);
            for (String key : firstRow.keySet()) {
                TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
                column.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().get(key))
                );
                fhirResourcesTable.getColumns().add(column);
            }

            fhirResourcesTable.getItems().setAll(resources);
            statusLabel.setText("Table updated with linked resource data.");

            // Configura nuovamente il menu contestuale per ogni riga
            fhirResourcesTable.setRowFactory(tv -> {
                TableRow<Map<String, String>> row = new TableRow<>();
                ContextMenu dynamicContextMenu = new ContextMenu();

                row.setOnContextMenuRequested(event -> {
                    if (!row.isEmpty()) {
                        Map<String, String> item = row.getItem();
                        dynamicContextMenu.getItems().clear();
                        String resourceType = resourceTypeChoiceBox.getValue();

                        if ("Patient".equalsIgnoreCase(resourceType)) {
                            dynamicContextMenu.getItems().addAll(viewCdaMenuItem, viewDicomMenuItem);
                            populateDynamicContextMenu(dynamicContextMenu, resourceType, item);
                        } else {
                            populateDynamicContextMenu(dynamicContextMenu, resourceType, item);
                        }
                        dynamicContextMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                });

                return row;
            });
        });
    }


    /**
     * Popola il menu contestuale dinamico in base al tipo di risorsa.
     */
    private void populateDynamicContextMenu(ContextMenu contextMenu, String resourceType, Map<String, String> item) {
        // Estrai EncounterId, PatientId, OrganizationId e PractitionerId dalla riga selezionata
        String encounterId = cleanReference(item.get("Encounter"));
        String patientId = cleanReference(item.get("Patient"));
        String organizationId = cleanReference(item.get("Organization"));
        String practitionerId = cleanReference(item.get("Practitioner"));

        // Gestione delle risorse specifiche
        if ("Practitioner".equalsIgnoreCase(resourceType) || "Organization".equalsIgnoreCase(resourceType)) {
            addDynamicMenuItem(contextMenu, resourceType, resourceType.equals("Practitioner") ? practitionerId : organizationId, null);
            return;
        }

        // Controlla i casi in cui EncounterId o PatientId non sono disponibili
        boolean hasEncounterId = encounterId != null && !encounterId.isEmpty();
        boolean hasPatientId = patientId != null && !patientId.isEmpty();

        if (!hasEncounterId && !hasPatientId) {
            System.err.println("[ERROR] Both Encounter ID and Patient ID are missing or empty.");
            return;
        }

        // Per le altre risorse, aggiungi voci dinamiche
        if ("Patient".equalsIgnoreCase(resourceType)) {
            contextMenu.getItems().add(viewCdaMenuItem);
            contextMenu.getItems().add(viewDicomMenuItem);
            addLinkedResourcesToContextMenu(contextMenu, encounterId, patientId, organizationId, practitionerId);
            return;
        }

        if ("Encounter".equalsIgnoreCase(resourceType)) {
            addLinkedResourcesToContextMenu(contextMenu, encounterId, patientId, organizationId, practitionerId);
            return;
        }

        // Aggiungi voce per il tipo di risorsa corrente
        addDynamicMenuItem(contextMenu, resourceType, encounterId, patientId);

        // Aggiungi voci per tutte le risorse collegate
        addLinkedResourcesToContextMenu(contextMenu, encounterId, patientId, organizationId, practitionerId);
    }

    /**
     * Aggiunge le risorse collegate dinamicamente al menu contestuale.
     */
    private void addLinkedResourcesToContextMenu(ContextMenu contextMenu, String encounterId, String patientId,
                                                 String organizationId, String practitionerId) {
        List<String> linkedResources = List.of(
                "AllergyIntolerance", "CarePlan", "Condition", "Device", "Encounter",
                "ImagingStudy", "Immunization", "MedicationRequest", "Observation",
                "Organization", "Patient", "Procedure"
        );

        // Aggiungi risorse collegate standard
        for (String linkedResource : linkedResources) {
            addDynamicMenuItem(contextMenu, linkedResource, encounterId, patientId);
        }

        // Aggiungi riferimenti specifici per Organization e Practitioner
        if (organizationId != null && !organizationId.isEmpty()) {
            addDynamicMenuItem(contextMenu, "Organization", organizationId, null);
        }

        if (practitionerId != null && !practitionerId.isEmpty()) {
            addDynamicMenuItem(contextMenu, "Practitioner", practitionerId, null);
        }
    }

    /**
     * Aggiunge un elemento al menu contestuale dinamico.
     */
    private void addDynamicMenuItem(ContextMenu contextMenu, String label, String resourceId, String patientId) {
        MenuItem menuItem = new MenuItem(label);

        menuItem.setOnAction(event -> {
            // Attiva la barra di progressione
            progressBar.setVisible(true);

            // Pulizia del resourceId per evitare duplicazioni
            String cleanResourceId = resourceId != null ? resourceId.replace(label + "/", "").trim() : "N/A";
            String cleanPatientId = patientId != null ? patientId.replace("Patient/", "").trim() : "N/A";

            // Mostra i dettagli del contesto selezionato
            System.out.println("[DEBUG] Menu selected - ResourceType: " + label +
                    ", ResourceId: " + cleanResourceId +
                    ", PatientId: " + cleanPatientId);

            // Crea la stringa di dettagli della risorsa
            StringBuilder resourceDetails = new StringBuilder(label + "_Combined");
            if (!"N/A".equals(cleanResourceId)) {
                resourceDetails.append("/").append(label).append("/").append(cleanResourceId);
            }
            if (!"N/A".equals(cleanPatientId)) {
                resourceDetails.append("/Patient/").append(cleanPatientId);
            }

            System.out.println("[DEBUG] Resource details constructed: " + resourceDetails);

            // Usa un Task per gestire l'operazione asincrona
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    EventManager.getInstance().getEventObservable().notify(
                            "linked_resource_selected",
                            new File(resourceDetails.toString())
                    );
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        if (EventManager.getInstance().getCurrentResources().isEmpty()) {
                            statusLabel.setText("No linked resources found for " + label);
                            statusLabel.setVisible(true);
                        } else {
                            statusLabel.setVisible(false);
                        }
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    Platform.runLater(() -> {
                        progressBar.setVisible(false);
                        statusLabel.setText("An error occurred during the operation.");
                        statusLabel.setVisible(true);
                    });
                }
            };

            // Esegui il Task in un thread separato
            new Thread(task).start();
        });

        contextMenu.getItems().add(menuItem);
    }

    /**
     * Rimuove prefissi e simboli inutili dal riferimento.
     */
    private String cleanReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            return reference;
        }
        if (reference.contains("Reference[")) {
            reference = reference.replace("Reference[", "").replace("]", "").trim();
        }
        return reference;
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
