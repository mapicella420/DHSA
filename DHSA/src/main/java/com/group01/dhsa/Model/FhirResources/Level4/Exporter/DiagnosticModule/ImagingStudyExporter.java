package com.group01.dhsa.Model.FhirResources.Level4.Exporter.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
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

    private static String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    private static void setFhirServerUrl() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIR_SERVER_URL = "http://localhost:8081/fhir";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIR_SERVER_URL = "http://localhost:8080/fhir";
            }
        }
    }

    @Override
    public List<Map<String, String>> exportResources() {
        setFhirServerUrl();
        List<Map<String, String>> imagingStudiesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all ImagingStudy resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(ImagingStudy.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
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
        setFhirServerUrl();
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

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> imagingStudiesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(ImagingStudy.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map fields to FHIR API query parameters
                switch (searchField) {
                    case "id":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("_id").exactly().code(searchValue));
                        break;
                    case "patient":
                        query = query.where(new ca.uhn.fhir.rest.gclient.ReferenceClientParam("subject").hasId(searchValue));
                        break;
                    case "encounter":
                        query = query.where(new ca.uhn.fhir.rest.gclient.ReferenceClientParam("encounter").hasId(searchValue));
                        break;
                    case "status":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("status").exactly().code(searchValue));
                        break;
                    case "started":
                        query = query.where(new ca.uhn.fhir.rest.gclient.DateClientParam("started").exactly().day(searchValue));
                        break;
                    case "modality":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("modality").exactly().code(searchValue));
                        break;
                    default:
                        System.err.println("[ERROR] Unsupported search field: " + searchField);
                }
            }

            // Execute the query and retrieve results
            Bundle bundle = query.returnBundle(Bundle.class).execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    ImagingStudy imagingStudy = (ImagingStudy) entry.getResource();
                    imagingStudiesList.add(convertResourceToMap(imagingStudy));
                }

                // Retrieve the next page
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


    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof ImagingStudy)) {
            throw new IllegalArgumentException("Resource is not of type ImagingStudy");
        }

        ImagingStudy imagingStudy = (ImagingStudy) resource;
        Map<String, String> imagingStudyData = new HashMap<>();

        // Meta information
        if (imagingStudy.hasMeta()) {
            imagingStudyData.put("VersionId", imagingStudy.getMeta().hasVersionId() ? imagingStudy.getMeta().getVersionId() : "N/A");
            imagingStudyData.put("LastUpdated", imagingStudy.getMeta().hasLastUpdated() ? imagingStudy.getMeta().getLastUpdated().toString() : "N/A");
            imagingStudyData.put("Source", imagingStudy.getMeta().hasSource() ? imagingStudy.getMeta().getSource() : "N/A");
        } else {
            imagingStudyData.put("VersionId", "N/A");
            imagingStudyData.put("LastUpdated", "N/A");
            imagingStudyData.put("Source", "N/A");
        }

        // Identifier
        imagingStudyData.put("Id", imagingStudy.getIdentifierFirstRep() != null ? imagingStudy.getIdentifierFirstRep().getValue() : "N/A");

        // Status
        imagingStudyData.put("Status", imagingStudy.hasStatus() ? imagingStudy.getStatus().toCode() : "N/A");

        // Subject (Patient)
        imagingStudyData.put("Patient", imagingStudy.hasSubject() ? imagingStudy.getSubject().getReference() : "N/A");

        // Encounter
        imagingStudyData.put("Encounter", imagingStudy.hasEncounter() ? imagingStudy.getEncounter().getReference() : "N/A");

        // Started date
        imagingStudyData.put("Started", imagingStudy.hasStarted() ? imagingStudy.getStarted().toString() : "N/A");

        if (!imagingStudy.getSeries().isEmpty()) {
            // Series-level data extraction
            ImagingStudy.ImagingStudySeriesComponent firstSeries = imagingStudy.getSeriesFirstRep();

            // Modality
            if (firstSeries.hasModality()) {
                Coding modalityCoding = firstSeries.getModality().getCodingFirstRep();
                imagingStudyData.put("Modality", modalityCoding.hasDisplay() ? modalityCoding.getDisplay() : "N/A");
            } else {
                imagingStudyData.put("Modality", "N/A");
            }

            // BodySite
            if (firstSeries.hasBodySite() && firstSeries.getBodySite().getConcept() != null) {
                Coding bodySiteCoding = firstSeries.getBodySite().getConcept().getCodingFirstRep();
                imagingStudyData.put("BodySite", bodySiteCoding.hasDisplay() ? bodySiteCoding.getDisplay() : "N/A");
            } else {
                imagingStudyData.put("BodySite", "N/A");
            }

            // Instance-level data extraction (e.g., SOPClass)
            if (!firstSeries.getInstance().isEmpty()) {
                ImagingStudy.ImagingStudySeriesInstanceComponent firstInstance = firstSeries.getInstanceFirstRep();
                if (firstInstance.hasSopClass()) {
                    imagingStudyData.put("SOPClass", firstInstance.getSopClass().hasDisplay() ? firstInstance.getSopClass().getDisplay() : "N/A");
                } else {
                    imagingStudyData.put("SOPClass", "N/A");
                }
            } else {
                imagingStudyData.put("SOPClass", "N/A");
            }
        } else {
            imagingStudyData.put("Modality", "N/A");
            imagingStudyData.put("BodySite", "N/A");
            imagingStudyData.put("SOPClass", "N/A");
        }

        return imagingStudyData;
    }


    private boolean matchesImagingStudy(ImagingStudy imagingStudy, String searchTerm) {
        setFhirServerUrl();
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
        setFhirServerUrl();
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
