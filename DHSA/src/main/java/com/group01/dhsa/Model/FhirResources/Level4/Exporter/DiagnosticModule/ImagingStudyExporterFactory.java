package com.group01.dhsa.Model.FhirResources.Level4.Exporter.DiagnosticModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;

/**
 * Factory for creating ImagingStudyExporter instances.
 */
public class ImagingStudyExporterFactory implements FhirResourceExporterFactory {

    @Override
    public FhirResourceExporter createExporter() {
        return new ImagingStudyExporter();
    }
}
