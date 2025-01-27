package com.group01.dhsa.Model.FhirResources;

import com.group01.dhsa.Model.FhirResources.Level4.Exporter.*;
import com.group01.dhsa.Model.FhirResources.Level5.Exporter.ClinicalModule.*;
import com.group01.dhsa.Model.FhirResources.Level5.Exporter.DiagnosticModule.*;
import com.group01.dhsa.Model.FhirResources.Level5.Exporter.MedicationsModule.*;

/**
 * Factory Manager for FHIR Resource Exporters.
 */
public class FhirExporterFactoryManager {

    /**
     * Returns the appropriate factory based on the resource type.
     *
     * @param resourceType The type of FHIR resource.
     * @return A factory for the specified resource type.
     */
    public static FhirResourceExporterFactory getFactory(String resourceType) {
        switch (resourceType.toLowerCase()) {
            case "patient":
                return new PatientExporterFactory();
            case "device":
                return new DeviceExporterFactory();
            case "organization":
                return new OrganizationExporterFactory();
            case "practitioner":
                return new ProviderExporterFactory();
            case "provider":
                return new ProviderExporterFactory();
            case "encounter":
                return new EncounterExporterFactory();
            case "allerg":
                return new AllergyExporterFactory();
            case "allergyintolerance":
                return new AllergyExporterFactory();
            case "careplan":
                return new CarePlanExporterFactory();
            case "condition":
                return new ConditionExporterFactory();
            case "procedure":
                return new ProcedureExporterFactory();
            case "imagingstudy": // Added ImagingStudy
                return new ImagingStudyExporterFactory();
            case "observation": // Added Observation
                return new ObservationExporterFactory();
            case "immunization": // Added Immunization
                return new ImmunizationExporterFactory();
            case "medicationrequest": // Added MedicationRequest
                return new MedicationExporterFactory();
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }
    }
}
