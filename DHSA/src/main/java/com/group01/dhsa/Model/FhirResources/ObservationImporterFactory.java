package com.group01.dhsa.Model.FhirResources;

public class ObservationImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new ObservationImporter();
    }
}
