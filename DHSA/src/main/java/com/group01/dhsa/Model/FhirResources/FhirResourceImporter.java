package com.group01.dhsa.Model.FhirResources;

// Interfaccia comune per tutti gli importer FHIR.
// Definisce il metodo che ogni importer deve implementare per importare un CSV sul server FHIR.
public interface FhirResourceImporter {
    // Metodo per importare un file CSV e creare risorse FHIR.
    void importCsvToFhir(String csvFilePath);
}