package com.group01.dhsa.Model.FhirResources.Level5.MedicationsModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporterFactory;

public class MedicationImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new MedicationImporter();
    }
}
