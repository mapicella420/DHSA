package com.group01.dhsa.Model.FhirResources.Level5.Exporter.MedicationsModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory for creating MedicationExporter instances.
 */
public class MedicationExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new MedicationExporter();
    }
}
