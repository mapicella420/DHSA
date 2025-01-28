package com.group01.dhsa.Model.FhirResources.Level3.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Controller.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncounterExporter implements FhirResourceExporter {

    private static String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    private static void setFhirServerUrl() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")) {
                FHIR_SERVER_URL = "http://localhost:8081/fhir";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")) {
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
                    .count(1000)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Encounter encounter = (Encounter) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> encounterData = new HashMap<>();

                // Extract Encounter ID (from the 'id' field of the resource)
                encounterData.put("Encounter", encounter.getIdElement().getIdPart());

                // Extract Identifier
                encounterData.put("Identifier", encounter.getIdentifierFirstRep() != null ? encounter.getIdentifierFirstRep().getValue() : "N/A");

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

                String practitioner = "N/A";
                if (encounter.hasParticipant() && !encounter.getParticipant().isEmpty()) {
                    Encounter.EncounterParticipantComponent participant = encounter.getParticipantFirstRep();
                    if (participant.hasActor() && participant.getActor().getReference().startsWith("Practitioner")) {
                        practitioner = participant.getActor().getReference();
                    }
                }
                encounterData.put("Practitioner", practitioner);

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

                    // Match Id (from the 'id' field of the resource)
                    if (!matches && encounter.getIdElement().getIdPart().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Identifier
                    if (!matches && encounter.getIdentifierFirstRep() != null &&
                            encounter.getIdentifierFirstRep().getValue().toLowerCase().contains(searchTerm.toLowerCase())) {
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
                        encounterData.put("Encounter", encounter.getIdElement().getIdPart());

                        // Extract Identifier
                        encounterData.put("Identifier", encounter.getIdentifierFirstRep() != null ? encounter.getIdentifierFirstRep().getValue() : "N/A");

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

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> encountersList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the search query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(Encounter.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map search fields to FHIR API query parameters
                switch (searchField) {
                    case "encounter": // Search by Encounter ID
                        query = query.where(new StringClientParam("_id").matches().value(searchValue));
                        break;
                    case "identifier": // Search by Identifier
                        query = query.where(new StringClientParam("identifier").matches().value(searchValue));
                        break;
                    case "type": // Search by Type (Coding Display)
                        query = query.where(new StringClientParam("type").matches().value(searchValue));
                        break;
                    case "patient": // Search by Subject (Patient Reference)
                        query = query.where(new StringClientParam("subject").matches().value(searchValue));
                        break;
                    case "organization": // Search by Service Provider (Organization Reference)
                        query = query.where(new StringClientParam("service-provider").matches().value(searchValue));
                        break;
                    case "periodstart": // Search by Start Date
                        query = query.where(new StringClientParam("date").matches().value(searchValue));
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
                    Encounter encounter = (Encounter) entry.getResource();
                    Map<String, String> encounterData = convertResourceToMap(encounter);
                    encountersList.add(encounterData);
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

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Encounter)) {
            throw new IllegalArgumentException("Resource is not of type Encounter");
        }

        Encounter encounter = (Encounter) resource;
        Map<String, String> encounterData = new HashMap<>();

        // ID
        encounterData.put("Encounter", encounter.getIdElement().getIdPart());

        // Meta
        if (encounter.hasMeta()) {
            encounterData.put("VersionId", encounter.getMeta().hasVersionId() ? encounter.getMeta().getVersionId() : "N/A");
            encounterData.put("LastUpdated", encounter.getMeta().hasLastUpdated() ? encounter.getMeta().getLastUpdated().toString() : "N/A");
            encounterData.put("Source", encounter.getMeta().hasSource() ? encounter.getMeta().getSource() : "N/A");
        } else {
            encounterData.put("VersionId", "N/A");
            encounterData.put("LastUpdated", "N/A");
            encounterData.put("Source", "N/A");
        }

        // Extensions
        encounterData.put("BaseEncounterCost", getExtensionValue(encounter, "http://hl7.org/fhir/StructureDefinition/base-encounter-cost"));
        encounterData.put("TotalClaimCost", getExtensionValue(encounter, "http://hl7.org/fhir/StructureDefinition/total-claim-cost"));
        encounterData.put("PayerCoverage", getExtensionValue(encounter, "http://hl7.org/fhir/StructureDefinition/payer-coverage"));

        // Identifier
        encounterData.put("Identifier", encounter.hasIdentifier() && !encounter.getIdentifier().isEmpty()
                ? encounter.getIdentifierFirstRep().getValue() : "N/A");


        // Type
        if (!encounter.getType().isEmpty() && encounter.getTypeFirstRep().hasCoding()) {
            Coding typeCoding = encounter.getTypeFirstRep().getCodingFirstRep();
            encounterData.put("TypeCode", typeCoding.hasCode() ? typeCoding.getCode() : "N/A");
            encounterData.put("TypeDisplay", typeCoding.hasDisplay() ? typeCoding.getDisplay() : "N/A");
        } else {
            encounterData.put("TypeCode", "N/A");
            encounterData.put("TypeDisplay", "N/A");
        }

        // Subject
        encounterData.put("Patient", encounter.hasSubject() ? encounter.getSubject().getReference() : "N/A");

        // Service Provider
        encounterData.put("Organization", encounter.hasServiceProvider() ? encounter.getServiceProvider().getReference() : "N/A");

        // Participant (Practitioner)
        if (encounter.hasParticipant() && !encounter.getParticipant().isEmpty()) {
            Encounter.EncounterParticipantComponent participant = encounter.getParticipantFirstRep();
            if (participant.hasActor()) {
                encounterData.put("Practitioner", participant.getActor().getReference());
            } else {
                encounterData.put("Practitioner", "N/A");
            }
        } else {
            encounterData.put("Practitioner", "N/A");
        }

        // Actual Period
        if (encounter.hasActualPeriod()) {
            encounterData.put("Start", encounter.getActualPeriod().hasStart() ? encounter.getActualPeriod().getStartElement().toHumanDisplay() : "N/A");
            encounterData.put("End", encounter.getActualPeriod().hasEnd() ? encounter.getActualPeriod().getEndElement().toHumanDisplay() : "N/A");
        } else {
            encounterData.put("Start", "N/A");
            encounterData.put("End", "N/A");
        }

        return encounterData;
    }

    /**
     * Utility method to extract the value of an extension as a String.
     */
    private String getExtensionValue(Encounter encounter, String url) {
        if (encounter.hasExtension(url)) {
            var extension = encounter.getExtensionByUrl(url).getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                return String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
            }
        }
        return "N/A";
    }


}
