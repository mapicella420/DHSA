package com.group01.dhsa.Model.FhirResources.Level5.Exporter.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Observation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exporter for Observation resources.
 */
public class ObservationExporter implements FhirResourceExporter {

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
        List<Map<String, String>> observationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Observation resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Observation.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Observation observation = (Observation) entry.getResource();

                // Extract relevant fields and store them in a Map
                observationsList.add(extractObservationData(observation));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return observationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> observationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Observation resources
            Bundle bundle = client.search()
                    .forResource(Observation.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Observation observation = (Observation) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesObservation(observation, searchTerm)) {
                        observationsList.add(extractObservationData(observation));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Observation resources: " + e.getMessage());
            e.printStackTrace();
        }

        return observationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchField, String searchValue) {
        return List.of();
    }

    private boolean matchesObservation(Observation observation, String searchTerm) {
        setFhirServerUrl();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Match Id
        if (observation.getIdElement().getIdPart().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Status
        if (observation.hasStatus() && observation.getStatus().toCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Code
        if (observation.hasCode() && observation.getCode().getText() != null &&
                observation.getCode().getText().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Subject
        if (observation.hasSubject() && observation.getSubject().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Encounter
        if (observation.hasEncounter() && observation.getEncounter().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Effective DateTime
        if (observation.hasEffectiveDateTimeType() &&
                observation.getEffectiveDateTimeType().getValueAsString().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match ValueQuantity
        if (observation.hasValueQuantity() &&
                String.valueOf(observation.getValueQuantity().getValue()).contains(searchTerm)) {
            return true;
        }

        return false;
    }

    private Map<String, String> extractObservationData(Observation observation) {
        setFhirServerUrl();
        Map<String, String> observationData = new HashMap<>();
        observationData.put("Id", observation.getIdentifierFirstRep() != null ? observation.getIdentifierFirstRep().getValue() : "N/A");
        observationData.put("Status", observation.hasStatus() ? observation.getStatus().toCode() : "N/A");
        observationData.put("Code", observation.hasCode() && observation.getCode().hasText() ? observation.getCode().getText() : "N/A");
        observationData.put("Subject", observation.hasSubject() ? observation.getSubject().getReference() : "N/A");
        observationData.put("Encounter", observation.hasEncounter() ? observation.getEncounter().getReference() : "N/A");
        observationData.put("EffectiveDateTime", observation.hasEffectiveDateTimeType() ? observation.getEffectiveDateTimeType().getValueAsString() : "N/A");

        if (observation.hasValueQuantity()) {
            observationData.put("Value", String.valueOf(observation.getValueQuantity().getValue()));
            observationData.put("Unit", observation.getValueQuantity().hasUnit() ? observation.getValueQuantity().getUnit() : "N/A");
        } else {
            observationData.put("Value", "N/A");
            observationData.put("Unit", "N/A");
        }

        return observationData;
    }
}
