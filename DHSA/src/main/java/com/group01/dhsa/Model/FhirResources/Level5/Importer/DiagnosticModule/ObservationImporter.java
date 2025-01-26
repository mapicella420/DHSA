package com.group01.dhsa.Model.FhirResources.Level5.Importer.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;
import com.group01.dhsa.Model.FhirResources.*;
import java.io.FileReader;
import java.io.Reader;

public class ObservationImporter implements FhirResourceImporter {

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
                Observation observation = new Observation();

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        observation.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping observation.");
                        continue;
                    }
                }

                // Validate and set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        observation.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping observation.");
                        continue;
                    }
                }

                // Verify if Observation already exists
                if (record.isMapped("CODE") && record.isMapped("DATE")
                        && observationExistsByCodeAndDate(client, record.get("CODE"), record.get("DATE"), record.get("PATIENT"))) {
                    System.out.println("Observation with code " + record.get("CODE") + " for patient " + record.get("PATIENT") + " on date " + record.get("DATE") + " already exists. Skipping.");
                    continue;
                }

                // Set Effective Date (Observation Date)
                if (record.isMapped("DATE") && !record.get("DATE").isEmpty()) {
                    observation.setEffective(new DateTimeType(record.get("DATE")));
                }

                // Set Code and Description
                CodeableConcept codeableConcept = new CodeableConcept();
                if (record.isMapped("DESCRIPTION") && !record.get("DESCRIPTION").isEmpty()) {
                    codeableConcept.setText(record.get("DESCRIPTION"));
                }
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    Coding coding = new Coding().setCode(record.get("CODE"));
                    codeableConcept.addCoding(coding);
                }
                observation.setCode(codeableConcept);

                // Set Value (numeric)
                if (record.isMapped("VALUE") && !record.get("VALUE").isEmpty()) {
                    String value = record.get("VALUE");
                    try {
                        double numericValue = Double.parseDouble(value);
                        Quantity valueQuantity = new Quantity()
                                .setValue(numericValue)
                                .setUnit(record.get("UNITS"));
                        observation.setValue(valueQuantity);
                    } catch (NumberFormatException e) {
                        System.out.println("Non-numeric value in VALUE field: " + value);
                    }
                }

                // Set Status to "final" (default)
                observation.setStatus(Enumerations.ObservationStatus.FINAL);

                // Send Observation to FHIR server
                client.create().resource(observation).execute();

                // Log success
                System.out.println("Observation for patient " + record.get("PATIENT") + " uploaded successfully.");
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
     * Validates if an Observation exists on the FHIR server by code, date, and patient.
     */
    private boolean observationExistsByCodeAndDate(IGenericClient client, String code, String date, String patientIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Observation")
                    .where(Observation.CODE.exactly().code(code))
                    .and(Observation.DATE.exactly().day(date))
                    .and(Observation.SUBJECT.hasChainedProperty(Patient.IDENTIFIER.exactly().identifier(patientIdentifier)))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Observation by code and date: " + e.getMessage());
            return false;
        }
    }

}
