package com.group01.dhsa.Model.FhirResources.Level5.Exporter.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcedureExporter implements FhirResourceExporter {

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
        List<Map<String, String>> proceduresList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Procedure resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Procedure.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Procedure procedure = (Procedure) entry.getResource();
                proceduresList.add(extractProcedureData(procedure));
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return proceduresList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> proceduresList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for all Procedure resources
            Bundle bundle = client.search()
                    .forResource(Procedure.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Procedure procedure = (Procedure) entry.getResource();

                    // Check if the search term matches any relevant field
                    if (matchesProcedure(procedure, searchTerm)) {
                        proceduresList.add(extractProcedureData(procedure));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Procedure resources: " + e.getMessage());
            e.printStackTrace();
        }

        return proceduresList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchField, String searchValue) {
        return List.of();
    }


    private boolean matchesProcedure(Procedure procedure, String searchTerm) {
        setFhirServerUrl();
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        // Match Patient reference
        if (procedure.hasSubject() && procedure.getSubject().getReference() != null
                && procedure.getSubject().getReference().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Code
        if (procedure.hasCode() && procedure.getCode().getCodingFirstRep() != null
                && procedure.getCode().getCodingFirstRep().getCode() != null
                && procedure.getCode().getCodingFirstRep().getCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Description
        if (procedure.hasCode() && procedure.getCode().getCodingFirstRep() != null
                && procedure.getCode().getCodingFirstRep().getDisplay() != null
                && procedure.getCode().getCodingFirstRep().getDisplay().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Date
        if (procedure.hasOccurrenceDateTimeType() && procedure.getOccurrenceDateTimeType().getValueAsString() != null
                && procedure.getOccurrenceDateTimeType().getValueAsString().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        // Match Reason Codes
        if (!procedure.getReason().isEmpty() && procedure.getReasonFirstRep().hasConcept()) {
            if (procedure.getReasonFirstRep().getConcept().getCodingFirstRep() != null
                    && procedure.getReasonFirstRep().getConcept().getCodingFirstRep().getCode() != null
                    && procedure.getReasonFirstRep().getConcept().getCodingFirstRep().getCode().toLowerCase().contains(lowerCaseSearchTerm)) {
                return true;
            }
            if (procedure.getReasonFirstRep().getConcept().getText() != null
                    && procedure.getReasonFirstRep().getConcept().getText().toLowerCase().contains(lowerCaseSearchTerm)) {
                return true;
            }
        }

        // Match Base Cost
        if (procedure.hasExtension("http://hl7.org/fhir/StructureDefinition/base-cost")) {
            String baseCost = procedure.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue() != null
                    ? procedure.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost").getValue().toString()
                    : null;
            if (baseCost != null && baseCost.toLowerCase().contains(lowerCaseSearchTerm)) {
                return true;
            }
        }

        // Match Status
        if (procedure.hasStatus() && procedure.getStatus().toCode() != null
                && procedure.getStatus().toCode().toLowerCase().contains(lowerCaseSearchTerm)) {
            return true;
        }

        return false;
    }


    private Map<String, String> extractProcedureData(Procedure procedure) {
        setFhirServerUrl();
        Map<String, String> procedureData = new HashMap<>();
        procedureData.put("Id", procedure.getIdentifierFirstRep() != null ? procedure.getIdentifierFirstRep().getValue() : "N/A");
        procedureData.put("Patient", procedure.hasSubject() ? procedure.getSubject().getReference() : "N/A");
        procedureData.put("Encounter", procedure.hasEncounter() ? procedure.getEncounter().getReference() : "N/A");
        procedureData.put("Date", procedure.hasOccurrenceDateTimeType() ? procedure.getOccurrenceDateTimeType().getValueAsString() : "N/A");

        if (procedure.hasCode() && !procedure.getCode().getCoding().isEmpty()) {
            Coding coding = procedure.getCode().getCodingFirstRep();
            procedureData.put("Code", coding.hasCode() ? coding.getCode() : "N/A");
            procedureData.put("Description", coding.hasDisplay() ? coding.getDisplay() : "N/A");
        } else {
            procedureData.put("Code", "N/A");
            procedureData.put("Description", "N/A");
        }

        if (!procedure.getReason().isEmpty() && procedure.getReasonFirstRep().hasConcept()) {
            procedureData.put("ReasonCode", procedure.getReasonFirstRep().getConcept().hasCoding() ? procedure.getReasonFirstRep().getConcept().getCodingFirstRep().getCode() : "N/A");
            procedureData.put("ReasonDescription", procedure.getReasonFirstRep().getConcept().hasText() ? procedure.getReasonFirstRep().getConcept().getText() : "N/A");
        } else {
            procedureData.put("ReasonCode", "N/A");
            procedureData.put("ReasonDescription", "N/A");
        }

        if (procedure.hasExtension("http://hl7.org/fhir/StructureDefinition/base-cost")) {
            // Ottieni l'estensione specifica
            var baseCostExtension = procedure.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/base-cost");
            if (baseCostExtension != null && baseCostExtension.getValue() instanceof org.hl7.fhir.r5.model.Quantity) {
                // Converte il valore in Quantity e prende il valore numerico
                org.hl7.fhir.r5.model.Quantity baseCostQuantity = (org.hl7.fhir.r5.model.Quantity) baseCostExtension.getValue();
                procedureData.put("BaseCost", String.valueOf(baseCostQuantity.getValue()));
            } else {
                procedureData.put("BaseCost", "N/A");
            }
        } else {
            procedureData.put("BaseCost", "N/A");
        }


        procedureData.put("Status", procedure.hasStatus() ? procedure.getStatus().toCode() : "N/A");

        return procedureData;
    }
}
