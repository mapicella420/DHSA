package com.group01.dhsa.Model.FhirResources;

import com.group01.dhsa.Model.FhirResources.Level3.Importer.*;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.ClinicalModule.AllergyImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.ClinicalModule.CarePlanImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.ClinicalModule.ConditionImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.ClinicalModule.ProcedureImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.DiagnosticModule.ImagingStudyImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.DiagnosticModule.ObservationImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.MedicationsModule.ImmunizationImporterFactory;
import com.group01.dhsa.Model.FhirResources.Level4.Importer.MedicationsModule.MedicationImporterFactory;

/**
 * The FhirImporterFactoryManager class provides a centralized factory method to retrieve
 * appropriate FHIR resource importer factories based on the file name. It identifies the type
 * of resource by analyzing the file name and returns the corresponding factory implementation.
 */
public class FhirImporterFactoryManager {

    /**
     * Retrieves the appropriate FHIR resource importer factory based on the provided file name.
     * The file name is analyzed (case-insensitively) to determine the resource type, and the
     * corresponding factory is returned. If no factory matches the file name, an exception is thrown.
     *
     * @param fileName The name of the file to determine the type of FHIR resource.
     * @return A specific implementation of {@link FhirResourceImporterFactory} for the resource type.
     * @throws IllegalArgumentException if the resource type is not supported.
     */
    public static FhirResourceImporterFactory getFactory(String fileName) {
        // Convert the file name to lowercase for case-insensitive matching.
        String fileNameLowerCase = fileName.toLowerCase();

        // Identify and return the appropriate factory based on the file name.
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
        } else if (fileNameLowerCase.contains("conditions")) {
            return new ConditionImporterFactory();
        } else if (fileNameLowerCase.contains("imaging")) {
            return new ImagingStudyImporterFactory();
        } else if (fileNameLowerCase.contains("medication")) {
            return new MedicationImporterFactory();
        } else if (fileNameLowerCase.contains("immunization")) {
            return new ImmunizationImporterFactory();
        } else {
            // If no matching factory is found, throw an exception indicating an unsupported resource type.
            throw new IllegalArgumentException("Unsupported resource type: " + fileName);
        }
    }
}
