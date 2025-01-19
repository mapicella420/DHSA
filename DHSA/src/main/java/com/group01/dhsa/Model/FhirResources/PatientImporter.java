package com.group01.dhsa.Model.FhirResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PatientImporter implements FhirResourceImporter {
    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public void importCsvToFhir(String csvFilePath) {
        try {
            // Inizializza il client FHIR
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Leggi il file CSV
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .withFirstRecordAsHeader()
                    .parse(in);

            // Itera sui record del CSV
            for (CSVRecord record : records) {
                // Verifica se il paziente esiste già
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    String patientId = record.get("Id");
                    if (patientExistsByIdentifier(client, patientId)) {
                        System.out.println("Paziente con ID " + patientId + " già esistente. Skipping.");
                        continue;
                    }
                }

                Patient patient = new Patient();

                // ID
                if (record.isMapped("Id")) {
                    patient.addIdentifier().setValue(record.get("Id"));
                }

                // Nome
                HumanName name = patient.addName();
                if (record.isMapped("PREFIX") && !record.get("PREFIX").isEmpty()) {
                    name.addPrefix(record.get("PREFIX")); // Aggiungi il prefisso
                }
                if (record.isMapped("FIRST") && !record.get("FIRST").isEmpty()) {
                    name.addGiven(record.get("FIRST")); // Nome
                }
                if (record.isMapped("LAST") && !record.get("LAST").isEmpty()) {
                    name.setFamily(record.get("LAST")); // Cognome
                }
                if (record.isMapped("SUFFIX") && !record.get("SUFFIX").isEmpty()) {
                    name.addSuffix(record.get("SUFFIX")); // Aggiungi il suffisso
                }
                if (record.isMapped("MAIDEN") && !record.get("MAIDEN").isEmpty()) {
                    name.addGiven(record.get("MAIDEN")); // Nome da nubile
                }

                // Data di nascita
                if (record.isMapped("BIRTHDATE")) {
                    patient.setBirthDateElement(new DateType(record.get("BIRTHDATE")));
                }

                // Data di morte
                if (record.isMapped("DEATHDATE") && !record.get("DEATHDATE").isEmpty()) {
                    try {
                        LocalDate deathDate = LocalDate.parse(record.get("DEATHDATE"));
                        patient.setDeceased(new DateTimeType(deathDate.toString()));
                    } catch (DateTimeParseException e) {
                        System.err.println("Formato non valido per DEATHDATE: " + record.get("DEATHDATE"));
                        patient.setDeceased(new BooleanType(false));
                    }
                } else {
                    patient.setDeceased(new BooleanType(false));
                }

                // Genere
                if (record.isMapped("GENDER")) {
                    patient.setGender(parseGender(record.get("GENDER")));
                }

                // Stato civile
                if (record.isMapped("MARITAL")) {
                    patient.setMaritalStatus(new CodeableConcept().setText(record.get("MARITAL")));
                }

                // Razza ed etnia
                if (record.isMapped("RACE")) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/us-core-race",
                            new CodeableConcept().setText(record.get("RACE")));
                }
                if (record.isMapped("ETHNICITY")) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity",
                            new CodeableConcept().setText(record.get("ETHNICITY")));
                }

                // Indirizzo con geolocalizzazione
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
                if (record.isMapped("COUNTY") && !record.get("COUNTY").isEmpty()) {
                    address.setDistrict(record.get("COUNTY"));
                }
                if (record.isMapped("LAT") && record.isMapped("LON") &&
                        !record.get("LAT").isEmpty() && !record.get("LON").isEmpty()) {
                    double latitude = Double.parseDouble(record.get("LAT"));
                    double longitude = Double.parseDouble(record.get("LON"));

                    // Estensione per latitudine
                    Extension latExtension = new Extension();
                    latExtension.setUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat");
                    latExtension.setValue(new DecimalType(latitude));

                    // Estensione per longitudine
                    Extension lonExtension = new Extension();
                    lonExtension.setUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon");
                    lonExtension.setValue(new DecimalType(longitude));

                    // Aggiungi estensioni all'indirizzo
                    address.addExtension(latExtension);
                    address.addExtension(lonExtension);
                }

                patient.addAddress(address);

                // Birthplace
                if (record.isMapped("BIRTHPLACE") && !record.get("BIRTHPLACE").isEmpty()) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/birthplace",
                            new Address().setText(record.get("BIRTHPLACE")));
                }

                // Identificativi
                if (record.isMapped("SSN")) {
                    patient.addIdentifier().setSystem("http://hl7.org/fhir/sid/us-ssn").setValue(record.get("SSN"));
                }
                if (record.isMapped("DRIVERS")) {
                    patient.addIdentifier().setSystem("http://hl7.org/fhir/sid/drivers-license").setValue(record.get("DRIVERS"));
                }
                if (record.isMapped("PASSPORT")) {
                    patient.addIdentifier().setSystem("http://hl7.org/fhir/sid/passport").setValue(record.get("PASSPORT"));
                }

                // Telefono e spese sanitarie
                if (record.isMapped("HEALTHCARE_EXPENSES") && !record.get("HEALTHCARE_EXPENSES").isEmpty()) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/healthcare-expenses",
                            new Quantity().setValue(Double.parseDouble(record.get("HEALTHCARE_EXPENSES"))));
                }
                if (record.isMapped("HEALTHCARE_COVERAGE") && !record.get("HEALTHCARE_COVERAGE").isEmpty()) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/healthcare-coverage",
                            new Quantity().setValue(Double.parseDouble(record.get("HEALTHCARE_COVERAGE"))));
                }

                // Invia il paziente al server FHIR
                client.create().resource(patient).execute();

                // Log di conferma
                System.out.println("Paziente con ID " + patient.getId() + " caricato con successo.");
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'importazione del CSV: " + e.getMessage());
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
            System.err.println("Errore durante il controllo esistenza paziente: " + e.getMessage());
            return false;
        }
    }

    private Enumerations.AdministrativeGender parseGender(String gender) {
        switch (gender.toLowerCase()) {
            case "m":
                return Enumerations.AdministrativeGender.MALE;
            case "f":
                return Enumerations.AdministrativeGender.FEMALE;
            default:
                return Enumerations.AdministrativeGender.UNKNOWN;
        }
    }
}
