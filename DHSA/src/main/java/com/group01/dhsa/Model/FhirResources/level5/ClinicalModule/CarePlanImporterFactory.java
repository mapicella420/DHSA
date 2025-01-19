package com.group01.dhsa.Model.FhirResources.Level5.ClinicalModule;

import com.group01.dhsa.Model.FhirResources.*;

public class CarePlanImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new CarePlanImporter();
    }
}