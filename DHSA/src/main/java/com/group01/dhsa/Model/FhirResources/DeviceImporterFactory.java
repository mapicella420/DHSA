package com.group01.dhsa.Model.FhirResources;

public class DeviceImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new DeviceImporter();
    }
}
