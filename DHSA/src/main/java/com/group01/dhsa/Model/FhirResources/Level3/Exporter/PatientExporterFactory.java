package com.group01.dhsa.Model.FhirResources.Level3.Exporter;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

public class PatientExporterFactory implements FhirResourceExporterFactory {
    @Override
    public FhirResourceExporter createExporter() {
        return new PatientExporter();
    }
}
