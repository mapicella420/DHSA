package com.group01.dhsa.Model.FhirResources.Level4.Exporter.MedicationsModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.MedicationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exporter for MedicationRequest resources.
 */
public class MedicationExporter implements FhirResourceExporter {

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
        List<Map<String, String>> medicationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all MedicationRequest resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(MedicationRequest.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                MedicationRequest medicationRequest = (MedicationRequest) entry.getResource();

                // Extract relevant fields and store them in a Map
                medicationsList.add(extractMedicationData(medicationRequest));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return medicationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> medicationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for MedicationRequest resources
            Bundle bundle = client.search()
                    .forResource(MedicationRequest.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    MedicationRequest medicationRequest = (MedicationRequest) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesMedication(medicationRequest, searchTerm)) {
                        medicationsList.add(extractMedicationData(medicationRequest));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching MedicationRequest resources: " + e.getMessage());
            e.printStackTrace();
        }

        return medicationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> medicationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the search query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(MedicationRequest.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map search fields to FHIR API query parameters
                switch (searchField) {
                    case "id":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("_id").exactly().code(searchValue));
                        break;
                    case "status":
                        query = query.where(new ca.uhn.fhir.rest.gclient.StringClientParam("status").matches().value(searchValue));
                        break;
                    case "medicationcode":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("medication-code").exactly().code(searchValue));
                        break;
                    case "patient":
                        query = query.where(new ca.uhn.fhir.rest.gclient.ReferenceClientParam("subject").hasId(searchValue));
                        break;
                    case "encounter":
                        query = query.where(new ca.uhn.fhir.rest.gclient.ReferenceClientParam("encounter").hasId(searchValue));
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
                    MedicationRequest medicationRequest = (MedicationRequest) entry.getResource();
                    Map<String, String> medicationData = convertResourceToMap(medicationRequest);
                    medicationsList.add(medicationData);
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching MedicationRequest resources: " + e.getMessage());
            e.printStackTrace();
        }

        return medicationsList;
    }


    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof MedicationRequest)) {
            throw new IllegalArgumentException("Resource is not of type MedicationRequest");
        }

        MedicationRequest medicationRequest = (MedicationRequest) resource;
        Map<String, String> medicationData = new HashMap<>();

        // Meta information
        if (medicationRequest.hasMeta()) {
            medicationData.put("VersionId", medicationRequest.getMeta().hasVersionId() ? medicationRequest.getMeta().getVersionId() : "N/A");
            medicationData.put("LastUpdated", medicationRequest.getMeta().hasLastUpdated() ? medicationRequest.getMeta().getLastUpdated().toString() : "N/A");
            medicationData.put("Source", medicationRequest.getMeta().hasSource() ? medicationRequest.getMeta().getSource() : "N/A");
        } else {
            medicationData.put("VersionId", "N/A");
            medicationData.put("LastUpdated", "N/A");
            medicationData.put("Source", "N/A");
        }

        // Identifier and Status
        medicationData.put("Id", medicationRequest.getIdElement().getIdPart());
        medicationData.put("Status", medicationRequest.hasStatus() ? medicationRequest.getStatus().toCode() : "N/A");

        // Medication
        if (medicationRequest.hasMedication() && medicationRequest.getMedication().getConcept().getCodingFirstRep() != null) {
            Coding medicationCoding = medicationRequest.getMedication().getConcept().getCodingFirstRep();
            medicationData.put("MedicationCode", medicationCoding.hasCode() ? medicationCoding.getCode() : "N/A");
            medicationData.put("MedicationDisplay", medicationCoding.hasDisplay() ? medicationCoding.getDisplay() : "N/A");
            medicationData.put("MedicationSystem", medicationCoding.hasSystem() ? medicationCoding.getSystem() : "N/A");
        } else {
            medicationData.put("MedicationCode", "N/A");
            medicationData.put("MedicationDisplay", "N/A");
            medicationData.put("MedicationSystem", "N/A");
        }

        // Subject (Patient) and Encounter
        medicationData.put("Patient", medicationRequest.hasSubject() ? medicationRequest.getSubject().getReference() : "N/A");
        medicationData.put("Encounter", medicationRequest.hasEncounter() ? medicationRequest.getEncounter().getReference() : "N/A");

        // Base Cost
        if (medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost") != null) {
            var baseCostExtension = medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue();
            if (baseCostExtension instanceof org.hl7.fhir.r5.model.Quantity) {
                medicationData.put("BaseCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) baseCostExtension).getValue()));
            } else {
                medicationData.put("BaseCost", "N/A");
            }
        } else {
            medicationData.put("BaseCost", "N/A");
        }

        // Total Cost
        if (medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-cost") != null) {
            var totalCostExtension = medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-cost").getValue();
            if (totalCostExtension instanceof org.hl7.fhir.r5.model.Quantity) {
                medicationData.put("TotalCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) totalCostExtension).getValue()));
            } else {
                medicationData.put("TotalCost", "N/A");
            }
        } else {
            medicationData.put("TotalCost", "N/A");
        }

        return medicationData;
    }


    private boolean matchesMedication(MedicationRequest medicationRequest, String searchTerm) {
        setFhirServerUrl();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Match Id
        if (medicationRequest.getIdElement().getIdPart().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Status
        if (medicationRequest.hasStatus() && medicationRequest.getStatus().toCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Medication (Code and Display)
        if (medicationRequest.hasMedication() &&
                medicationRequest.getMedication().getConcept().getCodingFirstRep().hasDisplay() &&
                medicationRequest.getMedication().getConcept().getCodingFirstRep().getDisplay().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Patient
        if (medicationRequest.hasSubject() && medicationRequest.getSubject().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Encounter
        if (medicationRequest.hasEncounter() && medicationRequest.getEncounter().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Base Cost
        if (medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost") != null) {
            var extension = medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                String baseCost = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                if (baseCost.contains(searchTerm)) {
                    return true;
                }
            }
        }

        // Match Total Cost
        if (medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-cost") != null) {
            var extension = medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                String totalCost = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                if (totalCost.contains(searchTerm)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Map<String, String> extractMedicationData(MedicationRequest medicationRequest) {
        setFhirServerUrl();
        Map<String, String> medicationData = new HashMap<>();
        medicationData.put("Id", medicationRequest.getIdElement().getIdPart());
        medicationData.put("Status", medicationRequest.hasStatus() ? medicationRequest.getStatus().toCode() : "N/A");

        if (medicationRequest.hasMedication() && medicationRequest.getMedication().getConcept().getCodingFirstRep() != null) {
            Coding medicationCoding = medicationRequest.getMedication().getConcept().getCodingFirstRep();
            medicationData.put("MedicationCode", medicationCoding.hasCode() ? medicationCoding.getCode() : "N/A");
            medicationData.put("MedicationDisplay", medicationCoding.hasDisplay() ? medicationCoding.getDisplay() : "N/A");
        } else {
            medicationData.put("MedicationCode", "N/A");
            medicationData.put("MedicationDisplay", "N/A");
        }

        medicationData.put("Patient", medicationRequest.hasSubject() ? medicationRequest.getSubject().getReference() : "N/A");
        medicationData.put("Encounter", medicationRequest.hasEncounter() ? medicationRequest.getEncounter().getReference() : "N/A");

        if (medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost") != null) {
            var extension = medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                medicationData.put("BaseCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue()));
            } else {
                medicationData.put("BaseCost", "N/A");
            }
        } else {
            medicationData.put("BaseCost", "N/A");
        }

        if (medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-cost") != null) {
            var extension = medicationRequest.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                medicationData.put("TotalCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue()));
            } else {
                medicationData.put("TotalCost", "N/A");
            }
        } else {
            medicationData.put("TotalCost", "N/A");
        }

        return medicationData;
    }
}
