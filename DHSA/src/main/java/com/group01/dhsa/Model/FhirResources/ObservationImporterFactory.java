package com.group01.dhsa.Model.FhirResources;

// Implementazione concreta della factory per le osservazioni.
// Crea un oggetto di tipo ObservationImporter.
public class ObservationImporterFactory extends FhirImporterFactory {
    @Override
    public FhirResourceImporter createImporter(String resourceType) {
        // Restituisce un importer specifico per le osservazioni.
        return new ObservationImporter();
    }
}