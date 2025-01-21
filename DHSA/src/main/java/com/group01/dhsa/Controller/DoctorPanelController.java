package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class DoctorPanelController {

    @FXML
    private Button dischargePatientButton;

    @FXML
    private MenuItem logout;

    private EventObservable eventManager;

    /**
     * Constructor
     */
    public DoctorPanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    @FXML
    void logout() {
        LoggedUser userLog = LoggedUser.getInstance();
        userLog.logout();

        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/LoginUserScreen.fxml",currentStage,"Login Screen");
    }

    /**
     * Action for "Discharge Patient" button.
     */
    @FXML
    private void onDischargePatientClick() {

        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DischargePanelScreen.fxml",currentStage,"Discharge Patient");

        //System.out.println("Discharge Patient button clicked!");
        // Logica per dimettere il paziente
        if (eventManager == null) {
            System.err.println("EventManager is not set!");
            return;
        }
        eventManager.notify("patient_discharge", null);
    }

    /**
     * Action to close the application.
     */
    @FXML
    private void onCloseApp() {
        System.out.println("Closing application...");
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Action for the "Convert CSV to FHIR" menu.
     */
    @FXML
    private void onConvertCsvClick() {
        System.out.println("Convert CSV to FHIR menu item clicked!");
    }

    /**
     * Action to upload a CSV file and switch to the Upload CSV screen.
     */
    @FXML
    private void onUploadCsvMenuClick() {
        System.out.println("Navigating to Upload CSV screen...");
        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow(); // Recupera lo Stage dalla scena corrente
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/UploadCSVScreen.fxml", currentStage, "Upload CSV");
    }


    /**
     * Action to upload a DICOM file.
     */
    @FXML
    private void onImportDicomMenuClick() {
        System.out.println("Navigating to Upload CSV screen...");
        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow(); // Recupera lo Stage dalla scena corrente
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DicomImportScreen.fxml", currentStage, "Upload CSV");

    }

    /**
     * Helper method to open a file chooser.
     *
     * @param title        The dialog title.
     * @param filterName   The name of the file filter.
     * @param filterPattern The file extension pattern.
     * @return The selected file, or null if canceled.
     */
    private File openFileChooser(String title, String filterName, String filterPattern) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, filterPattern));

        Stage stage = new Stage();
        return fileChooser.showOpenDialog(stage);
    }

    public void onViewCsvMenuClick(ActionEvent actionEvent) {
    }

    public void onsearchDicomMenuClick(ActionEvent actionEvent) {
        System.out.println("Navigating to DICOM List screen...");
        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow(); // Recupera lo Stage dalla scena corrente
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DicomListScreen.fxml", currentStage, "DICOM Files");
    }

}
