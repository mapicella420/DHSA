package com.group01.dhsa.Model.FhirResources;

// Factory Interface per creare FhirResourceExporter.
public interface FhirResourceExporterFactory {
    /**
     * Method for creating a specific exporter.
     *
     * @return An instance of FhirResourceExporter.
     */
    FhirResourceExporter createExporter();
}
