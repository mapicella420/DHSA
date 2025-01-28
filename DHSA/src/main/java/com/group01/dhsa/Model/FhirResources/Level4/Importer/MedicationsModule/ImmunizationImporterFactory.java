package com.group01.dhsa.Model.FhirResources.Level4.Importer.MedicationsModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporterFactory;

public class ImmunizationImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new ImmunizationImporter();
    }
}
