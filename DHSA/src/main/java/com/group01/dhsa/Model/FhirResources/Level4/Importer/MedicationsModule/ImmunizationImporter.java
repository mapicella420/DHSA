package com.group01.dhsa.Model.FhirResources.Level4.Importer.MedicationsModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import com.group01.dhsa.Model.FhirResources.*;
import com.group01.dhsa.Controller.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;

public class ImmunizationImporter implements FhirResourceImporter {

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
                String patientIdentifier = record.get("PATIENT");
                String encounterIdentifier = record.get("ENCOUNTER");
                String immunizationCode = record.get("CODE");
                String immunizationDate = record.get("DATE");

                // Check if the Immunization already exists
                if (immunizationExists(client, patientIdentifier, immunizationCode, immunizationDate)) {
                    System.out.println("Immunization with code " + immunizationCode + " for patient " + patientIdentifier + " on date " + immunizationDate + " already exists. Skipping.");
                    continue;
                }

                Immunization immunization = new Immunization();

                // Set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        immunization.setPatient(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping immunization.");
                        continue;
                    }
                }

                // Set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        immunization.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping immunization.");
                        continue;
                    }
                }

                // Set Occurrence Date
                if (record.isMapped("DATE") && !record.get("DATE").isEmpty()) {
                    immunization.setOccurrence(new DateTimeType(record.get("DATE")));
                }

                // Set Vaccine Code
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    immunization.setVaccineCode(new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION"))));
                }

                // Add Base Cost as an Extension
                if (record.isMapped("BASE_COST") && !record.get("BASE_COST").isEmpty()) {
                    immunization.addExtension("http://hl7.org/fhir/StructureDefinition/base-cost",
                            new Quantity().setValue(Double.parseDouble(record.get("BASE_COST"))));
                }

                // Set Status (default to completed)
                immunization.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);

                // Send Immunization to FHIR server
                client.create().resource(immunization).execute();

                // Log success
                System.out.println("Immunization with code " + record.get("CODE") + " for patient " + patientIdentifier + " uploaded successfully.");
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
     * Validates if an Immunization exists on the FHIR server by patient, code, and date.
     */
    private boolean immunizationExists(IGenericClient client, String patientIdentifier, String code, String date) {
        try {
            var bundle = client.search()
                    .forResource("Immunization")
                    .where(Immunization.PATIENT.hasId("Patient?identifier=" + patientIdentifier))
                    .and(new TokenClientParam("vaccine-code").exactly().code(code)) // Usa il parametro corretto "vaccine-code"
                    .and(Immunization.DATE.exactly().day(date))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Immunization by patient, vaccine-code, and date: " + e.getMessage());
            return false;
        }
    }

}
