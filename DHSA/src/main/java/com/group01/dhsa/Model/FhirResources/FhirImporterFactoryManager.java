package com.group01.dhsa.Model.FhirResources;

public class FhirImporterFactoryManager {
    public static FhirResourceImporterFactory getFactory(String fileName) {
        String fileNameLowerCase = fileName.toLowerCase();

        if (fileNameLowerCase.contains("patient")) {
            return new PatientImporterFactory();
        } else if (fileNameLowerCase.contains("organization")) {
            return new OrganizationImporterFactory();
        } else if (fileNameLowerCase.contains("encounter")) {
            return new EncounterImporterFactory();
        } else if (fileNameLowerCase.contains("observation")) {
            return new ObservationImporterFactory();
        } else {
            throw new IllegalArgumentException("Tipo di risorsa non supportato: " + fileName);
        }
    }
}
