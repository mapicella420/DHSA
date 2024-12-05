package com.group01.dhsa.Model.FhirResources;

// Implementazione concreta di FhirResourceImporter per gestire le osservazioni.
public class EncounterImporter implements FhirResourceImporter {
    @Override
    public void importCsvToFhir(String csvFilePath) {
        // Logica per leggere il file CSV delle osservazioni e inviarlo al server FHIR.
    }
}