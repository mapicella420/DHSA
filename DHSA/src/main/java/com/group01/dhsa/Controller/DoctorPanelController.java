package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
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
     *
     */
    public DoctorPanelController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    @FXML
    void logout() {
        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/LoginUserScreen.fxml",currentStage,"Login Screen");
    }

    /**
     * Azione per il bottone "Discharge Patient".
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

        // Notifica l'evento di dimissione
        eventManager.notify("patient_discharge", null);
    }

    /**
     * Azione per chiudere l'applicazione.
     */
    @FXML
    private void onCloseApp() {
        System.out.println("Closing application...");
        Stage stage = (Stage) dischargePatientButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Azione per il menu "Convert CSV to FHIR"
     */
    @FXML
    private void onConvertCsvClick() {
        System.out.println("Convert CSV to FHIR menu item clicked!");
        // Implementa la logica per la conversione CSV-FHIR
    }

    /**
     * Azione per caricare un file CSV.
     */
    @FXML
    private void onUploadCsvMenuClick() {
        if (eventManager == null) {
            System.err.println("EventManager is not set!");
            return;
        }

        File selectedFile = openFileChooser("Select CSV File", "CSV Files", "*.csv");
        if (selectedFile != null) {
            System.out.println("Selected CSV file: " + selectedFile.getAbsolutePath());
            eventManager.notify("csv_upload", selectedFile);
            System.out.println("Notify all listeners");

        } else {
            System.out.println("CSV file selection cancelled.");
        }
    }

    /**
     * Azione per caricare un file DICOM.
     */
    @FXML
    private void onImportDicomMenuClick() {
        if (eventManager == null) {
            System.err.println("EventManager is not set!");
            return;
        }

        File selectedFile = openFileChooser("Select DICOM File", "DICOM Files", "*.dcm");
        if (selectedFile != null) {
            System.out.println("Selected DICOM file: " + selectedFile.getAbsolutePath());
            eventManager.notify("dicom_upload", selectedFile);
        } else {
            System.out.println("DICOM file selection cancelled.");
        }
    }

    /**
     * Metodo di supporto per aprire un file chooser.
     *
     * @param title  Titolo della finestra di dialogo.
     * @param filterName Nome del filtro (es. "CSV Files").
     * @param filterPattern Pattern del filtro (es. "*.csv").
     * @return Il file selezionato, o null se la selezione è stata annullata.
     */
    private File openFileChooser(String title, String filterName, String filterPattern) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, filterPattern));

        Stage stage = new Stage();
        return fileChooser.showOpenDialog(stage);
    }
}
