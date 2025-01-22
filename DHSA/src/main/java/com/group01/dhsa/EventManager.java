package com.group01.dhsa;

import com.group01.dhsa.Model.*;
import com.group01.dhsa.ObserverPattern.EventObservable;

public class EventManager {

    private static EventManager instance;
    private final EventObservable eventObservable;

    /**
     * Costruttore privato per il Singleton
     */
    private EventManager() {
        this.eventObservable = new EventObservable();

        // Inizializza e registra i listener
        initializeListeners();
    }

    /**
     *  Metodo per ottenere l'unica istanza Singleton
     * @return instance
     */
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    // Metodo per ottenere l'EventObservable
    public EventObservable getEventObservable() {
        return eventObservable;
    }

    // Inizializza i listener
    private void initializeListeners() {
        // Registra il CsvImporter come listener
        CsvImporter csvImporter = new CsvImporter();
        eventObservable.subscribe("csv_upload", csvImporter);

        DicomImporter dicomImporter = new DicomImporter();
        eventObservable.subscribe("dicom_upload", dicomImporter);

        CdaUploader cdaUploader = new CdaUploader();
        eventObservable.subscribe("cda_upload", cdaUploader);

        CdaDocumentCreator cdaCreator = new CdaDocumentCreator(this.eventObservable);

        eventObservable.subscribe("generate_cda", (eventType, file) -> {
            String[] params = file.getName().replace(".xml", "").split("_");
            cdaCreator.createAndNotify(params[0], params[1]);
        });

        eventObservable.subscribe("convert_to_html", (eventType, file) -> {
            System.out.println("[DEBUG] Received convert_to_html event for file: " + file.getAbsolutePath());
            new CdaToHtmlConverter().convertAndNotify(file);
        });


    }
}
