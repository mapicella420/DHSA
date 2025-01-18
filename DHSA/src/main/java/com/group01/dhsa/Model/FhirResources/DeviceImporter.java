package com.group01.dhsa.Model.FhirResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;

public class DeviceImporter implements FhirResourceImporter {

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
                String deviceId = record.get("Id");

                // Check if the Device already exists
                if (deviceExistsByIdentifier(client, deviceId)) {
                    System.out.println("Device with ID " + deviceId + " already exists. Skipping.");
                    continue;
                }

                Device device = new Device();

                // Set Device ID
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    device.addIdentifier().setValue(deviceId);
                }

                // Associate with Patient using an Extension
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        device.addExtension("http://hl7.org/fhir/StructureDefinition/device-patient",
                                new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping device.");
                        continue;
                    }
                }

                // Validate and set Encounter as an Extension
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        device.addExtension("http://hl7.org/fhir/StructureDefinition/device-encounter",
                                new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping device.");
                        continue;
                    }
                }

                // Set Manufacturer
                if (record.isMapped("MANUFACTURER") && !record.get("MANUFACTURER").isEmpty()) {
                    device.setManufacturer(record.get("MANUFACTURER"));
                }

                // Set Model
                if (record.isMapped("MODEL") && !record.get("MODEL").isEmpty()) {
                    device.setModelNumber(record.get("MODEL"));
                }

                // Set Serial Number
                if (record.isMapped("SERIAL") && !record.get("SERIAL").isEmpty()) {
                    device.setSerialNumber(record.get("SERIAL"));
                }

                // Set Device Status
                if (record.isMapped("STATUS") && !record.get("STATUS").isEmpty()) {
                    device.setStatus(Device.FHIRDeviceStatus.fromCode(record.get("STATUS").toLowerCase()));
                }

                // Send Device to FHIR server
                client.create().resource(device).execute();

                // Log success
                System.out.println("Device with ID " + deviceId + " uploaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates if a Device exists on the FHIR server by identifier.
     */
    private boolean deviceExistsByIdentifier(IGenericClient client, String deviceIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Device")
                    .where(Device.IDENTIFIER.exactly().identifier(deviceIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Device by identifier: " + e.getMessage());
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
