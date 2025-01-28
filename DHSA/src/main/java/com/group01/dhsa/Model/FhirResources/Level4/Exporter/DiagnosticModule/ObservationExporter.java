package com.group01.dhsa.Model.FhirResources.Level4.Exporter.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Controller.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
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
                    .count(1000)
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
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> observationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the search query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(Observation.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map search fields to FHIR API query parameters
                switch (searchField) {
                    case "id":
                        query = query.where(new StringClientParam("_id").matches().value(searchValue));
                        break;
                    case "status":
                        query = query.where(new StringClientParam("status").matches().value(searchValue));
                        break;
                    case "code":
                        query = query.where(new StringClientParam("code").matches().value(searchValue));
                        break;
                    case "subject":
                        query = query.where(new StringClientParam("subject").matches().value(searchValue));
                        break;
                    case "encounter":
                        query = query.where(new StringClientParam("encounter").matches().value(searchValue));
                        break;
                    case "effective":
                        query = query.where(new StringClientParam("date").matches().value(searchValue));
                        break;
                    case "value":
                        query = query.where(new StringClientParam("value-quantity").matches().value(searchValue));
                        break;
                    default:
                        System.err.println("[ERROR] Unsupported search field: " + searchField);
                }
            }

            // Execute the query and fetch the bundle
            Bundle bundle = query.returnBundle(Bundle.class).execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Observation observation = (Observation) entry.getResource();
                    Map<String, String> observationData = convertResourceToMap(observation);
                    observationsList.add(observationData);
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
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Observation)) {
            throw new IllegalArgumentException("Resource is not of type Observation");
        }

        Observation observation = (Observation) resource;
        Map<String, String> observationData = new HashMap<>();

        // Meta information
        if (observation.hasMeta()) {
            observationData.put("VersionId", observation.getMeta().hasVersionId() ? observation.getMeta().getVersionId() : "N/A");
            observationData.put("LastUpdated", observation.getMeta().hasLastUpdated() ? observation.getMeta().getLastUpdated().toString() : "N/A");
            observationData.put("Source", observation.getMeta().hasSource() ? observation.getMeta().getSource() : "N/A");
        } else {
            observationData.put("VersionId", "N/A");
            observationData.put("LastUpdated", "N/A");
            observationData.put("Source", "N/A");
        }

        // Identifier
        observationData.put("Id", observation.getIdentifierFirstRep() != null ? observation.getIdentifierFirstRep().getValue() : "N/A");

        // Status
        observationData.put("Status", observation.hasStatus() ? observation.getStatus().toCode() : "N/A");

        // Code
        if (observation.hasCode()) {
            Coding coding = observation.getCode().getCodingFirstRep();
            observationData.put("Code", coding.hasCode() ? coding.getCode() : "N/A");
            observationData.put("CodeText", observation.getCode().hasText() ? observation.getCode().getText() : "N/A");
        } else {
            observationData.put("Code", "N/A");
            observationData.put("CodeText", "N/A");
        }

        // Subject (Patient)
        observationData.put("Patient", observation.hasSubject() ? observation.getSubject().getReference() : "N/A");

        // Encounter
        observationData.put("Encounter", observation.hasEncounter() ? observation.getEncounter().getReference() : "N/A");

        // Effective DateTime
        observationData.put("EffectiveDateTime", observation.hasEffectiveDateTimeType() ? observation.getEffectiveDateTimeType().getValueAsString() : "N/A");

        // ValueQuantity
        if (observation.hasValueQuantity()) {
            observationData.put("Value", String.valueOf(observation.getValueQuantity().getValue()));
            observationData.put("Unit", observation.getValueQuantity().hasUnit() ? observation.getValueQuantity().getUnit() : "N/A");
        } else {
            observationData.put("Value", "N/A");
            observationData.put("Unit", "N/A");
        }

        return observationData;
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
