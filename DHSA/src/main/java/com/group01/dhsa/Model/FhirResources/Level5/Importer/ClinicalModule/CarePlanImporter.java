package com.group01.dhsa.Model.FhirResources.Level5.Importer.ClinicalModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.*;
import com.group01.dhsa.Model.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;

public class CarePlanImporter implements FhirResourceImporter {

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
                CarePlan carePlan = new CarePlan();

                // Set CarePlan ID
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    carePlan.addIdentifier().setValue(record.get("Id"));
                }

                // Set Identifier
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    carePlan.addIdentifier(new Identifier().setValue(record.get("Id")));
                }

                // Verify if CarePlan already exists
                if (carePlanExistsByIdentifier(client, record.get("Id"))) {
                    System.out.println("CarePlan with identifier " + record.get("Id") + " already exists. Skipping.");
                    continue;
                }

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        carePlan.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping care plan.");
                        continue;
                    }
                }

                // Validate and set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        carePlan.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping care plan.");
                        continue;
                    }
                }

                // Set Period (Start and Stop)
                Period period = new Period();
                if (record.isMapped("START") && !record.get("START").isEmpty()) {
                    period.setStartElement(new DateTimeType(record.get("START")));
                }
                if (record.isMapped("STOP") && !record.get("STOP").isEmpty()) {
                    period.setEndElement(new DateTimeType(record.get("STOP")));
                }
                carePlan.setPeriod(period);

                // Set Code and Description
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    carePlan.addCategory(new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION"))));
                }

                // Set Reason Codes
                if (record.isMapped("REASONCODE") && !record.get("REASONCODE").isEmpty()) {
                    CodeableConcept reasonConcept = new CodeableConcept().addCoding(new Coding()
                            .setSystem("http://hl7.org/fhir/ValueSet/condition-code")
                            .setCode(record.get("REASONCODE"))
                            .setDisplay(record.get("REASONDESCRIPTION")));
                    carePlan.addAddresses(new CodeableReference(reasonConcept));
                }

                // Set Status to active (default)
                carePlan.setStatus(Enumerations.RequestStatus.ACTIVE);

                // Send CarePlan to FHIR server
                client.create().resource(carePlan).execute();

                // Log success
                System.out.println("CarePlan with ID " + carePlan.getId() + " uploaded successfully.");
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
     * Validates if a CarePlan exists on the FHIR server by identifier.
     */
    private boolean carePlanExistsByIdentifier(IGenericClient client, String carePlanIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("CarePlan")
                    .where(CarePlan.IDENTIFIER.exactly().identifier(carePlanIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking CarePlan by identifier: " + e.getMessage());
            return false;
        }
    }
}
