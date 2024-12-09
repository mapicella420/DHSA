package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DischargePanelController {

    @FXML
    private Button backButton;

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

    private EventObservable eventManager;

    public DischargePanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    @FXML
    void backToHome(ActionEvent event) {
        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml",currentStage,"Doctor Dashboard");
    }

    @FXML
    void dischargeSelectedPatient(ActionEvent event) {

    }

    @FXML
    void onCloseApp(ActionEvent event) {
        System.out.println("Closing application...");
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void searchPatient(ActionEvent event) {

    }

    @FXML
    void switchSelectedPatient(ActionEvent event) {

    }
}