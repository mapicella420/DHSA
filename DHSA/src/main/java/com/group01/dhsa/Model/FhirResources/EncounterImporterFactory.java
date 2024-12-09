package com.group01.dhsa.Model.FhirResources;

public class EncounterImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new EncounterImporter();
    }
}
