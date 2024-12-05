package com.group01.dhsa.Model.FhirResources;

// Implementazione concreta della factory per i pazienti.
// Crea un oggetto di tipo PatientImporter.
public class PatientImporterFactory extends FhirImporterFactory {
    @Override
    public FhirResourceImporter createImporter(String resourceType) {
        // Restituisce un importer specifico per i pazienti.
        return new PatientImporter();
    }
}


