package com.group01.dhsa.Model.FhirResources.Level4.Exporter.ClinicalModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory class for creating AllergyExporter instances.
 */
public class AllergyExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new AllergyExporter();
    }
}
