package com.group01.dhsa.Controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.EventManager;
import com.group01.dhsa.CdaDocumentCreator;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.ObserverPattern.EventObservable;
import jakarta.xml.bind.JAXBException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
    private ProgressIndicator progressCDA;

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
    private CdaDocumentCreator cda;
    private File cdaFile;

    public DischargePanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
        this.cda = new CdaDocumentCreator();
    }

    @FXML
    private void initialize() {
        firstNameField.setOnKeyPressed(this::handleEnterPressed);
        lastNameField.setOnKeyPressed(this::handleEnterPressed);
    }

    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            searchButton.fire();
        }
    }


    @FXML
    void backToHome() {
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
        stackPaneDischarge.setVisible(false);
        stackPaneCDA.setVisible(true);


        try {
            previewButton.setDisable(false);
            this.cdaFile = cda.createCdaDocument(patientIDMenu.getText(),encounterIDMenu.getText());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
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
        }
        else {
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

        System.out.println("Found " + encounterList.size() );

        if (!encounterList.isEmpty()) {
            int i = 1;

            for (Encounter encounter : encounterList) {
                if(checkEncounter(encounter)) {
                    MenuItem item = new MenuItem(encounter.getIdentifier().getFirst().getValue());
                    item.setId("item"+i);
                    i = i + 1;
                    item.setOnAction(this::switchSelectedEncounter);
                    encounterIDMenu.getItems().add(item);
                }
            }
            encounterIDMenu.setDisable(false);
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

        String FHIR_SERVER_URL = "http://localhost:8080/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

        Bundle response = client.search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value(surname))
                .and(Patient.NAME.matches().values(name))
                .returnBundle(Bundle.class)
                .execute();

        if (response.getEntry().isEmpty()) {
            errorLabel.setText("Patient not found");
        } else {
            errorLabel.setText("");

            int i = 1;
            for (Bundle.BundleEntryComponent entry : response.getEntry()) {
                Patient patient = (Patient) entry.getResource();

                if (!patient.getDeceased().isEmpty()) {
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
        return patientId.equals(pId) &&
                    !encounter.getType().getFirst().getCodingFirstRep()
                            .getDisplay().equals("Death Certification");
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