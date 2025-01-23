package com.group01.dhsa.Model.FhirResources.Level5.Exporter.DiagnosticModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory for creating ObservationExporter instances.
 */
public class ObservationExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new ObservationExporter();
    }
}
