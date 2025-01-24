package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;

public class CdaPreviewController {

    public Button uploadButton;
    @FXML
    private WebView webView;

    private final EventObservable eventManager;
    private File cdaFile;

    public CdaPreviewController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    @FXML
    private void initialize() {
        // Iscriviti agli eventi di generazione HTML
        eventManager.subscribe("html_generated", this::onHtmlGenerated);
        eventManager.subscribe("html_generation_failed", this::onHtmlGenerationFailed);

    }

    public void setCdaFile(File cdaFile) {
        // Notifica al modello di avviare la conversione
        this.cdaFile = cdaFile;
        System.out.println("[DEBUG] Sending convert_to_html notification for file: " + cdaFile.getAbsolutePath());
        EventManager.getInstance().getEventObservable().notify("convert_to_html", cdaFile);
    }

    private void onHtmlGenerated(String eventType, File htmlFile) {
        if (htmlFile != null && htmlFile.exists()) {
            System.out.println("[DEBUG] Loading generated HTML file: " + htmlFile.toURI().toString());
            webView.getEngine().load(htmlFile.toURI().toString());
        } else {
            System.out.println("[ERROR] HTML file is null or does not exist.");
            webView.getEngine().loadContent("<html><body><h1>Error rendering HTML</h1></body></html>");
        }
    }

    private void onHtmlGenerationFailed(String eventType, File file) {
        System.out.println("[ERROR] HTML generation failed.");
        webView.getEngine().loadContent("<html><body><h1>HTML generation failed</h1></body></html>");
    }

    public void handleBackButton(ActionEvent actionEvent) {
        System.out.println("[DEBUG] Returning to the Discharge Patient screen.");
        Stage stage = (Stage) webView.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "DICOM Files");
    }

    @FXML
    private void handlePrintButton(ActionEvent event) {
        try {
            WebEngine engine = webView.getEngine();
            PrinterJob printerJob = PrinterJob.createPrinterJob();

            if (printerJob != null && printerJob.showPrintDialog(webView.getScene().getWindow())) {
                // Avvia la stampa
                engine.print(printerJob);

                // Completa il lavoro di stampa
                printerJob.endJob();
                System.out.println("[DEBUG] Document printed successfully.");
            } else {
                System.err.println("[ERROR] Printing canceled or failed.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error while printing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void uploadCda() {
        try {
            // Notifica all'EventManager
            EventManager.getInstance().getEventObservable().notify("cda_upload", this.cdaFile);

            // Aggiorna lo stato
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
