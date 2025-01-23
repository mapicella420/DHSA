package com.group01.dhsa.Model.FhirResources;

// Factory Interface per creare FhirResourceExporter.
public interface FhirResourceExporterFactory {
    /**
     * Metodo per creare un esportatore specifico.
     *
     * @return Un'istanza di FhirResourceExporter.
     */
    FhirResourceExporter createExporter();
}
