package com.group01.dhsa.Model.FhirResources;

/**
 * Common interface for all FHIR resource importers.
 * This interface defines the method that each importer must implement to import a CSV file into a FHIR server.
 */
public interface FhirResourceImporter {

    /**
     * Imports a CSV file and creates FHIR resources.
     * This method processes the given CSV file and uses its data to generate FHIR resources,
     * which are then uploaded to the FHIR server.
     *
     * @param csvFilePath The path to the CSV file to be imported.
     */
    void importCsvToFhir(String csvFilePath);
}
