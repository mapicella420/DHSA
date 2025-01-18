package com.group01.dhsa.Model.FhirResources.level5.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Quantity;

import java.io.FileReader;
import java.io.Reader;

/**
 * Importer for Observation resources from a CSV file.
 */
public class ObservationImporter implements FhirResourceImporter {
    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public void importCsvToFhir(String csvFilePath) {
        try {
            // Initialize the FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Read the CSV file
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .withFirstRecordAsHeader()
                    .parse(in);

            // Iterate over the CSV records
            for (CSVRecord record : records) {
                Observation observation = new Observation();

                // Validate Patient existence by identifier
                String patientIdentifier = record.get("PATIENT");
                if (!patientExistsByIdentifier(client, patientIdentifier)) {
                    System.err.println("Patient identifier " + patientIdentifier + " not found. Skipping observation.");
                    continue;
                }
                observation.setSubject(new org.hl7.fhir.r5.model.Reference("Patient?identifier=" + patientIdentifier));

                // Validate Encounter existence
                String encounterId = record.get("ENCOUNTER");
                if (!resourceExists(client, "Encounter", encounterId)) {
                    System.err.println("Encounter ID " + encounterId + " not found. Skipping observation.");
                    continue;
                }
                observation.setEncounter(new org.hl7.fhir.r5.model.Reference("Encounter/" + encounterId));

                // Set observation date
                if (record.isMapped("DATE") && !record.get("DATE").isEmpty()) {
                    observation.setEffective(new org.hl7.fhir.r5.model.DateTimeType(record.get("DATE")));
                }

                // Set code and description
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty() &&
                        record.isMapped("DESCRIPTION") && !record.get("DESCRIPTION").isEmpty()) {
                    CodeableConcept code = new CodeableConcept();
                    code.addCoding(new Coding()
                            .setSystem("http://loinc.org")
                            .setCode(record.get("CODE"))
                            .setDisplay(record.get("DESCRIPTION")));
                    observation.setCode(code);
                }

                // Set value and units
                if (record.isMapped("VALUE") && !record.get("VALUE").isEmpty() &&
                        record.isMapped("UNITS") && !record.get("UNITS").isEmpty()) {
                    observation.setValue(new Quantity()
                            .setValue(Double.parseDouble(record.get("VALUE")))
                            .setUnit(record.get("UNITS")));
                }

                // Set type as category (if available)
                if (record.isMapped("TYPE") && !record.get("TYPE").isEmpty()) {
                    CodeableConcept category = new CodeableConcept();
                    category.addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                            .setCode(record.get("TYPE").toLowerCase())
                            .setDisplay(record.get("TYPE")));
                    observation.addCategory(category);
                }

                // Send the observation to the FHIR server
                client.create().resource(observation).execute();

                // Log success
                System.out.println("Observation created for Patient identifier " + patientIdentifier + " and Encounter ID " + encounterId);
            }
        } catch (Exception e) {
            // Handle errors
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a Patient exists on the FHIR server by matching the identifier.
     *
     * @param client The FHIR client used to connect to the server.
     * @param patientIdentifier The identifier to match against Patient resources.
     * @return true if a Patient with the given identifier exists, false otherwise.
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
     * Checks if a resource exists on the FHIR server by ID.
     *
     * @param client The FHIR client used to connect to the server.
     * @param resourceType The type of the FHIR resource (e.g., "Encounter").
     * @param resourceId The ID of the resource to check.
     * @return true if the resource exists, false otherwise.
     */
    private boolean resourceExists(IGenericClient client, String resourceType, String resourceId) {
        try {
            client.read()
                    .resource(resourceType)
                    .withId(resourceId)
                    .execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
