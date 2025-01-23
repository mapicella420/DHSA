package com.group01.dhsa.Model.FhirResources.Level5.Exporter.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ImagingStudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exporter for ImagingStudy resources.
 */
public class ImagingStudyExporter implements FhirResourceExporter {

    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public List<Map<String, String>> exportResources() {
        List<Map<String, String>> imagingStudiesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all ImagingStudy resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(ImagingStudy.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                ImagingStudy imagingStudy = (ImagingStudy) entry.getResource();

                // Extract relevant fields and store them in a Map
                imagingStudiesList.add(extractImagingStudyData(imagingStudy));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return imagingStudiesList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        List<Map<String, String>> imagingStudiesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for ImagingStudy resources
            Bundle bundle = client.search()
                    .forResource(ImagingStudy.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    ImagingStudy imagingStudy = (ImagingStudy) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesImagingStudy(imagingStudy, searchTerm)) {
                        imagingStudiesList.add(extractImagingStudyData(imagingStudy));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching ImagingStudy resources: " + e.getMessage());
            e.printStackTrace();
        }

        return imagingStudiesList;
    }

    private boolean matchesImagingStudy(ImagingStudy imagingStudy, String searchTerm) {
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Match Id
        if (imagingStudy.getIdElement().getIdPart().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Status
        if (imagingStudy.hasStatus() && imagingStudy.getStatus().toCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Patient reference
        if (imagingStudy.hasSubject() && imagingStudy.getSubject().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Encounter reference
        if (imagingStudy.hasEncounter() && imagingStudy.getEncounter().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Started
        if (imagingStudy.hasStarted() && imagingStudy.getStarted().toString().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Modality
        if (!imagingStudy.getSeries().isEmpty() && imagingStudy.getSeriesFirstRep().hasModality()) {
            Coding modalityCoding = imagingStudy.getSeriesFirstRep().getModality().getCodingFirstRep();
            if (modalityCoding.hasDisplay() && modalityCoding.getDisplay().toLowerCase().contains(lowerCaseSearchTerm)) {
                return true;
            }
        }

        // Match BodySite
        if (!imagingStudy.getSeries().isEmpty() && imagingStudy.getSeriesFirstRep().hasBodySite()) {
            Coding bodySiteCoding = imagingStudy.getSeriesFirstRep().getBodySite().getConcept().getCodingFirstRep();
            if (bodySiteCoding.hasDisplay() && bodySiteCoding.getDisplay().toLowerCase().contains(lowerCaseSearchTerm)) {
                return true;
            }
        }

        return false;
    }

    private Map<String, String> extractImagingStudyData(ImagingStudy imagingStudy) {
        Map<String, String> imagingStudyData = new HashMap<>();
        imagingStudyData.put("Id", imagingStudy.getIdentifierFirstRep() != null ? imagingStudy.getIdentifierFirstRep().getValue() : "N/A");
        imagingStudyData.put("Status", imagingStudy.hasStatus() ? imagingStudy.getStatus().toCode() : "N/A");
        imagingStudyData.put("Patient", imagingStudy.hasSubject() ? imagingStudy.getSubject().getReference() : "N/A");
        imagingStudyData.put("Encounter", imagingStudy.hasEncounter() ? imagingStudy.getEncounter().getReference() : "N/A");
        imagingStudyData.put("Started", imagingStudy.hasStarted() ? imagingStudy.getStarted().toString() : "N/A");

        if (!imagingStudy.getSeries().isEmpty()) {
            // Extract Modality
            if (imagingStudy.getSeriesFirstRep().hasModality()) {
                Coding modalityCoding = imagingStudy.getSeriesFirstRep().getModality().getCodingFirstRep();
                imagingStudyData.put("Modality", modalityCoding.hasDisplay() ? modalityCoding.getDisplay() : "N/A");
            } else {
                imagingStudyData.put("Modality", "N/A");
            }

            // Extract BodySite
            if (imagingStudy.getSeriesFirstRep().hasBodySite()) {
                Coding bodySiteCoding = imagingStudy.getSeriesFirstRep().getBodySite().getConcept().getCodingFirstRep();
                imagingStudyData.put("BodySite", bodySiteCoding.hasDisplay() ? bodySiteCoding.getDisplay() : "N/A");
            } else {
                imagingStudyData.put("BodySite", "N/A");
            }
        } else {
            imagingStudyData.put("Modality", "N/A");
            imagingStudyData.put("BodySite", "N/A");
        }

        return imagingStudyData;
    }
}
