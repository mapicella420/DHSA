package com.group01.dhsa.Model.FhirResources;

public class PatientImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new PatientImporter();
    }
}
