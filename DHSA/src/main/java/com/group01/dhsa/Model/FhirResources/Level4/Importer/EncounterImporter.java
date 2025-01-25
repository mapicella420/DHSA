package com.group01.dhsa.Model.FhirResources.Level4.Importer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Model.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;
public class EncounterImporter implements FhirResourceImporter {

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
                String encounterId = record.get("Id");

                // Check if the Encounter already exists
                if (encounterExistsByIdentifier(client, encounterId)) {
                    System.out.println("Encounter with ID " + encounterId + " already exists. Skipping.");
                    continue;
                }

                Encounter encounter = new Encounter();

                // Set Encounter ID
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    encounter.addIdentifier().setValue(encounterId);
                }

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        encounter.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping encounter.");
                        continue;
                    }
                }

                // Validate and set Organization
                if (record.isMapped("ORGANIZATION") && !record.get("ORGANIZATION").isEmpty()) {
                    String organizationIdentifier = record.get("ORGANIZATION");
                    if (organizationExistsByIdentifier(client, organizationIdentifier)) {
                        encounter.setServiceProvider(new Reference("Organization?identifier=" + organizationIdentifier));
                    } else {
                        System.out.println("Organization not found: " + organizationIdentifier + ". Skipping encounter.");
                        continue;
                    }
                }

                // Validate and set Provider (Practitioner)
                if (record.isMapped("PROVIDER") && !record.get("PROVIDER").isEmpty()) {
                    String providerIdentifier = record.get("PROVIDER");
                    if (providerExistsByIdentifier(client, providerIdentifier)) {
                        encounter.addParticipant().setActor(new Reference("Practitioner?identifier=" + providerIdentifier));
                    } else {
                        System.out.println("Provider not found: " + providerIdentifier + ". Skipping encounter.");
                        continue;
                    }
                }

                // Set Encounter Period
                Period period = new Period();
                if (record.isMapped("START") && !record.get("START").isEmpty()) {
                    period.setStartElement(new DateTimeType(record.get("START")));
                }
                if (record.isMapped("STOP") && !record.get("STOP").isEmpty()) {
                    period.setEndElement(new DateTimeType(record.get("STOP")));
                }
                encounter.setActualPeriod(period);

                // Set Encounter Class
                if (record.isMapped("ENCOUNTERCLASS") && !record.get("ENCOUNTERCLASS").isEmpty()) {
                    CodeableConcept classConcept = new CodeableConcept();
                    classConcept.addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                            .setCode(record.get("ENCOUNTERCLASS")));
                    encounter.addClass_(classConcept);
                }

                // Set Encounter Type
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    encounter.addType(new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION"))));
                }

                // Add Base Encounter Cost
                if (record.isMapped("BASE_ENCOUNTER_COST") && !record.get("BASE_ENCOUNTER_COST").isEmpty()) {
                    encounter.addExtension("http://hl7.org/fhir/StructureDefinition/base-encounter-cost",
                            new Quantity().setValue(Double.parseDouble(record.get("BASE_ENCOUNTER_COST"))));
                }

                // Add Total Claim Cost
                if (record.isMapped("TOTAL_CLAIM_COST") && !record.get("TOTAL_CLAIM_COST").isEmpty()) {
                    encounter.addExtension("http://hl7.org/fhir/StructureDefinition/total-claim-cost",
                            new Quantity().setValue(Double.parseDouble(record.get("TOTAL_CLAIM_COST"))));
                }

                // Add Payer Coverage
                if (record.isMapped("PAYER_COVERAGE") && !record.get("PAYER_COVERAGE").isEmpty()) {
                    encounter.addExtension("http://hl7.org/fhir/StructureDefinition/payer-coverage",
                            new Quantity().setValue(Double.parseDouble(record.get("PAYER_COVERAGE"))));
                }

                // Send Encounter to FHIR server
                client.create().resource(encounter).execute();

                // Log success
                System.out.println("Encounter with ID " + encounterId + " uploaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if an Encounter exists on the FHIR server by identifier.
     */
    private boolean encounterExistsByIdentifier(IGenericClient client, String encounterIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Encounter")
                    .where(Encounter.IDENTIFIER.exactly().identifier(encounterIdentifier))
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Encounter by identifier: " + e.getMessage());
            return false;
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
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Patient by identifier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if an Organization exists on the FHIR server by identifier.
     */
    private boolean organizationExistsByIdentifier(IGenericClient client, String organizationIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Organization")
                    .where(Organization.IDENTIFIER.exactly().identifier(organizationIdentifier))
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Organization by identifier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if a Practitioner (Provider) exists on the FHIR server by identifier.
     */
    private boolean providerExistsByIdentifier(IGenericClient client, String providerIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Practitioner")
                    .where(Practitioner.IDENTIFIER.exactly().identifier(providerIdentifier))
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Practitioner by identifier: " + e.getMessage());
            return false;
        }
    }
}
