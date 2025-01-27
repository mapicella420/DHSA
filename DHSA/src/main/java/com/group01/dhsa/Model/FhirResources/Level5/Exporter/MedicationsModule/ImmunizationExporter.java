package com.group01.dhsa.Model.FhirResources.Level5.Exporter.MedicationsModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Immunization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exporter for Immunization resources.
 */
public class ImmunizationExporter implements FhirResourceExporter {

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
        List<Map<String, String>> immunizationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Immunization resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Immunization.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Immunization immunization = (Immunization) entry.getResource();

                // Extract relevant fields and store them in a Map
                immunizationsList.add(extractImmunizationData(immunization));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return immunizationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> immunizationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Immunization resources
            Bundle bundle = client.search()
                    .forResource(Immunization.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Immunization immunization = (Immunization) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesImmunization(immunization, searchTerm)) {
                        immunizationsList.add(extractImmunizationData(immunization));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Immunization resources: " + e.getMessage());
            e.printStackTrace();
        }

        return immunizationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchField, String searchValue) {
        return List.of();
    }

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Immunization)) {
            throw new IllegalArgumentException("Resource is not of type Immunization");
        }

        Immunization immunization = (Immunization) resource;
        Map<String, String> immunizationData = new HashMap<>();

        // Meta information
        if (immunization.hasMeta()) {
            immunizationData.put("VersionId", immunization.getMeta().hasVersionId() ? immunization.getMeta().getVersionId() : "N/A");
            immunizationData.put("LastUpdated", immunization.getMeta().hasLastUpdated() ? immunization.getMeta().getLastUpdated().toString() : "N/A");
            immunizationData.put("Source", immunization.getMeta().hasSource() ? immunization.getMeta().getSource() : "N/A");
        } else {
            immunizationData.put("VersionId", "N/A");
            immunizationData.put("LastUpdated", "N/A");
            immunizationData.put("Source", "N/A");
        }

        // Identifier
        immunizationData.put("Id", immunization.getIdElement().getIdPart());

        // Status
        immunizationData.put("Status", immunization.hasStatus() ? immunization.getStatus().toCode() : "N/A");

        // Vaccine Code
        if (immunization.hasVaccineCode() && immunization.getVaccineCode().getCodingFirstRep() != null) {
            Coding vaccineCoding = immunization.getVaccineCode().getCodingFirstRep();
            immunizationData.put("VaccineCode", vaccineCoding.hasCode() ? vaccineCoding.getCode() : "N/A");
            immunizationData.put("VaccineDisplay", vaccineCoding.hasDisplay() ? vaccineCoding.getDisplay() : "N/A");
        } else {
            immunizationData.put("VaccineCode", "N/A");
            immunizationData.put("VaccineDisplay", "N/A");
        }

        // Patient reference
        immunizationData.put("Patient", immunization.hasPatient() ? immunization.getPatient().getReference() : "N/A");

        // Encounter reference
        immunizationData.put("Encounter", immunization.hasEncounter() ? immunization.getEncounter().getReference() : "N/A");

        // Occurrence DateTime
        immunizationData.put("OccurrenceDateTime", immunization.hasOccurrenceDateTimeType() ? immunization.getOccurrenceDateTimeType().getValueAsString() : "N/A");

        // Base Cost
        if (immunization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost") != null) {
            var extension = immunization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                immunizationData.put("BaseCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue()));
            } else {
                immunizationData.put("BaseCost", "N/A");
            }
        } else {
            immunizationData.put("BaseCost", "N/A");
        }

        return immunizationData;
    }


    private boolean matchesImmunization(Immunization immunization, String searchTerm) {
        setFhirServerUrl();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Match Id
        if (immunization.getIdElement().getIdPart().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Status
        if (immunization.hasStatus() && immunization.getStatus().toCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Vaccine Code
        if (immunization.hasVaccineCode() && immunization.getVaccineCode().getCodingFirstRep().hasDisplay() &&
                immunization.getVaccineCode().getCodingFirstRep().getDisplay().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Patient
        if (immunization.hasPatient() && immunization.getPatient().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Encounter
        if (immunization.hasEncounter() && immunization.getEncounter().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Occurrence DateTime
        if (immunization.hasOccurrenceDateTimeType() &&
                immunization.getOccurrenceDateTimeType().getValueAsString().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Base Cost
        if (immunization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost") != null) {
            var extension = immunization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                String baseCost = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                if (baseCost.contains(searchTerm)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Map<String, String> extractImmunizationData(Immunization immunization) {
        setFhirServerUrl();
        Map<String, String> immunizationData = new HashMap<>();
        immunizationData.put("Id", immunization.getIdElement().getIdPart());
        immunizationData.put("Status", immunization.hasStatus() ? immunization.getStatus().toCode() : "N/A");

        if (immunization.hasVaccineCode() && immunization.getVaccineCode().getCodingFirstRep() != null) {
            Coding vaccineCoding = immunization.getVaccineCode().getCodingFirstRep();
            immunizationData.put("VaccineCode", vaccineCoding.hasCode() ? vaccineCoding.getCode() : "N/A");
            immunizationData.put("VaccineDisplay", vaccineCoding.hasDisplay() ? vaccineCoding.getDisplay() : "N/A");
        } else {
            immunizationData.put("VaccineCode", "N/A");
            immunizationData.put("VaccineDisplay", "N/A");
        }

        immunizationData.put("Patient", immunization.hasPatient() ? immunization.getPatient().getReference() : "N/A");
        immunizationData.put("Encounter", immunization.hasEncounter() ? immunization.getEncounter().getReference() : "N/A");
        immunizationData.put("OccurrenceDateTime", immunization.hasOccurrenceDateTimeType() ? immunization.getOccurrenceDateTimeType().getValueAsString() : "N/A");

        if (immunization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost") != null) {
            var extension = immunization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                immunizationData.put("BaseCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue()));
            } else {
                immunizationData.put("BaseCost", "N/A");
            }
        } else {
            immunizationData.put("BaseCost", "N/A");
        }

        return immunizationData;
    }
}
