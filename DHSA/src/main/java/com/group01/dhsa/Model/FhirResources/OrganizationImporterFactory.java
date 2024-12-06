package com.group01.dhsa.Model.FhirResources;

public class OrganizationImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new OrganizationImporter();
    }
}
