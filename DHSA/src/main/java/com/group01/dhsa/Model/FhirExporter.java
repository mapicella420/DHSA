package com.group01.dhsa.Model;

import com.group01.dhsa.Model.FhirResources.*;
import com.group01.dhsa.ObserverPattern.EventListener;
import com.group01.dhsa.ObserverPattern.EventObservable;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * FhirExporter class listens for resource-related events and processes them accordingly.
 */
public class FhirExporter implements EventListener {

    private final EventObservable eventObservable;

    /**
     * Constructor that registers FhirExporter with the observable.
     *
     * @param eventObservable the event manager to register with.
     */
    public FhirExporter(EventObservable eventObservable) {
        this.eventObservable = eventObservable;
        this.eventObservable.subscribe("load_request", this);
        this.eventObservable.subscribe("search_request", this);
    }

    @Override
    public void handleEvent(String eventType, File file) {
        try {
            if ("load_request".equals(eventType)) {
                loadResources(file.getName());
            } else if ("search_request".equals(eventType)) {
                searchResources(file.getName(), file.getPath());
            }
        } catch (Exception e) {
            eventObservable.notify("error", new File("Error occurred: " + e.getMessage()));
        }
    }

    private void loadResources(String resourceType) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> resources = exporter.exportResources();

            // Notify completion with the loaded resources
            System.out.println("Loaded resources for type " + resourceType + ": " + resources);
            eventObservable.notify("load_complete", new File("Load successful"));
        } catch (Exception e) {
            System.err.println("Error loading resources for type: " + resourceType);
            eventObservable.notify("error", new File("Load failed"));
        }
    }

    private void searchResources(String resourceType, String searchTerm) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> results = exporter.searchResources(searchTerm);

            // Notify completion with the search results
            System.out.println("Search results for " + resourceType + " with term '" + searchTerm + "': " + results);
            eventObservable.notify("search_complete", new File("Search successful"));
        } catch (Exception e) {
            System.err.println("Error searching resources for type: " + resourceType + " with term: " + searchTerm);
            eventObservable.notify("error", new File("Search failed"));
        }
    }
}
