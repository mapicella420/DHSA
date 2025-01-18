package com.group01.dhsa.Model.FhirResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                        // Formatta la data usando il formato specifico (yyyy-MM-dd)
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate deathDate = LocalDate.parse(record.get("DEATHDATE"), formatter);

                        // Imposta la data di morte come DateType
                        String deathDateString = deathDate.toString(); // Formato: "YYYY-MM-DD"

                        // Imposta la data di morte come DateTimeType
                        patient.setDeceased(new DateTimeType(deathDateString));                    } catch (DateTimeParseException e) {
                        System.err.println("Formato non valido per DEATHDATE: " + record.get("DEATHDATE"));
                        // Se la data non è valida, imposta come boolean false
                        patient.setDeceased(new BooleanType(false));
                    }
                } else {
                    // Se DEATHDATE non è presente o vuoto, imposta come false
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

                // Indirizzo
                Address address = new Address();
                if (record.isMapped("ADDRESS")) address.addLine(record.get("ADDRESS"));
                if (record.isMapped("CITY")) address.setCity(record.get("CITY"));
                if (record.isMapped("STATE")) address.setState(record.get("STATE"));
                if (record.isMapped("ZIP")) address.setPostalCode(record.get("ZIP"));
                if (record.isMapped("COUNTY")) address.setDistrict(record.get("COUNTY"));
                patient.addAddress(address);

                // Telefono e identificativi
                if (record.isMapped("SSN")) {
                    patient.addIdentifier().setSystem("http://hl7.org/fhir/sid/us-ssn").setValue(record.get("SSN"));
                }
                if (record.isMapped("DRIVERS")) {
                    patient.addIdentifier().setSystem("http://hl7.org/fhir/sid/drivers-license").setValue(record.get("DRIVERS"));
                }
                if (record.isMapped("PASSPORT")) {
                    patient.addIdentifier().setSystem("http://hl7.org/fhir/sid/passport").setValue(record.get("PASSPORT"));
                }

                // Coordinate geografiche
                if (record.isMapped("LAT") && record.isMapped("LON")) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/geolocation",
                            new CodeableConcept().setText("Lat: " + record.get("LAT") + ", Lon: " + record.get("LON")));
                }

                // Informazioni sulle spese sanitarie
                if (record.isMapped("HEALTHCARE_EXPENSES")) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/healthcare-expenses",
                            new Quantity().setValue(Double.parseDouble(record.get("HEALTHCARE_EXPENSES"))));
                }
                if (record.isMapped("HEALTHCARE_COVERAGE")) {
                    patient.addExtension("http://hl7.org/fhir/StructureDefinition/healthcare-coverage",
                            new Quantity().setValue(Double.parseDouble(record.get("HEALTHCARE_COVERAGE"))));
                }

                // Invia il paziente al server FHIR
                client.create().resource(patient).execute();

                // Log di conferma
                System.out.println("Paziente con ID " + patient.getId() + " caricato con successo.");
            }
        } catch (Exception e) {
            // Gestione errori
            System.err.println("Errore durante l'importazione del CSV: " + e.getMessage());
            e.printStackTrace();
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
