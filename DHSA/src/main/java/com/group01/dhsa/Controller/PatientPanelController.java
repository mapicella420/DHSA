package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Controller per la schermata "Patient Panel".
 */
public class PatientPanelController {

    public TextField searchField;
    @FXML
    private ChoiceBox<String> resourceTypeChoiceBox;

    @FXML
    private TableView<Map<String, String>> clinicalDataTable;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar progressBar;

    private final EventObservable eventManager;

    private boolean isLoading = false; // Flag per prevenire notifiche multiple

    public PatientPanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
        initializeListeners();
    }

    @FXML
    private void initialize() {
        // Popola il menu a tendina con i tipi di risorse
        resourceTypeChoiceBox.getItems().addAll(
                "AllergyIntolerance", "Condition", "Observation", "Encounter",
                "Procedure", "CarePlan", "ImagingStudy", "Immunization", "Medications"
        );

        progressBar.setVisible(false);
        statusLabel.setText("Status: Ready");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearch());

        // Listener per il cambio di risorsa
        resourceTypeChoiceBox.setOnAction(event -> onResourceTypeChange());

        // Configura il menu contestuale per ogni riga della tabella
        configureTableRowFactory();
    }

    @FXML
    private void configureTableRowFactory() {
        clinicalDataTable.setRowFactory(tv -> {
            TableRow<Map<String, String>> row = new TableRow<>();
            ContextMenu dynamicContextMenu = new ContextMenu();

            // Debug: Mostra che la factory viene configurata
            System.out.println("[DEBUG] Configurazione RowFactory applicata alla tabella.");

            // Configura il menu contestuale quando una riga è selezionata
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    // Ottieni l'elemento della riga selezionata
                    Map<String, String> item = row.getItem();
                    System.out.println("[DEBUG] Context menu richiesto per elemento: " + item);

                    // Ricostruisci il ContextMenu
                    dynamicContextMenu.getItems().clear();
                    String resourceType = resourceTypeChoiceBox.getValue();
                    System.out.println("[DEBUG] Tipo di risorsa selezionato: " + resourceType);

                    if (resourceType != null && !resourceType.isEmpty()) {
                        populateDynamicContextMenu(dynamicContextMenu, resourceType, item);
                        if (!dynamicContextMenu.getItems().isEmpty()) {
                            // Mostra il ContextMenu alla posizione del clic
                            System.out.println("[DEBUG] Mostra ContextMenu con " + dynamicContextMenu.getItems().size() + " elementi.");
                            dynamicContextMenu.show(row, event.getScreenX(), event.getScreenY());
                        } else {
                            System.out.println("[DEBUG] Nessun elemento da mostrare nel ContextMenu.");
                        }
                    } else {
                        System.out.println("[DEBUG] Nessuna risorsa selezionata.");
                    }
                } else {
                    System.out.println("[DEBUG] Context menu richiesto per riga vuota.");
                }
            });

            // Debug: Per clic secondario
            row.setOnMouseClicked(event -> {
                if (event.isSecondaryButtonDown()) {
                    System.out.println("[DEBUG] Clic secondario su riga: " + row.getItem());
                    dynamicContextMenu.hide();
                }
            });

            return row;
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
            addDynamicMenuItem(contextMenu, resourceType, resourceType.equals("Organization") ? organizationId : practitionerId, null);
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



    /**
     * Inizializza i listener per ricevere gli aggiornamenti dal model tramite l'Observer.
     */
    private void initializeListeners() {
        // Listener per la notifica di completamento della ricerca
        eventManager.subscribe("search_complete", (eventType, file) -> {
            Platform.runLater(() -> {
                isLoading = false;
                List<Map<String, String>> resources = EventManager.getInstance().getCurrentResources();
                updateTable(resources);
                progressBar.setVisible(false);
                statusLabel.setText("Resources loaded successfully!");
            });
        });

        // Listener per errori
        eventManager.subscribe("error", (eventType, file) -> {
            Platform.runLater(() -> {
                isLoading = false;
                progressBar.setVisible(false);
                statusLabel.setText("Error: Unable to load resources.");
            });
        });
    }

    /**
     * Gestisce il cambio del tipo di risorsa selezionata dall'utente.
     */
    @FXML
    private void onResourceTypeChange() {
        String selectedResourceType = resourceTypeChoiceBox.getValue();

        if (selectedResourceType == null || selectedResourceType.isEmpty()) {
            statusLabel.setText("Please select a resource type.");
            return;
        }

        // Recupera l'identificativo dell'utente loggato
        String patientIdentifier = LoggedUser.getInstance().getFhirId();
        System.out.println(patientIdentifier);
        if (patientIdentifier == null || patientIdentifier.isEmpty()) {
            statusLabel.setText("Error: Logged user identifier is missing.");
            return;
        }

        // Mostra la progress bar
        progressBar.setVisible(true);
        statusLabel.setText("Loading resources for patient identifier: " + patientIdentifier);

        // Ottieni l'ID del paziente tramite il client FHIR
        String patientId = FHIRClient.getInstance().getPatientIdById(patientIdentifier);
        if (patientId == null) {
            statusLabel.setText("Error: Could not resolve patient ID.");
            progressBar.setVisible(false);
            return;
        }

        // Notifica al modello di eseguire la ricerca delle risorse
        EventManager.getInstance().searchResourcesForPatient(selectedResourceType, patientId);
    }



    /**
     * Aggiorna la tabella con i dati restituiti dal model.
     *
     * @param resources Lista di mappe contenenti i dati da mostrare nella tabella.
     */
    private FilteredList<Map<String, String>> filteredData;

    private void updateTable(List<Map<String, String>> resources) {
        Platform.runLater(() -> {
            System.out.println("[DEBUG] Aggiornamento tabella con risorse: " + resources);

            clinicalDataTable.getColumns().clear();

            if (resources.isEmpty()) {
                statusLabel.setText("No resources found.");
                System.out.println("[DEBUG] Nessuna risorsa trovata.");
                return;
            }

            Map<String, String> firstRow = resources.get(0);
            for (String key : firstRow.keySet()) {
                TableColumn<Map<String, String>, String> column = new TableColumn<>(key);
                column.setCellValueFactory(data ->
                        new javafx.beans.property.SimpleStringProperty(data.getValue().get(key))
                );
                clinicalDataTable.getColumns().add(column);
            }

            ObservableList<Map<String, String>> observableData = FXCollections.observableArrayList(resources);
            filteredData = new FilteredList<>(observableData, p -> true);
            clinicalDataTable.setItems(filteredData);

            System.out.println("[DEBUG] Configurazione del RowFactory dopo l'aggiornamento.");
            configureTableRowFactory();
        });
    }


    @FXML
    private void onCloseApp() {
        System.out.println("Closing application...");
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onLogout() {
        LoggedUser userLog = LoggedUser.getInstance();
        userLog.logout();

        Stage currentStage = (Stage) statusLabel.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/LoginUserScreen.fxml",currentStage,"Login Screen");
    }


    @FXML
    public void onViewClinicalData(ActionEvent actionEvent) {
        // Recupera l'identificativo del paziente loggato
        String patientIdentifier = LoggedUser.getInstance().getFhirId();
        if (patientIdentifier == null || patientIdentifier.isEmpty()) {
            statusLabel.setText("Error: Logged user identifier is missing.");
            return;
        }

        // Ottieni l'ID del paziente tramite il client FHIR
        String patientId = FHIRClient.getInstance().getPatientIdByIdentifier(patientIdentifier);
        if (patientId == null) {
            statusLabel.setText("Error: Could not resolve patient ID.");
            return;
        }

        // Recupera il nome del paziente (opzionale, ma utile per filtrare i CDA nella schermata successiva)
        String patientName = FHIRClient.getInstance().getPatientFromIdentifier(patientId).getNameFirstRep().getNameAsSingleString();
        if (patientName == null || patientName.isEmpty()) {
            statusLabel.setText("Error: Could not retrieve patient name.");
            return;
        }

        // Prepara i dati da passare alla nuova schermata
        Map<String, Object> data = Map.of("patientName", patientName);

        // Cambia schermata passando i dati al controller
        Stage currentStage = (Stage) statusLabel.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        Object controller = screenChanger.switchScreenWithData(
                "/com/group01/dhsa/View/PatientCdaScreen.fxml",
                currentStage,
                "CDA List",
                data
        );

        // Verifica che il controller sia un'istanza di CdaListController
        if (controller instanceof PatientCdaController) {
            ((PatientCdaController) controller).receiveData(data);
            System.out.println("[DEBUG] Patient name passed to PatientCdaController: " + patientName);
        } else {
            System.err.println("[ERROR] Controller is not an instance of PatientCdaController.");
        }

    }


    @FXML
    private void onRefresh() {
        clinicalDataTable.getItems().clear();
        progressBar.setVisible(false);
        statusLabel.setText("Table cleared and refreshed!");
    }

    public void onDownload(ActionEvent actionEvent) {
    }

    @FXML
    private void onSearch() {
        String searchText = searchField.getText().toLowerCase();

        if (filteredData != null) {
            filteredData.setPredicate(resource -> {
                if (searchText == null || searchText.isEmpty()) {
                    return true; // Mostra tutti i risultati se la ricerca è vuota
                }

                // Cerca in tutte le colonne
                return resource.values().stream()
                        .anyMatch(value -> value != null && value.toLowerCase().contains(searchText));
            });
        }
    }


    @FXML
    public void onViewDicomData(ActionEvent actionEvent) {
        // Recupera l'identificativo del paziente loggato
        String patientIdentifier = LoggedUser.getInstance().getFhirId();
        if (patientIdentifier == null || patientIdentifier.isEmpty()) {
            statusLabel.setText("Error: Logged user identifier is missing.");
            return;
        }

        // Ottieni l'ID del paziente tramite il client FHIR
        String patientId = FHIRClient.getInstance().getPatientIdByIdentifier(patientIdentifier);
        if (patientId == null) {
            statusLabel.setText("Error: Could not resolve patient ID.");
            return;
        }

        // Recupera il nome del paziente
        String patientName = FHIRClient.getInstance().getPatientFromIdentifier(patientId).getNameFirstRep().getNameAsSingleString();
        if (patientName == null || patientName.isEmpty()) {
            statusLabel.setText("Error: Could not retrieve patient name.");
            return;
        }

        // Rimuovi prefissi come "Mr." o "Mrs."
        String normalizedPatientName = patientName.replaceAll("^(Mr\\.\\s*|Mrs\\.\\s*)", "").trim();

        // Prepara i dati da passare alla nuova schermata
        Map<String, Object> data = Map.of("patientName", normalizedPatientName);

        // Cambia schermata passando i dati al controller
        Stage currentStage = (Stage) statusLabel.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();

        Object controller = screenChanger.switchScreenWithData(
                "/com/group01/dhsa/View/PatientDicomScreen.fxml",
                currentStage,
                "Dicom List",
                data
        );

        // Verifica che il controller sia un'istanza di PatientDicomController
        if (controller instanceof PatientDicomController) {
            ((PatientDicomController) controller).receiveData(data);
            System.out.println("[DEBUG] Normalized patient name passed to PatientDicomController: " + normalizedPatientName);
        } else {
            System.err.println("[ERROR] Controller is not an instance of PatientDicomController.");
        }
    }

}
