package com.group01.dhsa.Model.FhirResources.Level5.Importer.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;
import com.group01.dhsa.Model.FhirResources.*;
import java.io.FileReader;
import java.io.Reader;

public class ConditionImporter implements FhirResourceImporter {

    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public void importCsvToFhir(String csvFilePath) {
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
                Condition condition = new Condition();

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        condition.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping condition.");
                        continue;
                    }
                }

                // Validate and set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        condition.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping condition.");
                        continue;
                    }
                }

                // Verify if Condition already exists
                if (record.isMapped("CODE") && conditionExistsByCode(client, record.get("CODE"), record.get("PATIENT"))) {
                    System.out.println("Condition with code " + record.get("CODE") + " already exists for patient " + record.get("PATIENT") + ". Skipping.");
                    continue;
                }

                // Set Onset Date
                if (record.isMapped("START") && !record.get("START").isEmpty()) {
                    condition.setOnset(new DateTimeType(record.get("START")));
                }

                // Set Abatement Date
                if (record.isMapped("STOP") && !record.get("STOP").isEmpty()) {
                    condition.setAbatement(new DateTimeType(record.get("STOP")));
                }

                // Set Code and Description
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    condition.setCode(new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION"))));
                }

                // Set Clinical Status to active (default)
                condition.setClinicalStatus(new CodeableConcept().addCoding(new Coding()
                        .setSystem("http://hl7.org/fhir/ValueSet/condition-clinical")
                        .setCode("active")));

                // Set Verification Status to confirmed (default)
                condition.setVerificationStatus(new CodeableConcept().addCoding(new Coding()
                        .setSystem("http://hl7.org/fhir/ValueSet/condition-verification")
                        .setCode("confirmed")));

                // Send Condition to FHIR server
                client.create().resource(condition).execute();

                // Log success
                System.out.println("Condition for patient " + record.get("PATIENT") + " with code " + record.get("CODE") + " uploaded successfully.");
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
     * Validates if a Condition exists on the FHIR server by code and patient.
     */
    private boolean conditionExistsByCode(IGenericClient client, String code, String patientIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Condition")
                    .where(Condition.CODE.exactly().code(code))
                    .and(Condition.SUBJECT.hasId("Patient?identifier=" + patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Condition by code: " + e.getMessage());
            return false;
        }
    }
}
