package com.group01.dhsa.Model.FhirResources.Level4.Importer.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;
import com.group01.dhsa.Model.FhirResources.*;
import java.io.FileReader;
import java.io.Reader;

public class ProcedureImporter implements FhirResourceImporter {

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
    public void importCsvToFhir(String csvFilePath) {
        setFhirServerUrl();
        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Read CSV file
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .withFirstRecordAsHeader()
                    .parse(in);

            // Iterate over CSV records
            for (CSVRecord record : records) {
                Procedure procedure = new Procedure();

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        procedure.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping procedure.");
                        continue;
                    }
                }

                // Validate and set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        procedure.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping procedure.");
                        continue;
                    }
                }

                // Verify if Procedure already exists
                if (record.isMapped("CODE") && procedureExistsByCode(client, record.get("CODE"), record.get("PATIENT"))) {
                    System.out.println("Procedure with code " + record.get("CODE") + " already exists for patient " + record.get("PATIENT") + ". Skipping.");
                    continue;
                }

                // Set Occurrence Date
                if (record.isMapped("DATE") && !record.get("DATE").isEmpty()) {
                    procedure.setOccurrence(new DateTimeType(record.get("DATE")));
                }

                // Set Code and Description
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    procedure.setCode(new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION"))));
                }

                // Set Reason Codes
                if (record.isMapped("REASONCODE") && !record.get("REASONCODE").isEmpty()) {
                    CodeableConcept reasonConcept = new CodeableConcept().addCoding(new Coding()
                            .setSystem("http://hl7.org/fhir/ValueSet/condition-code")
                            .setCode(record.get("REASONCODE"))
                            .setDisplay(record.get("REASONDESCRIPTION")));
                    procedure.addReason(new CodeableReference(reasonConcept));
                }

                // Add Base Cost as an Extension
                if (record.isMapped("BASE_COST") && !record.get("BASE_COST").isEmpty()) {
                    procedure.addExtension("http://hl7.org/fhir/StructureDefinition/base-cost",
                            new Quantity().setValue(Double.parseDouble(record.get("BASE_COST"))));
                }

                // Set Status to completed using Enumerations.EventStatus
                procedure.setStatus(Enumerations.EventStatus.COMPLETED);

                // Send Procedure to FHIR server
                client.create().resource(procedure).execute();

                // Log success
                System.out.println("Procedure for patient " + record.get("PATIENT") + " uploaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates if a Patient exists on the FHIR server by identifier.
     */
    private boolean patientExistsByIdentifier(IGenericClient client, String patientIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Patient")
                    .where(Patient.IDENTIFIER.exactly().identifier(patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Patient by identifier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if an Encounter exists on the FHIR server by identifier.
     */
    private boolean encounterExistsByIdentifier(IGenericClient client, String encounterIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Encounter")
                    .where(Encounter.IDENTIFIER.exactly().identifier(encounterIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Encounter by identifier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if a Procedure exists on the FHIR server by code and patient.
     */
    private boolean procedureExistsByCode(IGenericClient client, String code, String patientIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Procedure")
                    .where(Procedure.CODE.exactly().code(code))
                    .and(Procedure.SUBJECT.hasId("Patient?identifier=" + patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Procedure by code: " + e.getMessage());
            return false;
        }
    }
}
