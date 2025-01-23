package com.group01.dhsa.Model;

import com.group01.dhsa.Model.FhirResources.*;
import com.group01.dhsa.ObserverPattern.EventListener;

import java.io.File;

public class FhirExporter implements EventListener {

    @Override
    public void handleEvent(String eventType, File file) {
        if ("export_request".equals(eventType)) {
            exportResource(file.getName());
        }
    }

    public void exportResource(String resourceType) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            Object resources = exporter.exportResources();

            // Passa i dati alla tabella o al componente appropriato
            System.out.println("Exported resources: " + resources);

        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
        }
    }
}
