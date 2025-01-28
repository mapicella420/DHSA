package com.group01.dhsa.Model.FhirResources.Level4.Importer.ClinicalModule;
import com.group01.dhsa.Model.FhirResources.*;

public class ProcedureImporterFactory implements FhirResourceImporterFactory {
    @Override
    public FhirResourceImporter createImporter() {
        return new ProcedureImporter();
    }
}
