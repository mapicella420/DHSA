package com.group01.dhsa.Model;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.ObserverPattern.EventObservable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.hl7.fhir.r5.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PatientDataTransfer {
    private final EventObservable eventObservable;
    private static String CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials.txt";
    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String MONGO_DB_NAME = "data_app";
    private static final String MONGO_COLLECTION_NAME = "users";

    public static void setMongoUri() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            MONGO_URI = "mongodb://admin:mongodb@localhost:27018";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
        }
    }
    public static void setCredentialsFilePath() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials-other.txt";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials.txt";
        }
    }

    public PatientDataTransfer() {
        this.eventObservable = EventManager.getInstance().getEventObservable();
    }

    public PatientDataTransfer(EventObservable eventObservable) {
        this.eventObservable = eventObservable;
    }

    public void transferAndNotify(boolean success) {
        if (success) {
            eventObservable.notify("transfer_complete", null);
        } else {
            // Notify listeners about the failure
            eventObservable.notify("transfer_failed", null);
        }
    }

    public void transferPatient(String patientId, String encounterId, String organization) {
        setMongoUri();
        setCredentialsFilePath();

        FHIRClient fhirClient = FHIRClient.getInstance();

        Patient patient = fhirClient.getPatientFromId(patientId);

        //Create on other hospital
        if (organization != null) {
            if (organization.equalsIgnoreCase("Other Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8081/fhir");
            } else if (organization.equalsIgnoreCase("My Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8080/fhir");
            }
        }
        try {
            MethodOutcome meth = fhirClient.createResource(patient);
            if (meth == null){
                throw new NullPointerException("MethodOutcome is null");
            }
        }catch (Exception e){
            System.out.println("Transfer not completed: " + e);
            transferAndNotify(false);
        }

        //Search on current organization
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8081/fhir");
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8080/fhir");
            }
        }

        Encounter oldEncounter = fhirClient.getEncounterById(encounterId);
        Encounter encounter = new Encounter();
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(new Identifier().setValue(encounterId));
        encounter.setIdentifier(identifiers);

        CodeableConcept classConcept = new CodeableConcept();
        classConcept.addCoding(new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                .setCode(oldEncounter.getClass_FirstRep().getCodingFirstRep().getCode()));
        encounter.addClass_(classConcept);

        encounter.addType(new CodeableConcept().addCoding(new Coding()
                .setCode(oldEncounter.getTypeFirstRep().getCodingFirstRep().getCode())
                .setDisplay(oldEncounter.getTypeFirstRep().getCodingFirstRep().getDisplay())));

        if (organization.equalsIgnoreCase("My Hospital")){
            Reference reference = new Reference().setReference("Organization/3547");
            encounter.setServiceProvider(reference);
        } else if (organization.equalsIgnoreCase("Other Hospital")){
            Reference reference = new Reference().setReference("Organization/3580");
            encounter.setServiceProvider(reference);
        }

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DB_NAME);
            MongoCollection<Document> usersCollection = database.getCollection(MONGO_COLLECTION_NAME);

            BufferedWriter credentialsWriter = new BufferedWriter(
                    new FileWriter(CREDENTIALS_FILE_PATH, true));

            String firstName = patient.getNameFirstRep().getGiven().getFirst().asStringValue();
            String lastName = patient.getNameFirstRep().getFamily();

            String username = generateUsername(firstName, lastName);
            String password = generatePassword();

            if (userExistsByUsername(usersCollection, username)) {
                System.out.println("Username " + username + " già esistente. Skipping.");
            } else{
                Document userDocument = new Document("username", username)
                        .append("passwordHash", BCrypt.hashpw(password, BCrypt.gensalt()))
                        .append("fhirID", patientId)
                        .append("organization", LoggedUser.getOrganization())
                        .append("role", "patient")
                        .append("createdAt", new Date());
                usersCollection.insertOne(userDocument);


                credentialsWriter.write("ID FHIR: " + patientId + ", Username: " + username + ", Password: " + password);
                credentialsWriter.newLine();
            }

        } catch (IOException e) {
            transferAndNotify(false);
            throw new RuntimeException(e);
        }

        if (organization.equalsIgnoreCase("Other Hospital")) {
            FHIRClient.setFhirServerUrl("http://localhost:8081/fhir");
        } else if (organization.equalsIgnoreCase("My Hospital")) {
            FHIRClient.setFhirServerUrl("http://localhost:8080/fhir");
        }
        Patient patientOther = fhirClient.getPatientById(patientId);

        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8081/fhir");
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8080/fhir");
            }
        }

        encounter.setSubject(new Reference("Patient/" + patientOther.getIdPart()));

        if (organization.equalsIgnoreCase("My Hospital")){
            List<Encounter.EncounterParticipantComponent> participants = new ArrayList<>();
            Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
            participant.setActor(new Reference("Practitioner/6089"));
            participants.add(participant);
            encounter.setParticipant(participants);
        } else if (organization.equalsIgnoreCase("Other Hospital")){
            List<Encounter.EncounterParticipantComponent> participants = new ArrayList<>();
            Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
            participant.setActor(new Reference("Practitioner/6155"));
            participants.add(participant);
            encounter.setParticipant(participants);
        }



        fhirClient.createResource(encounter);
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

    private boolean userExistsByUsername(MongoCollection<Document> usersCollection, String username) {
        return usersCollection.find(new Document("username", username)).first() != null;
    }

}
