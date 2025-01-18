package com.group01.dhsa.Model.FhirResources.level5.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;

public class AllergyImporter implements FhirResourceImporter {

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
                AllergyIntolerance allergy = new AllergyIntolerance();

                // Set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        allergy.setPatient(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping allergy.");
                        continue;
                    }
                }

                // Set Encounter (if available)
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        allergy.addExtension("http://hl7.org/fhir/StructureDefinition/encounter-reference",
                                new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping allergy.");
                        continue;
                    }
                }

                // Set Allergy Start Date
                if (record.isMapped("START") && !record.get("START").isEmpty()) {
                    allergy.setRecordedDateElement(new DateTimeType(record.get("START")));
                }

                // Set Allergy End Date (if available)
                if (record.isMapped("STOP") && !record.get("STOP").isEmpty()) {
                    allergy.addExtension("http://hl7.org/fhir/StructureDefinition/allergy-end-date",
                            new DateTimeType(record.get("STOP")));
                }

                // Set Allergy Code and Description
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    CodeableConcept allergyCode = new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION")));
                    allergy.setCode(allergyCode);
                }

                // Set Clinical Status (default to active if not provided)
                allergy.setClinicalStatus(new CodeableConcept().addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical")
                        .setCode("active")));

                // Set Verification Status
                allergy.setVerificationStatus(new CodeableConcept().addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification")
                        .setCode("confirmed")));

                // Send AllergyIntolerance to FHIR server
                client.create().resource(allergy).execute();

                // Log success
                System.out.println("Allergy for Patient with ID " + record.get("PATIENT") + " uploaded successfully.");
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
}
