package com.group01.dhsa.Model.FhirResources.Level3.Importer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.group01.dhsa.Controller.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;

public class DeviceImporter implements FhirResourceImporter {

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
                String deviceCode = record.get("CODE");

                // Check if the Device already exists
                if (deviceExistsByCode(client, deviceCode)) {
                    System.out.println("Device with CODE " + deviceCode + " already exists. Skipping.");
                    continue;
                }

                Device device = new Device();

                // Set Device Identifier using CODE
                if (record.isMapped("CODE") && !record.get("CODE").isEmpty()) {
                    device.addIdentifier().setValue(deviceCode);
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

                // Set Definition (corresponds to DESCRIPTION in dataset)
                if (record.isMapped("DESCRIPTION") && !record.get("DESCRIPTION").isEmpty()) {
                    CodeableReference codeableReference = new CodeableReference();
                    CodeableConcept codeableConcept = new CodeableConcept();
                    codeableConcept.setText(record.get("DESCRIPTION")); // Imposta il testo della descrizione
                    codeableReference.setConcept(codeableConcept); // Collega il CodeableConcept al CodeableReference
                    device.setDefinition(codeableReference); // Imposta la definizione del dispositivo
                }


                // Set UDI (Unique Device Identifier)
                if (record.isMapped("UDI") && !record.get("UDI").isEmpty()) {
                    Device.DeviceUdiCarrierComponent udiCarrier = new Device.DeviceUdiCarrierComponent();
                    udiCarrier.setCarrierHRF(record.get("UDI"));
                    device.addUdiCarrier(udiCarrier);
                }

                // Set Status (optional, default to 'active' if not provided)
                device.setStatus(Device.FHIRDeviceStatus.ACTIVE);

                // Send Device to FHIR server
                client.create().resource(device).execute();

                // Log success
                System.out.println("Device with CODE " + deviceCode + " uploaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates if a Device exists on the FHIR server by code.
     */
    private boolean deviceExistsByCode(IGenericClient client, String deviceCode) {
        try {
            var bundle = client.search()
                    .forResource("Device")
                    .where(Device.IDENTIFIER.exactly().identifier(deviceCode))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Device by code: " + e.getMessage());
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
