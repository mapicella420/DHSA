package com.group01.dhsa.Model.FhirResources.Level4.Exporter;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

public class ProviderExporterFactory implements FhirResourceExporterFactory {
    @Override
    public FhirResourceExporter createExporter() {
        return new ProviderExporter();
    }
}
