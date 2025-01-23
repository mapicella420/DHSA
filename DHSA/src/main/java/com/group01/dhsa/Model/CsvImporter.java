package com.group01.dhsa.Model;

import com.group01.dhsa.Model.FhirResources.*;
import com.group01.dhsa.ObserverPattern.EventListener;

import java.io.File;

/**
 * This class listens for CSV upload events and handles the import of CSV files into FHIR resources.
 * It implements the `EventListener` interface to observe and handle specific events.
 */
public class CsvImporter implements EventListener {

    /**
     * Handles the received event. If the event type is "csv_upload",
     * it triggers the CSV import process.
     *
     * @param eventType The type of the event, e.g., "csv_upload".
     * @param file      The file associated with the event, in this case, the CSV file to import.
     */
    @Override
    public void handleEvent(String eventType, File file) {
        if ("csv_upload".equals(eventType)) {
            importCsv(file); // Trigger the CSV import process
        }
    }

    /**
     * Imports a CSV file and converts its data into FHIR resources using the appropriate importer.
     *
     * @param file The CSV file to be imported.
     */
    public void importCsv(File file) {
        try {
            // Retrieve the file name for determining the resource type
            String fileName = file.getName();

            // Use the factory to obtain the appropriate importer for the file type
            FhirResourceImporterFactory factory = FhirImporterFactoryManager.getFactory(fileName);

            // Create the importer using the factory and process the CSV file
            FhirResourceImporter importer = factory.createImporter();
            System.out.println("Importing CSV: " + fileName);

            // Import the CSV data and create FHIR resources
            importer.importCsvToFhir(file.getAbsolutePath());

            // Notify observers after successful import (implementation not shown here)
        } catch (IllegalArgumentException e) {
            // Handle cases where the resource type is unsupported
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // General exception handling for unexpected issues
            System.err.println("Error during import: " + e.getMessage());
        }
    }
}
