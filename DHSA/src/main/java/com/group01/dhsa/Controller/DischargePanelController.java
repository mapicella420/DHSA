package com.group01.dhsa.Controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Patient;

import java.util.EventObject;

public class DischargePanelController {

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

    private EventObservable eventManager;

    public DischargePanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
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
    }

    @FXML
    void searchPatient() {
        patientIDMenu.setDisable(true);
        patientIDMenu.setText("Patient ID");
        patientIDMenu.getItems().clear();

        String surname = lastNameField.getText();
        String name = firstNameField.getText();

        String FHIR_SERVER_URL = "http://localhost:8080/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

        // Perform a search by type
        Bundle response = client.search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value(surname))
                .and(Patient.NAME.matches().values(name))
                .returnBundle(Bundle.class)
                .execute();

        System.out.println("Found " + response.getEntry().size() );

        if (response.getEntry().isEmpty()){
            errorLabel.setText("Patient not found");
        } else {
            errorLabel.setText("");
        }

        int i = 1;
        for(Bundle.BundleEntryComponent c:response.getEntry()) {

            Patient p = (Patient) c.getResource();

            if (!p.hasDeceasedBooleanType()) {

                MenuItem item = new MenuItem(p.getIdentifier().get(0).getValue());
                item.setId("item"+i);
                i = i + 1;
                item.setOnAction(this::switchSelectedPatient);
                patientIDMenu.getItems().add(item);
            }
        }
        patientIDMenu.setDisable(false);
    }

    @FXML
    void downloadPDF(ActionEvent event) {

    }

}