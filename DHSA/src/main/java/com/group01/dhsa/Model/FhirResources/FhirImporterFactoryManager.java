package com.group01.dhsa.Model.FhirResources;

import com.group01.dhsa.Model.FhirResources.Level4.*;
import com.group01.dhsa.Model.FhirResources.Level5.ClinicalModule.*;
import com.group01.dhsa.Model.FhirResources.Level5.DiagnosticModule.*;
import com.group01.dhsa.Model.FhirResources.Level5.MedicationsModule.ImmunizationImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level5.MedicationsModule.MedicationImporterFactory;

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
        } else if (fileNameLowerCase.contains("careplan")) {
            return new CarePlanImporterFactory();
        } else if (fileNameLowerCase.contains("procedure")) {
            return new ProcedureImporterFactory();
        }  else if (fileNameLowerCase.contains("conditions")) {
            return new ConditionImporterFactory();
        } else if (fileNameLowerCase.contains("imaging")) {
            return new ImagingStudyImporterFactory();
        } else if (fileNameLowerCase.contains("medication")) {
            return new MedicationImporterFactory();
        } else if (fileNameLowerCase.contains("immunization")) {
            return new ImmunizationImporterFactory();
        } else {
            throw new IllegalArgumentException("Tipo di risorsa non supportato: " + fileName);
        }
    }
}
