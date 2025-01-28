package com.group01.dhsa.Model.FhirResources;

/**
 * Factory interface for creating FHIR resource importers.
 * This interface defines a method for creating instances of {@link FhirResourceImporter}.
 * It ensures that each factory provides a standardized way to instantiate its specific importer.
 */
public interface FhirResourceImporterFactory {

    /**
     * Creates and returns an instance of a FHIR resource importer.
     * This method is implemented by specific factories to instantiate and return the appropriate importer.
     *
     * @return A new instance of {@link FhirResourceImporter}.
     */
    FhirResourceImporter createImporter();
}
