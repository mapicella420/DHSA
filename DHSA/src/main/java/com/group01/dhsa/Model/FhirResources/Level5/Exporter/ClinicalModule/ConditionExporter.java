package com.group01.dhsa.Model.FhirResources.Level5.Exporter.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionExporter implements FhirResourceExporter {

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
        List<Map<String, String>> conditionsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Condition resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Condition.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Condition condition = (Condition) entry.getResource();

                // Extract relevant fields and store them in a Map
                conditionsList.add(extractConditionData(condition));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return conditionsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> conditionsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for all Condition resources
            Bundle bundle = client.search()
                    .forResource(Condition.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Condition condition = (Condition) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesCondition(condition, searchTerm)) {
                        conditionsList.add(extractConditionData(condition));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Condition resources: " + e.getMessage());
            e.printStackTrace();
        }

        return conditionsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchField, String searchValue) {
        return null;
    }

    private boolean matchesCondition(Condition condition, String searchTerm) {
        setFhirServerUrl();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Match Patient reference
        if (condition.hasSubject() && condition.getSubject().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Code
        if (condition.hasCode() && condition.getCode().getCodingFirstRep().getCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Description
        if (condition.hasCode() && condition.getCode().getCodingFirstRep().getDisplay().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Onset
        if (condition.hasOnsetDateTimeType() && condition.getOnsetDateTimeType().getValueAsString().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Abatement
        if (condition.hasAbatementDateTimeType() && condition.getAbatementDateTimeType().getValueAsString().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Clinical Status
        if (condition.hasClinicalStatus() && condition.getClinicalStatus().getCodingFirstRep().getCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Verification Status
        if (condition.hasVerificationStatus() && condition.getVerificationStatus().getCodingFirstRep().getCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        return false;
    }

    private Map<String, String> extractConditionData(Condition condition) {
        setFhirServerUrl();
        Map<String, String> conditionData = new HashMap<>();
        conditionData.put("Id", condition.getIdentifierFirstRep() != null ? condition.getIdentifierFirstRep().getValue() : "N/A");
        conditionData.put("Patient", condition.hasSubject() ? condition.getSubject().getReference() : "N/A");
        conditionData.put("Encounter", condition.hasEncounter() ? condition.getEncounter().getReference() : "N/A");
        conditionData.put("Onset", condition.hasOnsetDateTimeType() ? condition.getOnsetDateTimeType().getValueAsString() : "N/A");
        conditionData.put("Abatement", condition.hasAbatementDateTimeType() ? condition.getAbatementDateTimeType().getValueAsString() : "N/A");

        if (condition.hasCode() && !condition.getCode().getCoding().isEmpty()) {
            Coding coding = condition.getCode().getCodingFirstRep();
            conditionData.put("Code", coding.hasCode() ? coding.getCode() : "N/A");
            conditionData.put("Description", coding.hasDisplay() ? coding.getDisplay() : "N/A");
        } else {
            conditionData.put("Code", "N/A");
            conditionData.put("Description", "N/A");
        }

        conditionData.put("ClinicalStatus", condition.hasClinicalStatus() ? condition.getClinicalStatus().getCodingFirstRep().getCode() : "N/A");
        conditionData.put("VerificationStatus", condition.hasVerificationStatus() ? condition.getVerificationStatus().getCodingFirstRep().getCode() : "N/A");

        return conditionData;
    }
}
