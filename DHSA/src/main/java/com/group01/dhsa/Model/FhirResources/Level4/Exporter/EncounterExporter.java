package com.group01.dhsa.Model.FhirResources.Level4.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.CodeableConcept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncounterExporter implements FhirResourceExporter {

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
        List<Map<String, String>> encountersList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Encounter resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Encounter.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Encounter encounter = (Encounter) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> encounterData = new HashMap<>();

                // Extract Encounter ID
                encounterData.put("Id", encounter.getIdentifierFirstRep() != null ? encounter.getIdentifierFirstRep().getValue() : "N/A");


                // Extract Type
                if (!encounter.getType().isEmpty()) {
                    CodeableConcept type = encounter.getType().get(0); // Primo elemento
                    if (!type.getCoding().isEmpty()) {
                        Coding coding = type.getCoding().get(0); // Primo Coding
                        encounterData.put("Type", coding.getDisplay() != null ? coding.getDisplay() : "N/A");
                    } else {
                        encounterData.put("Type", "N/A");
                    }
                } else {
                    encounterData.put("Type", "N/A");
                }

                // Extract Start and End Dates
                if (encounter.hasActualPeriod()) {
                    encounterData.put("Start", encounter.getActualPeriod().hasStart() ? encounter.getActualPeriod().getStart().toString() : "N/A");
                    encounterData.put("End", encounter.getActualPeriod().hasEnd() ? encounter.getActualPeriod().getEnd().toString() : "N/A");
                } else {
                    encounterData.put("Start", "N/A");
                    encounterData.put("End", "N/A");
                }

                // Extract Subject (Patient Reference)
                if (encounter.hasSubject()) {
                    encounterData.put("Patient", encounter.getSubject().getReference());
                } else {
                    encounterData.put("Patient", "N/A");
                }

                // Extract Service Provider (Organization Reference)
                if (encounter.hasServiceProvider()) {
                    encounterData.put("Organization", encounter.getServiceProvider().getReference());
                } else {
                    encounterData.put("Organization", "N/A");
                }

                // Extract Total Cost
                if (encounter.hasExtension("http://hl7.org/fhir/StructureDefinition/total-claim-cost")) {
                    var extension = encounter.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-claim-cost").getValue();
                    if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                        encounterData.put("TotalCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue()));
                    } else {
                        encounterData.put("TotalCost", "N/A");
                    }
                } else {
                    encounterData.put("TotalCost", "N/A");
                }


                // Add encounter data to the list
                encountersList.add(encounterData);
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return encountersList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> encountersList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Encounter resources
            Bundle bundle = client.search()
                    .forResource(Encounter.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Encounter encounter = (Encounter) entry.getResource();

                    // Check if the search term matches any relevant field
                    boolean matches = false;

                    // Match Id
                    if (!matches && encounter.getIdElement().getIdPart().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Type
                    if (!matches && !encounter.getType().isEmpty()) {
                        CodeableConcept type = encounter.getType().get(0);
                        if (type.hasCoding() && type.getCodingFirstRep().hasDisplay() &&
                                type.getCodingFirstRep().getDisplay().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                    }

                    // Match Start Date
                    if (!matches && encounter.hasActualPeriod() && encounter.getActualPeriod().hasStart() &&
                            encounter.getActualPeriod().getStart().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match End Date
                    if (!matches && encounter.hasActualPeriod() && encounter.getActualPeriod().hasEnd() &&
                            encounter.getActualPeriod().getEnd().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Patient reference
                    if (!matches && encounter.hasSubject() && encounter.getSubject().getReference().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Service Provider (Organization)
                    if (!matches && encounter.hasServiceProvider() && encounter.getServiceProvider().getReference().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Total Cost
                    if (!matches && encounter.hasExtension("http://hl7.org/fhir/StructureDefinition/total-claim-cost")) {
                        var extension = encounter.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-claim-cost").getValue();
                        if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                            String totalCost = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                            if (totalCost.toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                        }
                    }

                    // Add matching encounter to the result list
                    if (matches) {
                        Map<String, String> encounterData = new HashMap<>();

                        // Extract Encounter ID
                        encounterData.put("Id", encounter.getIdentifierFirstRep() != null ? encounter.getIdentifierFirstRep().getValue() : "N/A");

                        // Extract Type
                        if (!encounter.getType().isEmpty()) {
                            CodeableConcept type = encounter.getType().get(0); // First type
                            if (!type.getCoding().isEmpty()) {
                                Coding coding = type.getCoding().get(0); // First Coding
                                encounterData.put("Type", coding.getDisplay() != null ? coding.getDisplay() : "N/A");
                            } else {
                                encounterData.put("Type", "N/A");
                            }
                        } else {
                            encounterData.put("Type", "N/A");
                        }

                        // Extract Start and End Dates
                        if (encounter.hasActualPeriod()) {
                            encounterData.put("Start", encounter.getActualPeriod().hasStart() ? encounter.getActualPeriod().getStart().toString() : "N/A");
                            encounterData.put("End", encounter.getActualPeriod().hasEnd() ? encounter.getActualPeriod().getEnd().toString() : "N/A");
                        } else {
                            encounterData.put("Start", "N/A");
                            encounterData.put("End", "N/A");
                        }

                        // Extract Subject (Patient Reference)
                        encounterData.put("Patient", encounter.hasSubject() ? encounter.getSubject().getReference() : "N/A");

                        // Extract Service Provider (Organization Reference)
                        encounterData.put("Organization", encounter.hasServiceProvider() ? encounter.getServiceProvider().getReference() : "N/A");

                        // Extract Total Cost
                        if (encounter.hasExtension("http://hl7.org/fhir/StructureDefinition/total-claim-cost")) {
                            var extension = encounter.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/total-claim-cost").getValue();
                            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                                encounterData.put("TotalCost", String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue()));
                            } else {
                                encounterData.put("TotalCost", "N/A");
                            }
                        } else {
                            encounterData.put("TotalCost", "N/A");
                        }

                        // Add encounter data to the list
                        encountersList.add(encounterData);
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Encounter resources: " + e.getMessage());
            e.printStackTrace();
        }

        return encountersList;
    }



}
