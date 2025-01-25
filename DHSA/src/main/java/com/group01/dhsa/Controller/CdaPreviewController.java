package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventObservable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CdaPreviewController {

    public MenuItem upload;
    public MenuItem export;
    public Button backButton;
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

    @FXML
    private void handleExportXmlButton(ActionEvent actionEvent) {
        if (cdaFile != null && cdaFile.exists()) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save CDA as XML");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

                File saveFile = fileChooser.showSaveDialog(backButton.getScene().getWindow());

                if (saveFile != null) {
                    try (FileWriter writer = new FileWriter(saveFile)) {
                        writer.write(new String(java.nio.file.Files.readAllBytes(cdaFile.toPath())));
                        System.out.println("[DEBUG] File exported to: " + saveFile.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                System.err.println("[ERROR] Error while exporting XML: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("[ERROR] File not found");
        }
    }

    @FXML
    public void handleImportXmlButton(ActionEvent actionEvent) {
        // Apri un file chooser per selezionare il file XML
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        // Mostra la finestra di dialogo per scegliere il file
        File selectedFile = fileChooser.showOpenDialog(backButton.getScene().getWindow());

        if (selectedFile != null) {
            System.out.println("[DEBUG] Selected XML file: " + selectedFile.getAbsolutePath());

            // Salva il file caricato come riferimento nel controller
            this.cdaFile = selectedFile;

            // Notifica all'EventManager per avviare la conversione in HTML
            System.out.println("[DEBUG] Sending convert_to_html notification for file: " + cdaFile.getAbsolutePath());
            EventManager.getInstance().getEventObservable().notify("convert_to_html", cdaFile);

        } else {
            System.out.println("[DEBUG] No file selected.");
        }
    }


}
