package com.group01.dhsa.Model.FhirResources.Level4.Exporter.MedicationsModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory for creating ImmunizationExporter instances.
 */
public class ImmunizationExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new ImmunizationExporter();
    }
}
