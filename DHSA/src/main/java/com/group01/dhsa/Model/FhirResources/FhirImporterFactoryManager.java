package com.group01.dhsa.Model.FhirResources;

public class FhirImporterFactoryManager {
    // Metodo statico per ottenere l'importer appropriato in base al nome del file
    public static FhirResourceImporter getImporter(String fileName) {
        String fileNameLowerCase = fileName.toLowerCase();

        // Verifica il tipo di risorsa in base al contenuto del nome del file
        if (fileNameLowerCase.contains("patient")) {
            return new PatientImporter();
        } else if (fileNameLowerCase.contains("observation")) {
            return new ObservationImporter();
        }

        else {
            throw new IllegalArgumentException("Tipo di risorsa non supportato: " + fileName);
        }
    }
}

