package com.group01.dhsa;

import com.group01.dhsa.Model.*;
import com.group01.dhsa.ObserverPattern.EventObservable;

/**
 * The EventManager class is a Singleton responsible for managing events in the system.
 * It initializes an {@link EventObservable} instance and registers listeners for various events.
 */
public class EventManager {

    private static EventManager instance; // Singleton instance
    private final EventObservable eventObservable;

    /**
     * Private constructor to ensure only one instance of EventManager is created.
     */
    private EventManager() {
        this.eventObservable = new EventObservable();
        initializeListeners(); // Initialize and register event listeners
    }

    /**
     * Retrieves the Singleton instance of the EventManager.
     *
     * @return The Singleton instance of the EventManager.
     */
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    /**
     * Provides access to the {@link EventObservable} instance managed by the EventManager.
     *
     * @return The EventObservable instance.
     */
    public EventObservable getEventObservable() {
        return eventObservable;
    }

    /**
     * Initializes and registers listeners for various events.
     */
    private void initializeListeners() {
        // Register listener for CSV uploads
        CsvImporter csvImporter = new CsvImporter();
        eventObservable.subscribe("csv_upload", csvImporter);

        // Register listener for DICOM uploads
        DicomImporter dicomImporter = new DicomImporter();
        eventObservable.subscribe("dicom_upload", dicomImporter);

        // Register listener for CDA uploads
        CdaUploader cdaUploader = new CdaUploader();
        eventObservable.subscribe("cda_upload", cdaUploader);

        // Register a listener for generating CDA documents
        CdaDocumentCreator cdaCreator = new CdaDocumentCreator(this.eventObservable);
        eventObservable.subscribe("generate_cda", (eventType, file) -> {
            // Extract patient and encounter IDs from the file name
            String[] params = file.getName().replace(".xml", "").split("_");
            cdaCreator.createAndNotify(params[0], params[1]);
        });

        // Register a listener for converting CDA files to HTML
        eventObservable.subscribe("convert_to_html", (eventType, file) -> {
            System.out.println("[DEBUG] Received convert_to_html event for file: " + file.getAbsolutePath());
            new CdaToHtmlConverter().convertAndNotify(file);
        });


        FhirExporter fhirExporter = new FhirExporter();
        eventObservable.subscribe("export_request", fhirExporter);

    }
}
