package com.group01.dhsa.Model.FhirResources.level5.ClinicalModule;

import com.group01.dhsa.Model.FhirResources.*;

public class ConditionImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new ConditionImporter();
    }
}