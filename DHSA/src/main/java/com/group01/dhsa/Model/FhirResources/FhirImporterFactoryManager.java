package com.group01.dhsa.Model.FhirResources;

import com.group01.dhsa.Model.FhirResources.level5.ClinicalModule.AllergyImporterFactory;
import com.group01.dhsa.Model.FhirResources.level5.DiagnosticModule.ObservationImporterFactory;

public class FhirImporterFactoryManager {
    public static FhirResourceImporterFactory getFactory(String fileName) {
        String fileNameLowerCase = fileName.toLowerCase();

        if (fileNameLowerCase.contains("patient")) {
            return new PatientImporterFactory();
        } else if (fileNameLowerCase.contains("organization")) {
            return new OrganizationImporterFactory();
        } else if (fileNameLowerCase.contains("encounter")) {
            return new EncounterImporterFactory();
        } else if (fileNameLowerCase.contains("provider")) {
            return new ProviderImporterFactory();
        } else if (fileNameLowerCase.contains("observation")) {
            return new ObservationImporterFactory();
        } else if (fileNameLowerCase.contains("device")) {
            return new DeviceImporterFactory();
        } else if (fileNameLowerCase.contains("allerg")) {
            return new AllergyImporterFactory();
        } else {
            throw new IllegalArgumentException("Tipo di risorsa non supportato: " + fileName);
        }
    }
}
