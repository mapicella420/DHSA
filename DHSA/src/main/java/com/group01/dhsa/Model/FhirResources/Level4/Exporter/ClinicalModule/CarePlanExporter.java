package com.group01.dhsa.Model.FhirResources.Level4.Exporter.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarePlanExporter implements FhirResourceExporter {

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
        List<Map<String, String>> carePlansList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all CarePlan resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(CarePlan.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                CarePlan carePlan = (CarePlan) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> carePlanData = new HashMap<>();
                carePlanData.put("Id", carePlan.getIdentifierFirstRep() != null ? carePlan.getIdentifierFirstRep().getValue() : "N/A");

                // Extract Patient reference
                carePlanData.put("Patient", carePlan.hasSubject() ? carePlan.getSubject().getReference() : "N/A");

                // Extract Encounter reference
                carePlanData.put("Encounter", carePlan.hasEncounter() ? carePlan.getEncounter().getReference() : "N/A");

                // Extract Period (Start and Stop)
                if (carePlan.hasPeriod()) {
                    Period period = carePlan.getPeriod();
                    carePlanData.put("Start", period.hasStart() ? period.getStart().toString() : "N/A");
                    carePlanData.put("Stop", period.hasEnd() ? period.getEnd().toString() : "N/A");
                } else {
                    carePlanData.put("Start", "N/A");
                    carePlanData.put("Stop", "N/A");
                }

                // Extract Categories (Code and Description)
                if (!carePlan.getCategory().isEmpty()) {
                    CodeableConcept category = carePlan.getCategoryFirstRep();
                    carePlanData.put("Code", category.hasCoding() && category.getCodingFirstRep().hasCode() ? category.getCodingFirstRep().getCode() : "N/A");
                    carePlanData.put("Description", category.hasCoding() && category.getCodingFirstRep().hasDisplay() ? category.getCodingFirstRep().getDisplay() : "N/A");
                } else {
                    carePlanData.put("Code", "N/A");
                    carePlanData.put("Description", "N/A");
                }

                // Extract Reason Codes
                if (!carePlan.getAddresses().isEmpty()) {
                    CodeableConcept reasonConcept = carePlan.getAddressesFirstRep().getConcept();
                    if (reasonConcept != null) {
                        carePlanData.put("ReasonCode", reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasCode() ? reasonConcept.getCodingFirstRep().getCode() : "N/A");
                        carePlanData.put("ReasonDescription", reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasDisplay() ? reasonConcept.getCodingFirstRep().getDisplay() : "N/A");
                    }
                } else {
                    carePlanData.put("ReasonCode", "N/A");
                    carePlanData.put("ReasonDescription", "N/A");
                }

                // Extract Status
                carePlanData.put("Status", carePlan.hasStatus() ? carePlan.getStatus().toCode() : "N/A");

                // Add care plan data to the list
                carePlansList.add(carePlanData);
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return carePlansList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> carePlansList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform initial search query for CarePlan resources
            Bundle bundle = client.search()
                    .forResource(CarePlan.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    CarePlan carePlan = (CarePlan) entry.getResource();

                    // Check if the search term matches any relevant field
                    boolean matches = false;

                    // Match Id
                    if (!matches && carePlan.getIdElement().getIdPart().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Patient reference
                    if (!matches && carePlan.hasSubject() && carePlan.getSubject().getReference().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Encounter reference
                    if (!matches && carePlan.hasEncounter() && carePlan.getEncounter().getReference().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Period (Start)
                    if (!matches && carePlan.hasPeriod() && carePlan.getPeriod().hasStart() && carePlan.getPeriod().getStart().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Period (Stop)
                    if (!matches && carePlan.hasPeriod() && carePlan.getPeriod().hasEnd() && carePlan.getPeriod().getEnd().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Category Code or Description
                    if (!matches && carePlan.hasCategory() && !carePlan.getCategory().isEmpty()) {
                        CodeableConcept category = carePlan.getCategoryFirstRep();
                        if (category.hasCoding() && category.getCodingFirstRep().hasCode() &&
                                category.getCodingFirstRep().getCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                        if (category.hasCoding() && category.getCodingFirstRep().hasDisplay() &&
                                category.getCodingFirstRep().getDisplay().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                    }

                    // Match Reason Code or Description
                    if (!matches && !carePlan.getAddresses().isEmpty()) {
                        CodeableConcept reasonConcept = carePlan.getAddressesFirstRep().getConcept();
                        if (reasonConcept != null) {
                            if (reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasCode() &&
                                    reasonConcept.getCodingFirstRep().getCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                            if (reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasDisplay() &&
                                    reasonConcept.getCodingFirstRep().getDisplay().toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                        }
                    }

                    // Match Status
                    if (!matches && carePlan.hasStatus() && carePlan.getStatus().toCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Add matching care plan to the result list
                    if (matches) {
                        Map<String, String> carePlanData = new HashMap<>();
                        carePlanData.put("Id", carePlan.getIdentifierFirstRep() != null ? carePlan.getIdentifierFirstRep().getValue() : "N/A");

                        carePlanData.put("Patient", carePlan.hasSubject() ? carePlan.getSubject().getReference() : "N/A");
                        carePlanData.put("Encounter", carePlan.hasEncounter() ? carePlan.getEncounter().getReference() : "N/A");

                        if (carePlan.hasPeriod()) {
                            Period period = carePlan.getPeriod();
                            carePlanData.put("Start", period.hasStart() ? period.getStart().toString() : "N/A");
                            carePlanData.put("Stop", period.hasEnd() ? period.getEnd().toString() : "N/A");
                        } else {
                            carePlanData.put("Start", "N/A");
                            carePlanData.put("Stop", "N/A");
                        }

                        if (!carePlan.getCategory().isEmpty()) {
                            CodeableConcept category = carePlan.getCategoryFirstRep();
                            carePlanData.put("Code", category.hasCoding() && category.getCodingFirstRep().hasCode() ? category.getCodingFirstRep().getCode() : "N/A");
                            carePlanData.put("Description", category.hasCoding() && category.getCodingFirstRep().hasDisplay() ? category.getCodingFirstRep().getDisplay() : "N/A");
                        } else {
                            carePlanData.put("Code", "N/A");
                            carePlanData.put("Description", "N/A");
                        }

                        if (!carePlan.getAddresses().isEmpty()) {
                            CodeableConcept reasonConcept = carePlan.getAddressesFirstRep().getConcept();
                            if (reasonConcept != null) {
                                carePlanData.put("ReasonCode", reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasCode() ? reasonConcept.getCodingFirstRep().getCode() : "N/A");
                                carePlanData.put("ReasonDescription", reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasDisplay() ? reasonConcept.getCodingFirstRep().getDisplay() : "N/A");
                            }
                        } else {
                            carePlanData.put("ReasonCode", "N/A");
                            carePlanData.put("ReasonDescription", "N/A");
                        }

                        carePlanData.put("Status", carePlan.hasStatus() ? carePlan.getStatus().toCode() : "N/A");

                        // Add care plan data to the list
                        carePlansList.add(carePlanData);
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching CarePlan resources: " + e.getMessage());
            e.printStackTrace();
        }

        return carePlansList;
    }

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> carePlansList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(CarePlan.class);

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
                    case "category":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("category").exactly().code(searchValue));
                        break;
                    case "start":
                        query = query.where(new ca.uhn.fhir.rest.gclient.DateClientParam("period-start").exactly().day(searchValue));
                        break;
                    case "stop":
                        query = query.where(new ca.uhn.fhir.rest.gclient.DateClientParam("period-end").exactly().day(searchValue));
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
                    CarePlan carePlan = (CarePlan) entry.getResource();
                    carePlansList.add(convertResourceToMap(carePlan));
                }

                // Retrieve the next page
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching CarePlan resources: " + e.getMessage());
            e.printStackTrace();
        }

        return carePlansList;
    }

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof CarePlan)) {
            throw new IllegalArgumentException("Resource is not of type CarePlan");
        }

        CarePlan carePlan = (CarePlan) resource;
        Map<String, String> carePlanData = new HashMap<>();

        // ID
        carePlanData.put("Id", carePlan.getIdElement().getIdPart());

        // Meta
        if (carePlan.hasMeta()) {
            carePlanData.put("VersionId", carePlan.getMeta().hasVersionId() ? carePlan.getMeta().getVersionId() : "N/A");
            carePlanData.put("LastUpdated", carePlan.getMeta().hasLastUpdated() ? carePlan.getMeta().getLastUpdated().toString() : "N/A");
            carePlanData.put("Source", carePlan.getMeta().hasSource() ? carePlan.getMeta().getSource() : "N/A");
        } else {
            carePlanData.put("VersionId", "N/A");
            carePlanData.put("LastUpdated", "N/A");
            carePlanData.put("Source", "N/A");
        }

        // Identifier
        if (carePlan.hasIdentifier() && !carePlan.getIdentifier().isEmpty()) {
            carePlanData.put("Identifier", carePlan.getIdentifierFirstRep().getValue());
        } else {
            carePlanData.put("Identifier", "N/A");
        }

        // Status
        carePlanData.put("Status", carePlan.hasStatus() ? carePlan.getStatus().toCode() : "N/A");

        // Category
        if (!carePlan.getCategory().isEmpty()) {
            CodeableConcept category = carePlan.getCategoryFirstRep();
            carePlanData.put("CategoryCode", category.hasCoding() && category.getCodingFirstRep().hasCode() ? category.getCodingFirstRep().getCode() : "N/A");
            carePlanData.put("CategoryDescription", category.hasCoding() && category.getCodingFirstRep().hasDisplay() ? category.getCodingFirstRep().getDisplay() : "N/A");
        } else {
            carePlanData.put("CategoryCode", "N/A");
            carePlanData.put("CategoryDescription", "N/A");
        }

        // Subject
        carePlanData.put("Patient", carePlan.hasSubject() ? carePlan.getSubject().getReference() : "N/A");

        // Encounter
        carePlanData.put("Encounter", carePlan.hasEncounter() ? carePlan.getEncounter().getReference() : "N/A");

        // Period
        if (carePlan.hasPeriod()) {
            carePlanData.put("Start", carePlan.getPeriod().hasStart() ? carePlan.getPeriod().getStart().toString() : "N/A");
            carePlanData.put("Stop", carePlan.getPeriod().hasEnd() ? carePlan.getPeriod().getEnd().toString() : "N/A");
        } else {
            carePlanData.put("Start", "N/A");
            carePlanData.put("Stop", "N/A");
        }

        // Addresses (Reason Codes)
        if (!carePlan.getAddresses().isEmpty()) {
            CodeableConcept reasonConcept = carePlan.getAddressesFirstRep().getConcept();
            if (reasonConcept != null) {
                carePlanData.put("ReasonCode", reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasCode() ? reasonConcept.getCodingFirstRep().getCode() : "N/A");
                carePlanData.put("ReasonDescription", reasonConcept.hasCoding() && reasonConcept.getCodingFirstRep().hasDisplay() ? reasonConcept.getCodingFirstRep().getDisplay() : "N/A");
            } else {
                carePlanData.put("ReasonCode", "N/A");
                carePlanData.put("ReasonDescription", "N/A");
            }
        } else {
            carePlanData.put("ReasonCode", "N/A");
            carePlanData.put("ReasonDescription", "N/A");
        }

        return carePlanData;
    }



}
