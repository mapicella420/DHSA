package com.group01.dhsa.Model.FhirResources.Level4.Exporter.ClinicalModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory for creating ProcedureExporter instances.
 */
public class ProcedureExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new ProcedureExporter();
    }
}
