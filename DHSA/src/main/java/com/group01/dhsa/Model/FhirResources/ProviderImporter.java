package com.group01.dhsa.Model.FhirResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.hl7.fhir.r5.model.Practitioner;
import org.hl7.fhir.r5.model.PractitionerRole;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Quantity;

import java.io.FileReader;
import java.io.Reader;

/**
 * Importer for Provider resources from a CSV file.
 */
public class ProviderImporter implements FhirResourceImporter {
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
                Practitioner practitioner = new Practitioner();
                PractitionerRole practitionerRole = new PractitionerRole();

                // Validate Organization existence by identifier
                String organizationIdentifier = record.get("ORGANIZATION");
                if (!organizationExistsByIdentifier(client, organizationIdentifier)) {
                    System.err.println("Organization identifier " + organizationIdentifier + " not found. Skipping provider.");
                    continue;
                }
                practitionerRole.setOrganization(new org.hl7.fhir.r5.model.Reference("Organization?identifier=" + organizationIdentifier));

                // Set Practitioner ID
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    practitioner.addIdentifier().setValue(record.get("Id"));
                }

                // Set Practitioner name
                if (record.isMapped("NAME") && !record.get("NAME").isEmpty()) {
                    String[] nameParts = record.get("NAME").split(" ");
                    if (nameParts.length > 0) {
                        practitioner.addName().setFamily(nameParts[nameParts.length - 1]);
                        for (int i = 0; i < nameParts.length - 1; i++) {
                            practitioner.getNameFirstRep().addGiven(nameParts[i]);
                        }
                    }
                }

                // Set gender
                if (record.isMapped("GENDER") && !record.get("GENDER").isEmpty()) {
                    practitioner.setGender(parseGender(record.get("GENDER")));
                }

                // Set specialty as codeable concept
                if (record.isMapped("SPECIALITY") && !record.get("SPECIALITY").isEmpty()) {
                    CodeableConcept specialty = new CodeableConcept();
                    specialty.addCoding(new Coding()
                            .setSystem("http://hl7.org/fhir/ValueSet/c80-practice-codes")
                            .setDisplay(record.get("SPECIALITY")));
                    practitionerRole.addSpecialty(specialty);
                }

                // Set address
                if (record.isMapped("ADDRESS") || record.isMapped("CITY") || record.isMapped("STATE") || record.isMapped("ZIP")) {
                    Address address = new Address();
                    if (record.isMapped("ADDRESS") && !record.get("ADDRESS").isEmpty()) {
                        address.addLine(record.get("ADDRESS"));
                    }
                    if (record.isMapped("CITY") && !record.get("CITY").isEmpty()) {
                        address.setCity(record.get("CITY"));
                    }
                    if (record.isMapped("STATE") && !record.get("STATE").isEmpty()) {
                        address.setState(record.get("STATE"));
                    }
                    if (record.isMapped("ZIP") && !record.get("ZIP").isEmpty()) {
                        address.setPostalCode(record.get("ZIP"));
                    }
                    practitioner.addAddress(address);
                }

                // Add contact point for phone
                if (record.isMapped("PHONE") && !record.get("PHONE").isEmpty()) {
                    practitioner.addTelecom(new ContactPoint()
                            .setSystem(ContactPoint.ContactPointSystem.PHONE)
                            .setValue(record.get("PHONE")));
                }

                // Add utilization as an extension
                if (record.isMapped("UTILIZATION") && !record.get("UTILIZATION").isEmpty()) {
                    practitionerRole.addExtension("http://hl7.org/fhir/StructureDefinition/utilization",
                            new Quantity().setValue(Double.parseDouble(record.get("UTILIZATION"))));
                }

                // Send Practitioner and PractitionerRole to the FHIR server
                client.create().resource(practitioner).execute();
                client.create().resource(practitionerRole).execute();

                // Log success
                System.out.println("Provider created with Practitioner ID " + practitioner.getId() +
                        " and associated Organization identifier " + organizationIdentifier);
            }
        } catch (Exception e) {
            // Handle errors
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if an Organization exists on the FHIR server by matching the identifier.
     *
     * @param client The FHIR client used to connect to the server.
     * @param organizationIdentifier The identifier to match against Organization resources.
     * @return true if an Organization with the given identifier exists, false otherwise.
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
     * Parses gender from a string.
     *
     * @param gender The gender string from the dataset.
     * @return The FHIR gender enumeration.
     */
    private AdministrativeGender parseGender(String gender) {
        switch (gender.toLowerCase()) {
            case "male":
            case "m":
                return AdministrativeGender.MALE;
            case "female":
            case "f":
                return AdministrativeGender.FEMALE;
            default:
                return AdministrativeGender.UNKNOWN;
        }
    }
}
