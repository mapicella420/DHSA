package com.group01.dhsa.Model.FhirResources.level5.MedicationsModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.Enumerations;

import java.io.FileReader;
import java.io.Reader;

public class MedicationImporter implements FhirResourceImporter {

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
                MedicationRequest medicationRequest = new MedicationRequest();

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        medicationRequest.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping medication request.");
                        continue;
                    }
                }

                // Validate and set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        medicationRequest.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping medication request.");
                        continue;
                    }
                }

                // Verify if MedicationRequest already exists
                if (record.isMapped("CODE") && medicationRequestExistsByCode(client, record.get("CODE"), record.get("PATIENT"))) {
                    System.out.println("MedicationRequest with code " + record.get("CODE") + " already exists for patient " + record.get("PATIENT") + ". Skipping.");
                    continue;
                }

                // Set Medication Code and Description
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    CodeableConcept medicationCode = new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setSystem("http://hl7.org/fhir/sid/ndc") // Update as needed
                            .setDisplay(record.get("DESCRIPTION")));
                    medicationRequest.setMedication(new CodeableReference().setConcept(medicationCode));
                }

                // Add Base Cost and Total Cost as Extensions
                if (record.isMapped("BASE_COST") && !record.get("BASE_COST").isEmpty()) {
                    medicationRequest.addExtension("http://hl7.org/fhir/StructureDefinition/base-cost",
                            new Quantity().setValue(Double.parseDouble(record.get("BASE_COST"))));
                }
                if (record.isMapped("TOTALCOST") && !record.get("TOTALCOST").isEmpty()) {
                    medicationRequest.addExtension("http://hl7.org/fhir/StructureDefinition/total-cost",
                            new Quantity().setValue(Double.parseDouble(record.get("TOTALCOST"))));
                }

                // Set Status to "active" (default)
                medicationRequest.setStatus(MedicationRequest.MedicationrequestStatus.fromCode("active"));

                // Send MedicationRequest to FHIR server
                client.create().resource(medicationRequest).execute();

                // Log success
                System.out.println("MedicationRequest with code " + record.get("CODE") + " uploaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    private boolean medicationRequestExistsByCode(IGenericClient client, String code, String patientIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("MedicationRequest")
                    .where(MedicationRequest.MEDICATION.hasChainedProperty(Medication.CODE.exactly().code("12345")))
                    .returnBundle(Bundle.class)
                    .execute();


            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking MedicationRequest by code: " + e.getMessage());
            return false;
        }
    }
}
