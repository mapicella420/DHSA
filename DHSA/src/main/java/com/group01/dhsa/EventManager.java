package com.group01.dhsa;

import com.group01.dhsa.Model.*;
import com.group01.dhsa.Model.FhirResources.FhirExporterFactoryManager;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;
import com.group01.dhsa.ObserverPattern.EventObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The EventManager class is a Singleton responsible for managing events in the system.
 * It initializes an {@link EventObservable} instance and registers listeners for various events.
 */
public class EventManager {

    private static EventManager instance; // Singleton instance
    private final EventObservable eventObservable;
    private List<Map<String, String>> currentResources; // Stores the current resources

    /**
     * Private constructor to ensure only one instance of EventManager is created.
     */
    private EventManager() {
        this.eventObservable = new EventObservable();
        this.currentResources = new ArrayList<>(); // Initialize empty resource list
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
     * Returns the current resources stored in EventManager.
     *
     * @return A list of maps representing the resources.
     */
    public List<Map<String, String>> getCurrentResources() {
        return currentResources;
    }

    /**
     * Updates the current resources stored in EventManager.
     *
     * @param resources The list of resources to store.
     */
    public void setCurrentResources(List<Map<String, String>> resources) {
        this.currentResources = resources;
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

        // Register FHIR resource handling listeners
        eventObservable.subscribe("load_request", (eventType, file) -> {
            String resourceType = file.getName();
            loadResources(resourceType);
        });

        eventObservable.subscribe("search_request", (eventType, file) -> {
            String[] params = file.getName().split("_");
            String resourceType = params[0];
            String searchTerm = params[1];
            searchResources(resourceType, searchTerm);
        });
    }

    private void loadResources(String resourceType) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> resources = exporter.exportResources();
            setCurrentResources(resources);

            // Notify that resources have been loaded
            eventObservable.notify("load_complete", null);

        } catch (Exception e) {
            System.err.println("Error loading resources for type: " + resourceType);
            eventObservable.notify("error", null);
        }
    }

    private void searchResources(String resourceType, String searchTerm) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> results = exporter.searchResources(searchTerm);
            setCurrentResources(results);

            // Notify that search has been completed
            eventObservable.notify("search_complete", null);

        } catch (Exception e) {
            System.err.println("Error searching resources for type: " + resourceType + " with term: " + searchTerm);
            eventObservable.notify("error", null);
        }
    }
}
