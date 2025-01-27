package com.group01.dhsa.Model.FhirResources.Level5.Exporter.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.AllergyIntolerance;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllergyExporter implements FhirResourceExporter {

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
        List<Map<String, String>> allergiesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all AllergyIntolerance resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(AllergyIntolerance.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                AllergyIntolerance allergy = (AllergyIntolerance) entry.getResource();
                allergiesList.add(extractAllergyDetails(allergy));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return allergiesList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> allergiesList = new ArrayList<>();
        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for all AllergyIntolerance resources
            Bundle bundle = client.search()
                    .forResource(AllergyIntolerance.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    AllergyIntolerance allergy = (AllergyIntolerance) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesAllergy(allergy, searchTerm)) {
                        allergiesList.add(extractAllergyDetails(allergy));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Allergy resources: " + e.getMessage());
            e.printStackTrace();
        }

        return allergiesList;
    }

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> allergiesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all AllergyIntolerance resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(AllergyIntolerance.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    AllergyIntolerance allergy = (AllergyIntolerance) entry.getResource();

                    boolean matches = true;
                    for (int i = 0; i < searchFields.length; i++) {
                        String searchField = searchFields[i].toLowerCase();
                        String searchValue = searchValues[i];

                        // Handle each search field
                        switch (searchField) {
                            case "patient":
                                if (!allergy.hasPatient() || !allergy.getPatient().getReference().contains(searchValue)) {
                                    matches = false;
                                }
                                break;
                            case "encounter":
                                // Check if the extension contains the encounter reference
                                if (!allergy.hasExtension("http://hl7.org/fhir/StructureDefinition/encounter-reference")) {
                                    matches = false;
                                } else {
                                    String encounterReference = ((org.hl7.fhir.r5.model.Reference) allergy
                                            .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/encounter-reference")
                                            .getValue())
                                            .getReference();
                                    if (!encounterReference.contains(searchValue)) {
                                        matches = false;
                                    }
                                }
                                break;
                            default:
                                System.err.println("[ERROR] Unsupported search field: " + searchField);
                                matches = false;
                        }

                        // If any condition is not met, skip this resource
                        if (!matches) {
                            break;
                        }
                    }

                    // If all conditions are satisfied, add the resource to the result list
                    if (matches) {
                        allergiesList.add(convertResourceToMap(allergy));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Allergy resources: " + e.getMessage());
            e.printStackTrace();
        }

        return allergiesList;
    }

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof AllergyIntolerance)) {
            throw new IllegalArgumentException("Resource is not of type AllergyIntolerance");
        }

        AllergyIntolerance allergy = (AllergyIntolerance) resource;
        Map<String, String> allergyData = new HashMap<>();

        // ID
        allergyData.put("Id", allergy.getIdElement().getIdPart());

        // Meta
        if (allergy.hasMeta()) {
            allergyData.put("VersionId", allergy.getMeta().hasVersionId() ? allergy.getMeta().getVersionId() : "N/A");
            allergyData.put("LastUpdated", allergy.getMeta().hasLastUpdated() ? allergy.getMeta().getLastUpdated().toString() : "N/A");
            allergyData.put("Source", allergy.getMeta().hasSource() ? allergy.getMeta().getSource() : "N/A");
        } else {
            allergyData.put("VersionId", "N/A");
            allergyData.put("LastUpdated", "N/A");
            allergyData.put("Source", "N/A");
        }

        // Patient
        allergyData.put("Patient", allergy.hasPatient() ? allergy.getPatient().getReference() : "N/A");

        // Encounter
        if (allergy.hasExtension("http://hl7.org/fhir/StructureDefinition/encounter-reference")) {
            allergyData.put("Encounter", allergy.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/encounter-reference").getValue().toString());
        } else {
            allergyData.put("Encounter", "N/A");
        }

        // Recorded Date
        allergyData.put("Start", allergy.hasRecordedDate() ? allergy.getRecordedDateElement().getValueAsString() : "N/A");

        // Allergy End Date
        if (allergy.hasExtension("http://hl7.org/fhir/StructureDefinition/allergy-end-date")) {
            allergyData.put("End", allergy.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/allergy-end-date").getValue().toString());
        } else {
            allergyData.put("End", "N/A");
        }

        // Code and Description
        if (allergy.hasCode()) {
            allergyData.put("Code", allergy.getCode().getCodingFirstRep().getCode());
            allergyData.put("Description", allergy.getCode().getCodingFirstRep().getDisplay());
        } else {
            allergyData.put("Code", "N/A");
            allergyData.put("Description", "N/A");
        }

        // Clinical Status
        allergyData.put("ClinicalStatus", allergy.hasClinicalStatus() ? allergy.getClinicalStatus().getCodingFirstRep().getCode() : "N/A");

        // Verification Status
        allergyData.put("VerificationStatus", allergy.hasVerificationStatus() ? allergy.getVerificationStatus().getCodingFirstRep().getCode() : "N/A");

        return allergyData;
    }



    private boolean matchesAllergy(AllergyIntolerance allergy, String searchTerm) {
        setFhirServerUrl();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        return (allergy.hasPatient() && allergy.getPatient().getReference().toLowerCase().contains(lowerCaseSearchTerm)) ||
                (allergy.hasCode() && allergy.getCode().getCodingFirstRep().getCode().toLowerCase().contains(lowerCaseSearchTerm)) ||
                (allergy.hasCode() && allergy.getCode().getCodingFirstRep().getDisplay().toLowerCase().contains(lowerCaseSearchTerm));
    }

    private Map<String, String> extractAllergyDetails(AllergyIntolerance allergy) {
        setFhirServerUrl();
        Map<String, String> allergyData = new HashMap<>();
        allergyData.put("Patient", allergy.hasPatient() ? allergy.getPatient().getReference() : "N/A");

        if (allergy.hasExtension("http://hl7.org/fhir/StructureDefinition/encounter-reference")) {
            allergyData.put("Encounter", allergy.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/encounter-reference").getValue().toString());
        } else {
            allergyData.put("Encounter", "N/A");
        }

        allergyData.put("Start", allergy.hasRecordedDate() ? allergy.getRecordedDateElement().getValueAsString() : "N/A");

        if (allergy.hasExtension("http://hl7.org/fhir/StructureDefinition/allergy-end-date")) {
            allergyData.put("End", allergy.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/allergy-end-date").getValue().toString());
        } else {
            allergyData.put("End", "N/A");
        }

        if (allergy.hasCode()) {
            allergyData.put("Code", allergy.getCode().getCodingFirstRep().getCode());
            allergyData.put("Description", allergy.getCode().getCodingFirstRep().getDisplay());
        } else {
            allergyData.put("Code", "N/A");
            allergyData.put("Description", "N/A");
        }

        allergyData.put("ClinicalStatus", allergy.hasClinicalStatus() ? allergy.getClinicalStatus().getCodingFirstRep().getCode() : "N/A");
        allergyData.put("VerificationStatus", allergy.hasVerificationStatus() ? allergy.getVerificationStatus().getCodingFirstRep().getCode() : "N/A");

        return allergyData;
    }
}
