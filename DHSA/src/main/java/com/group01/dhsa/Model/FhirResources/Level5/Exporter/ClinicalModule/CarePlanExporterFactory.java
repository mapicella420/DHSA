package com.group01.dhsa.Model.FhirResources.Level5.Exporter.ClinicalModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory for creating CarePlanExporter instances.
 */
public class CarePlanExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new CarePlanExporter();
    }
}
