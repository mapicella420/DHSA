package com.group01.dhsa.Model.FhirResources.Level4.Importer.DiagnosticModule;

import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporterFactory;

public class ImagingStudyImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new ImagingStudyImporter();
    }
}
