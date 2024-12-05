package com.group01.dhsa.Model.FhirResources;

// Classe astratta che rappresenta il Factory Method.
// Definisce il metodo astratto che le sottoclassi dovranno implementare per creare un importer specifico.
public abstract class FhirImporterFactory {
    // Metodo abstract per creare un importer in base al tipo di risorsa.
    public abstract FhirResourceImporter createImporter(String resourceType);
}