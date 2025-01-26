package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.LoggedUser;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
                "AllergyIntolerance",  "Condition" ,"Observation", "Encounter",
                "Procedure", "CarePlan", "ImagingStudy", "Immunization", "Medications"
        );

        progressBar.setVisible(false);
        statusLabel.setText("Status: Ready");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> onSearch());

        // Listener per il cambio di risorsa
        resourceTypeChoiceBox.setOnAction(event -> onResourceTypeChange());
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
            System.out.println("[DEBUG] Updating table with resources: " + resources); // Debug

            clinicalDataTable.getColumns().clear();

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
                clinicalDataTable.getColumns().add(column);
            }

            // Usa FilteredList per il filtraggio
            ObservableList<Map<String, String>> observableData = FXCollections.observableArrayList(resources);
            filteredData = new FilteredList<>(observableData, p -> true);

            clinicalDataTable.setItems(filteredData);

            System.out.println("[DEBUG] Table updated successfully."); // Debug
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
        Object controller = screenChanger.switchScreenWithDataModal(
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

        Object controller = screenChanger.switchScreenWithDataModal(
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
