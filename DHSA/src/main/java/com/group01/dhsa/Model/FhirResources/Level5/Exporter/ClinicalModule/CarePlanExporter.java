package com.group01.dhsa.Model.FhirResources.Level5.Exporter.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarePlanExporter implements FhirResourceExporter {

    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public List<Map<String, String>> exportResources() {
        List<Map<String, String>> carePlansList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all CarePlan resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(CarePlan.class)
                    .returnBundle(Bundle.class)
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


}
