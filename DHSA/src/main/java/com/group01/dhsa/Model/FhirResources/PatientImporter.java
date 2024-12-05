package com.group01.dhsa.Model.FhirResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.csv.*;
import org.hl7.fhir.r5.model.*;

import java.io.FileReader;
import java.io.Reader;


// Implementazione concreta di FhirResourceImporter per gestire i pazienti.
public class PatientImporter implements FhirResourceImporter {
    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public void importCsvToFhir(String csvFilePath) {
        try {
            // Inizializza il client FHIR.
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Leggi il file CSV.
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader() // Legge la prima riga come intestazioni.
                    .withFirstRecordAsHeader() // Salta la prima riga.
                    .parse(in);

            // Itera sui record del CSV.
            for (CSVRecord record : records) {
                // Crea un nuovo oggetto Patient.
                Patient patient = new Patient();
                patient.setId(record.get("Id"));
                patient.addName()
                        .setFamily(record.get("LAST"))
                        .addGiven(record.get("FIRST"));
                patient.setBirthDateElement(new DateType(record.get("BIRTHDATE")));
                patient.setGender(parseGender(record.get("GENDER")));

                // Crea e aggiungi l'indirizzo.
                Address address = new Address();
                address.setCity(record.get("CITY"));
                address.setState(record.get("STATE"));
                address.setPostalCode(record.get("ZIP"));
                address.addLine(record.get("ADDRESS"));
                patient.addAddress(address);

                // Invia il paziente al server FHIR.
                client.create().resource(patient).execute();

                // Log di conferma per ogni paziente.
                System.out.println("Paziente con ID " + record.get("Id") + " caricato con successo.");
            }
        } catch (Exception e) {
            // Gestione delle eccezioni.
            System.err.println("Errore durante l'importazione del CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Enumerations.AdministrativeGender parseGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return Enumerations.AdministrativeGender.MALE;
        } else if ("female".equalsIgnoreCase(gender)) {
            return Enumerations.AdministrativeGender.FEMALE;
        } else {
            return Enumerations.AdministrativeGender.UNKNOWN;
        }
    }
}

