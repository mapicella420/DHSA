package com.group01.dhsa.Model.FhirResources.Level4;

import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporterFactory;

public class EncounterImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new EncounterImporter();
    }
}
