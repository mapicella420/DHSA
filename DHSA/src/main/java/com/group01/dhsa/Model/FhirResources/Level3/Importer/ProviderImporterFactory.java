package com.group01.dhsa.Model.FhirResources.Level3.Importer;

import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporterFactory;

/**
 * Factory class for creating a ProviderImporter instance.
 */
public class ProviderImporterFactory implements FhirResourceImporterFactory {

    /**
     * Creates an importer for Provider resources.
     *
     * @return A new instance of ProviderImporter.
     */
    @Override
    public FhirResourceImporter createImporter() {
        return new ProviderImporter();
    }
}
