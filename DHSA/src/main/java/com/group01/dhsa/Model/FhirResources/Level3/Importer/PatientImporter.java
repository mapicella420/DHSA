package com.group01.dhsa.Model.FhirResources.Level3.Importer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;
import com.group01.dhsa.Controller.LoggedUser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.hl7.fhir.r5.model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.UUID;

public class PatientImporter implements FhirResourceImporter {
    private static String FHIR_SERVER_URL = "http://localhost:8080/fhir";
    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String MONGO_DB_NAME = "data_app";
    private static final String MONGO_COLLECTION_NAME = "users";
    private static  String CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials.txt";

    private static void setFhirServerUrl() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIR_SERVER_URL = "http://localhost:8081/fhir";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIR_SERVER_URL = "http://localhost:8080/fhir";
            }
        }
    }

    public static void setMongoUri() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            MONGO_URI = "mongodb://admin:mongodb@localhost:27017";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
        }
    }

    public static void setCredentialsFilePath() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials.txt";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials-other.txt";
        }
    }

    @Override
    public void importCsvToFhir(String csvFilePath) {
        setMongoUri();
        setFhirServerUrl();
        setCredentialsFilePath();
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DB_NAME);
            MongoCollection<Document> usersCollection = database.getCollection(MONGO_COLLECTION_NAME);

            BufferedWriter credentialsWriter = new BufferedWriter(new FileWriter(CREDENTIALS_FILE_PATH, true));

            // Inizializza il client FHIR
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Leggi il file CSV
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .withFirstRecordAsHeader()
                    .parse(in);

            for (CSVRecord record : records) {
                // Controlla se il paziente esiste già
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    String patientId = record.get("Id");
                    if (patientExistsByIdentifier(client, patientId)) {
                        System.out.println("[ DEBUG ] Paziente con ID " + patientId + " già esistente. Skipping.");
                        continue;
                    }
                }

                Patient patient = new Patient();

                // ID del paziente
                String patientId = record.isMapped("Id") ? record.get("Id") : UUID.randomUUID().toString();
                patient.addIdentifier().setValue(patientId);

                // Nome e cognome
                String firstName = record.isMapped("FIRST") ? record.get("FIRST") : "";
                String lastName = record.isMapped("LAST") ? record.get("LAST") : "";

                HumanName name = patient.addName();
                if (!firstName.isEmpty()) {
                    name.addGiven(firstName);
                }
                if (!lastName.isEmpty()) {
                    name.setFamily(lastName);
                }

                // Generazione di username e password
                String username = generateUsername(firstName, lastName);
                String password = generatePassword();

                // Controlla se l'username esiste già
                if (userExistsByUsername(usersCollection, username)) {
                    System.out.println("Username " + username + " già esistente. Skipping.");
                    continue;
                }

                // Salva l'utente su MongoDB
                Document userDocument = new Document("username", username)
                        .append("passwordHash", BCrypt.hashpw(password, BCrypt.gensalt()))
                        .append("fhirID", patientId)
                        .append("organization", LoggedUser.getOrganization())
                        .append("role", "patient")
                        .append("createdAt", new Date());
                usersCollection.insertOne(userDocument);

                // Salva le credenziali nel file .txt
                credentialsWriter.write("ID FHIR: " + patientId + ", Username: " + username + ", Password: " + password);
                credentialsWriter.newLine();

                if (record.isMapped("PREFIX") && !record.get("PREFIX").isEmpty()) {
                    name.addPrefix(record.get("PREFIX")); // Aggiungi il prefisso
                }
                if (record.isMapped("SUFFIX") && !record.get("SUFFIX").isEmpty()) {
                    name.addSuffix(record.get("SUFFIX")); // Aggiungi il suffisso
                }
                if (record.isMapped("MAIDEN") && !record.get("MAIDEN").isEmpty()) {
                    name.addGiven(record.get("MAIDEN")); // Nome da nubile
                }

                // Completa i dettagli del paziente
                if (record.isMapped("BIRTHDATE")) {
                    patient.setBirthDateElement(new DateType(record.get("BIRTHDATE")));
                }
                if (record.isMapped("GENDER")) {
                    patient.setGender(parseGender(record.get("GENDER")));
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


                // Salva il paziente nel server FHIR
                client.create().resource(patient).execute();
                System.out.println("Paziente con ID " + patientId + " caricato con successo.");
            }

            credentialsWriter.close();
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

    private boolean userExistsByUsername(MongoCollection<Document> usersCollection, String username) {
        return usersCollection.find(new Document("username", username)).first() != null;
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

    private String generateUsername(String firstName, String lastName) {
        // Usa parte del nome e cognome per generare un username
        String namePart = firstName.length() > 3 ? firstName.substring(0, 3) : firstName;
        String lastNamePart = lastName.length() > 3 ? lastName.substring(0, 3) : lastName;
        return namePart.toLowerCase() + lastNamePart.toLowerCase() + UUID.randomUUID().toString().substring(0, 4);
    }

    private String generatePassword() {
        // Genera una password casuale
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
