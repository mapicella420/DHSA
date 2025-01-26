package com.group01.dhsa.Model.FhirResources.Level5.Exporter.MedicationsModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
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
    public List<Map<String, String>> searchResources(String searchField, String searchValue) {
        return List.of();
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
