package com.group01.dhsa.Controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.LoggedUser;
import com.group01.dhsa.ObserverPattern.EventObservable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.bson.Document;
import org.hl7.fhir.r5.model.*;

import java.io.File;
import java.util.List;

public class DischargePanelController {

    public Button previewButton;
    public Button uploadButton;
    @FXML
    private Button backButton;

    @FXML
    private Button backButton2;

    @FXML
    private Label cdaStatus;

    @FXML
    private Button dischargePatientButton;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private MenuButton patientIDMenu;

    @FXML
    private Button searchButton;

    @FXML
    private StackPane stackPaneCDA;

    @FXML
    private StackPane stackPaneDischarge;

    @FXML
    private MenuButton encounterIDMenu;

    private EventObservable eventManager;
    private File cdaFile;
    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "cdaDocuments";

    public DischargePanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    public static void setMongoUri() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
            }
        }
    }

    @FXML
    private void initialize() {
        firstNameField.setOnKeyPressed(this::handleEnterPressed);
        lastNameField.setOnKeyPressed(this::handleEnterPressed);

        // Iscrizione agli eventi del modello
        eventManager.subscribe("cda_generated", this::onCdaGenerated);
        eventManager.subscribe("cda_generation_failed", this::onCdaGenerationFailed);
    }


    // Metodo per gestire la generazione completata della CDA
    private void onCdaGenerated(String eventType, File file) {
        if (file != null) {
            this.cdaFile = file;
            previewButton.setDisable(false);
            cdaStatus.setText("CDA generated successfully!");
        }
    }

    // Metodo per gestire il fallimento nella generazione della CDA
    private void onCdaGenerationFailed(String eventType, File file) {
        cdaStatus.setText("Failed to generate CDA. Please try again.");
    }


    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            searchButton.fire();
        }
    }


    @FXML
    void backToHome() {
        eventManager.unsubscribe("cda_generated", this::onCdaGenerated);
        eventManager.unsubscribe("cda_generation_failed", this::onCdaGenerationFailed);
        System.out.println("[INFO] Unsubscribed from all events.");
        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml",currentStage,"Doctor Dashboard");
    }

    @FXML
    void switchPanel() {
        stackPaneDischarge.setVisible(true);
        stackPaneCDA.setVisible(false);
    }

    @FXML
    void dischargeSelectedPatient() {
        // Disabilita i pulsanti e aggiorna lo stato dell'interfaccia
        stackPaneDischarge.setVisible(false);
        stackPaneCDA.setVisible(true);
        previewButton.setDisable(true);
        cdaStatus.setText("Generating CDA...");


        // Notifica al modello di generare la CDA
        EventManager.getInstance().getEventObservable().notify("generate_cda", new File(patientIDMenu.getText() + "_" + encounterIDMenu.getText() + ".xml"));
    }

    @FXML
    void onCloseApp() {
        System.out.println("Closing application...");
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        stage.close();
    }


    @FXML
    void switchSelectedPatient(ActionEvent event) {
        if (patientIDMenu.getText().equals("Patient ID")) {
            MenuItem caller = (MenuItem) event.getSource();
            patientIDMenu.setText(caller.getText());
            patientIDMenu.getItems().remove(caller);
        } else {
            MenuItem caller = (MenuItem) event.getSource();
            String oldCaller = patientIDMenu.getText();
            patientIDMenu.setText(caller.getText());
            caller.setText(oldCaller);
        }

        encounterIDMenu.setDisable(true);
        encounterIDMenu.setText("Encounter ID");
        encounterIDMenu.getItems().clear();

        List<Encounter> encounterList = FHIRClient.getInstance()
                .getEncountersForPatient(patientIDMenu.getText());

        if (!encounterList.isEmpty()) {
            int i = 1;
            for (Encounter encounter : encounterList) {
                if (checkEncounter(encounter)) {
                    MenuItem item = new MenuItem(encounter.getIdentifier().getFirst().getValue());
                    item.setId("item" + i);
                    i++;
                    item.setOnAction(this::switchSelectedEncounter);
                    encounterIDMenu.getItems().add(item);
                }
            }

            if (!encounterIDMenu.getItems().isEmpty()) {
                encounterIDMenu.setDisable(false);
            }
        }

        dischargePatientButton.setDisable(patientIDMenu.getText().equals("Patient ID") ||
                encounterIDMenu.getText().equals("Encounter ID"));
    }


    @FXML
    void switchSelectedEncounter(ActionEvent event) {
        if (encounterIDMenu.getText().equals("Encounter ID")) {
            MenuItem caller = (MenuItem) event.getSource();
            encounterIDMenu.setText(caller.getText());
            encounterIDMenu.getItems().remove(caller);
        }
        else {
            MenuItem caller = (MenuItem) event.getSource();
            String oldCaller = encounterIDMenu.getText();
            encounterIDMenu.setText(caller.getText());
            caller.setText(oldCaller);
        }
        dischargePatientButton.setDisable(patientIDMenu.getText().equals("Patient ID") ||
                encounterIDMenu.getText().equals("Encounter ID"));
    }

    @FXML
    void searchPatient() {

        patientIDMenu.setDisable(true);
        patientIDMenu.setText("Patient ID");
        patientIDMenu.getItems().clear();

        encounterIDMenu.setDisable(true);
        encounterIDMenu.setText("Encounter ID");
        encounterIDMenu.getItems().clear();

        String surname = lastNameField.getText();
        String name = firstNameField.getText();

        String FHIR_SERVER_URL = LoggedUser.getOrganization().equalsIgnoreCase("My Hospital") ?
                "http://localhost:8080/fhir" : "http://localhost:8081/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

        Bundle response = client.search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value(surname))
                .and(Patient.GIVEN.matches().values(name))
                .returnBundle(Bundle.class)
                .count(20)
                .execute();

        if (response.getEntry().isEmpty()) {
            errorLabel.setText("Patient not found");
        } else {
            errorLabel.setText("");

            int i = 1;
            for (Bundle.BundleEntryComponent entry : response.getEntry()) {
                Patient patient = (Patient) entry.getResource();

                if (patient.hasDeceasedBooleanType()) {
                    MenuItem item = new MenuItem(patient.getIdentifier().getFirst().getValue());
                    item.setId("item" + i);
                    i++;
                    item.setOnAction(this::switchSelectedPatient);
                    patientIDMenu.getItems().add(item);
                }
            }

            if (patientIDMenu.getItems().isEmpty()) {
                errorLabel.setText("No patients match the criteria");
            } else {
                patientIDMenu.setDisable(false);
            }

        }

    }

    @FXML
    void downloadPDF(ActionEvent event) {
        eventManager.unsubscribe("cda_generated", this::onCdaGenerated);
        eventManager.unsubscribe("cda_generation_failed", this::onCdaGenerationFailed);
        try {
            if (cdaFile != null && cdaFile.exists()) {
                // Ottieni la finestra corrente
                Stage currentStage = (Stage) previewButton.getScene().getWindow();

                // Cambia schermata usando ChangeScreen
                ChangeScreen screenChanger = new ChangeScreen();
                Object controller = screenChanger.switchScreen(
                        "/com/group01/dhsa/View/CdaPreviewScreen.fxml",
                        currentStage,
                        "Preview CDA Document"
                );

                // Imposta il file CDA nel controller della schermata di anteprima
                if (controller instanceof CdaPreviewController) {
                    ((CdaPreviewController) controller).setCdaFile(cdaFile);
                    System.out.println("[DEBUG] CDA file passed to CdaPreviewController.");
                } else {
                    System.err.println("[ERROR] The controller is not an instance of CdaPreviewController.");
                }
            } else {
                cdaStatus.setText("No CDA file available to preview.");
            }
        } catch (Exception e) {
            cdaStatus.setText("Error opening CDA preview: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean checkEncounter(Encounter encounter){

        String patientId = encounter.getSubject().getReference().split("/")[1];
        String pId = FHIRClient.getInstance().getPatientById(patientIDMenu.getText()).getIdPart();
        return checkCda(encounter.getIdentifierFirstRep().getValue()) && patientId.equals(pId) &&
                !encounter.getType().getFirst().getCodingFirstRep()
                        .getDisplay().equals("Death Certification");
    }

    private boolean checkCda(String encounterId){
        setMongoUri();
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            List<Document> documents = collection.find().into(new java.util.ArrayList<>());

            for (Document doc : documents) {
                System.out.println("[DEBUG] Loaded document: " + doc.toJson());
                String xmlContent = doc.getString("xmlContent");
                if (xmlContent != null) {
                    String[] cods = xmlContent.split("<encompassingEncounter>");
                    String encId = cods[1].split("extension=\"")[1]
                            .split("\" assigningAuthorityName")[0];
                    if (encounterId.equals(encId)) {
                        return false;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Error loading DICOM files: " + e.getMessage());
        }
        return true;
    }


    @FXML
    void uploadCda() {
        try {
            // Notifica all'EventManager
            EventManager.getInstance().getEventObservable().notify("cda_upload", this.cdaFile);

            // Aggiorna lo stato
            cdaStatus.setText("CDA uploaded successfully!");
        } catch (Exception e) {
            cdaStatus.setText("Error uploading CDA: " + e.getMessage());
            e.printStackTrace();
        }
    }

}