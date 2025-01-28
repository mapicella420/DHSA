package com.group01.dhsa.Model;

import com.group01.dhsa.LoggedUser;
import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.ObserverPattern.EventObservable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hl7.fhir.r5.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The PatientDataTransfer class handles the transfer of patient-related data between different hospitals or systems.
 * This includes FHIR resources, credentials, and associated files (e.g., DICOM).
 */
public class PatientDataTransfer {
    private final EventObservable eventObservable;
    private static String CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials.txt";
    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String MONGO_DB_NAME = "data_app";
    private static final String MONGO_COLLECTION_NAME = "users";
    /**
     * Updates the MongoDB URI based on the logged user's organization.
     */
    public static void setMongoUri() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            MONGO_URI = "mongodb://admin:mongodb@localhost:27018";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
        }
    }

    /**
     * Updates the credentials file path based on the logged user's organization.
     */
    public static void setCredentialsFilePath() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials-other.txt";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            CREDENTIALS_FILE_PATH = "src/main/resources/com/group01/dhsa/CredentialPatient/credentials.txt";
        }
    } /**
     * Default constructor that initializes the event observable using the EventManager.
     */
    public PatientDataTransfer() {
        this.eventObservable = EventManager.getInstance().getEventObservable();
    }
    /**
     * Constructor that accepts an EventObservable for event notifications.
     *
     * @param eventObservable The observable to use for notifying events.
     */
    public PatientDataTransfer(EventObservable eventObservable) {
        this.eventObservable = eventObservable;
    }

    /**
     * Notifies observers about the completion or failure of a data transfer.
     *
     * @param success Whether the transfer was successful.
     */
    public void transferAndNotify(boolean success) {
        if (success) {
            eventObservable.notify("transfer_complete", null);
        } else {
            // Notify listeners about the failure
            eventObservable.notify("transfer_failed", null);
        }
    }
    /**
     * Transfers a patient's data and related information (e.g., credentials, DICOM files) to a specified organization.
     *
     * @param patientId   The ID of the patient to transfer.
     * @param encounterId The ID of the encounter associated with the patient.
     * @param organization The target organization for the data transfer.
     */
    public void transferPatient(String patientId, String encounterId, String organization) {
        setMongoUri();
        setCredentialsFilePath();

        FHIRClient fhirClient = FHIRClient.getInstance();
        Bundle transactionBundle = new Bundle();
        transactionBundle.setType(Bundle.BundleType.TRANSACTION);

        Patient patient = fhirClient.getPatientById(patientId);
        //Search if it's already present
        sendData(organization);
        Patient existingPatient = fhirClient.getPatientById(patientId);

        retriveData();
        Encounter oldEncounter = fhirClient.getEncounterById(encounterId);

        String uploadedPatientId = "";
        if (existingPatient != null) {
            uploadedPatientId = existingPatient.getIdPart();
        } else {
            Bundle.BundleEntryComponent patientEntry = new Bundle.BundleEntryComponent();
            Patient patient1 = patient.copy();
            patient1.setId("");
            patientEntry.setResource(patient1);

            patientEntry.getRequest()
                    .setMethod(Bundle.HTTPVerb.POST);

            transactionBundle.addEntry(patientEntry);
            sendData(organization);
            Bundle response = fhirClient.transaction(transactionBundle);
            uploadedPatientId = response.getEntry().getFirst()
                    .getResponse().getLocation().split("/")[1];
        }


        //Uploading new patient credentials
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DB_NAME);
            MongoCollection<Document> usersCollection = database.getCollection(MONGO_COLLECTION_NAME);

            BufferedWriter credentialsWriter = new BufferedWriter(
                    new FileWriter(CREDENTIALS_FILE_PATH, true));

            String firstName = patient.getNameFirstRep().getGiven().getFirst().asStringValue();
            String lastName = patient.getNameFirstRep().getFamily();

            String username = generateUsername(firstName, lastName);
            String password = generatePassword();

            if (!userExistsByUsername(usersCollection, username) && !userExistsByIdentifier(usersCollection, patientId)) {
                Document userDocument = new Document("username", username)
                        .append("passwordHash", BCrypt.hashpw(password, BCrypt.gensalt()))
                        .append("fhirID", patientId)
                        .append("organization", organization)
                        .append("role", "patient")
                        .append("createdAt", new Date());
                usersCollection.insertOne(userDocument);

                System.out.println("[INFO] User inserted into MongoDB: " + userDocument.toJson());

                credentialsWriter.write("ID FHIR: " + patientId + ", Username: " + username + ", Password: " + password);
                credentialsWriter.newLine();
                credentialsWriter.close();
            }


        } catch (IOException e) {
            transferAndNotify(false);
            throw new RuntimeException(e);
        }

        //Search for dicom in other hospital
        if (LoggedUser.getOrganization().equals("My Hospital")){
            MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
        }

        List<Document> filteredFiles;
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase databaseDicom = mongoClient.getDatabase("medicalData");
            MongoCollection<Document> collection = databaseDicom.getCollection("dicomFiles");
            List<Document> files = collection.find().into(new java.util.ArrayList<>());
            files.forEach(doc -> System.out.println("[DEBUG] Loaded document: " + doc.toJson()));

            filteredFiles = files.stream()
                    .filter(doc -> {
                        String patientNameMongo = doc.getString("patientName");
                        String patientName = patient.getName().getFirst().getGiven()
                                .getFirst().toString() + " " +
                                patient.getNameFirstRep().getFamily();
                        boolean name = isNameMatch(patientNameMongo, patientName);
                        DateTimeType encounterDate = oldEncounter.getActualPeriod().getStartElement();

                        String date = getFieldValue(doc, "studyDate");

                        String encounterD = encounterDate != null && encounterDate.getValue() != null
                                ? new SimpleDateFormat("yyyyMMdd").format(encounterDate.getValue())
                                : "Invalid Date";

                        boolean isSameDate = date.equals(encounterD);

                        return name && isSameDate;
                    })
                    .toList();
        }
        if (!filteredFiles.isEmpty()){
            if (organization.equals("My Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
            } else if (organization.equals("Other Hospital")) {
                MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
            }
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase databaseDicom = mongoClient.getDatabase("medicalData");
                MongoCollection<Document> collection = databaseDicom.getCollection("dicomFiles");
                List<Document> files = collection.find().into(new java.util.ArrayList<>());

                if (files.isEmpty()) {
                    System.out.println("[DEBUG] No documents found in the collection.");
                } else {
                    files.forEach(doc ->
                            System.out.println("[DEBUG] Loaded document: " + doc.toJson()));
                }

                Set<String> existingFileNames = files.stream()
                        .map(file -> getFieldValue(file, "fileName"))
                        .collect(Collectors.toSet());

                for (Document filteredFile : filteredFiles) {
                    String fileFilteredName = getFieldValue(filteredFile, "fileName");

                    if (!existingFileNames.contains(fileFilteredName)) {
                        collection.insertOne(filteredFile);
                        System.out.println("[INFO] Inserted new document: " + filteredFile.toJson());
                    } else {
                        System.out.println("[INFO] File already exists, skipping: " + fileFilteredName);
                    }
                }
            }
        }

        //New Encounter for other hospital
        Encounter encounter = new Encounter();
        encounter.addIdentifier().setValue(encounterId+1);

        CodeableConcept classConcept = new CodeableConcept();
        classConcept.addCoding(new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                .setCode(oldEncounter.getClass_FirstRep().getCodingFirstRep().getCode()));
        encounter.addClass_(classConcept);

        encounter.addType(new CodeableConcept().addCoding(new Coding()
                .setCode(oldEncounter.getTypeFirstRep().getCodingFirstRep().getCode())
                .setDisplay(oldEncounter.getTypeFirstRep().getCodingFirstRep().getDisplay())));

        //Fixed values chosen by hand
        if (organization.equalsIgnoreCase("My Hospital")){
            Reference reference = new Reference().setReference("Organization/3547");
            encounter.setServiceProvider(reference);
        } else if (organization.equalsIgnoreCase("Other Hospital")){
            Reference reference = new Reference().setReference("Organization/8");
            encounter.setServiceProvider(reference);
        }

        encounter.setSubject(new Reference("Patient/" + uploadedPatientId));

        List<Encounter.EncounterParticipantComponent> participants = new ArrayList<>();
        Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
        if (organization.equalsIgnoreCase("My Hospital")){
            participant.setActor(new Reference("Practitioner/6089"));
        } else if (organization.equalsIgnoreCase("Other Hospital")){
            participant.setActor(new Reference("Practitioner/2550"));
        }
        participants.add(participant);
        encounter.setParticipant(participants);

        // Set Encounter Period
        Period period = new Period();
        period.setStart(new Date());
        period.setEnd(null);
        encounter.setActualPeriod(period);

        sendData(organization);
        transactionBundle = new Bundle();
        transactionBundle.setType(Bundle.BundleType.TRANSACTION);
        Bundle.BundleEntryComponent encounterEntry = new Bundle.BundleEntryComponent();
        encounterEntry.setResource(encounter);
        encounterEntry.getRequest()
                .setMethod(Bundle.HTTPVerb.POST)
                .setUrl("Encounter");
        transactionBundle.addEntry(encounterEntry);

        Bundle response = fhirClient.transaction(transactionBundle);
        String uploadedEncounterId = response.getEntry().getFirst()
                .getResponse().getLocation().split("/")[1];


        transactionBundle = new Bundle();
        transactionBundle.setType(Bundle.BundleType.TRANSACTION);

        retriveData();
        //Allergies
        List<AllergyIntolerance> allergies = fhirClient.getAllergiesForPatientAndEncounter(
                patient.getIdPart(), oldEncounter.getIdPart()
        );

        if (allergies != null && !allergies.isEmpty()) {
            for (AllergyIntolerance allergy : allergies) {
                allergy.setPatient(new Reference("Patient/" + uploadedPatientId));
                allergy.getExtension().removeIf(ext ->
                        "http://hl7.org/fhir/StructureDefinition/encounter-reference".equals(ext.getUrl())
                );

                allergy.addExtension(new Extension(
                        "http://hl7.org/fhir/StructureDefinition/encounter-reference",
                        new Reference("Encounter/" + uploadedEncounterId)
                ));

                addEntryToBundle(transactionBundle, allergy, Bundle.HTTPVerb.POST, "AllergyIntolerance");

            }
        }

        //MedicationRequest
        List<MedicationRequest> medicationRequests = fhirClient
                .getMedicationRequestForPatientAndEncounter(patient.getIdPart(),
                        oldEncounter.getIdPart());

        if (medicationRequests != null && !medicationRequests.isEmpty()) {
            for (MedicationRequest medicationRequest : medicationRequests) {
                medicationRequest.setSubject(new Reference("Patient/" + uploadedPatientId));
                medicationRequest.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, medicationRequest, Bundle.HTTPVerb.POST, "MedicationRequest");
            }
        }

        List<Procedure> procedures = fhirClient.getProceduresForPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (procedures != null && !procedures.isEmpty()) {
            for (Procedure procedure : procedures) {
                procedure.setSubject(new Reference("Patient/" + uploadedPatientId));
                procedure.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, procedure, Bundle.HTTPVerb.POST, "Procedure");
            }
        }

        //Observation
        List<Observation> observations = fhirClient.getObservationsForPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (observations != null && !observations.isEmpty()) {
            for (Observation observation : observations) {
                observation.setSubject(new Reference("Patient/" + uploadedPatientId));
                observation.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, observation, Bundle.HTTPVerb.POST, "Observation");
            }
        }

        //Immunization
        List<Immunization> immunizations = fhirClient.getImmunizationsForPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (immunizations != null && !immunizations.isEmpty()) {
            for (Immunization immunization : immunizations) {
                immunization.setPatient(new Reference("Patient/" + uploadedPatientId));
                immunization.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, immunization, Bundle.HTTPVerb.POST, "Immunization");
            }
        }

        //CarePlan
        List<CarePlan> carePlans = fhirClient.getCarePlansForPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (carePlans != null && !carePlans.isEmpty()) {
            for (CarePlan carePlan : carePlans) {
                carePlan.setSubject(new Reference("Patient/" + uploadedPatientId));
                carePlan.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, carePlan, Bundle.HTTPVerb.POST, "CarePlan");
            }
        }

        //Condition
        List<Condition> conditions = fhirClient.getConditionsForPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (conditions != null && !conditions.isEmpty()) {
            for (Condition condition : conditions) {
                condition.setSubject(new Reference("Patient/" + uploadedPatientId));
                condition.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, condition, Bundle.HTTPVerb.POST, "Condition");
            }
        }

        //Imaging study
        List<ImagingStudy> imagingStudies = fhirClient.getImagingStudiesForPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (imagingStudies != null && !imagingStudies.isEmpty()) {
            for (ImagingStudy imagingStudy : imagingStudies) {
                imagingStudy.setSubject(new Reference("Patient/" + uploadedPatientId));
                imagingStudy.setEncounter(new Reference("Encounter/" + uploadedEncounterId));

                addEntryToBundle(transactionBundle, imagingStudy, Bundle.HTTPVerb.POST, "ImagingStudy");
            }
        }

        //Device
        List<Device> devices = fhirClient.getDeviceByPatientAndEncounter(
                patient.getIdPart(),
                oldEncounter.getIdPart()
        );

        if (devices != null && !devices.isEmpty()) {
            for (Device device : devices) {
                device.getExtension().removeIf(ext ->
                        "http://hl7.org/fhir/StructureDefinition/device-patient".equals(ext.getUrl())
                );
                device.addExtension(new Extension(
                        "http://hl7.org/fhir/StructureDefinition/device-patient",
                        new Reference("Patient/" + uploadedPatientId)
                ));

                device.getExtension().removeIf(ext ->
                        "http://hl7.org/fhir/StructureDefinition/device-encounter".equals(ext.getUrl())
                );
                device.addExtension(new Extension(
                        "http://hl7.org/fhir/StructureDefinition/device-encounter",
                        new Reference("Encounter/" + uploadedEncounterId)
                ));

                addEntryToBundle(transactionBundle, device, Bundle.HTTPVerb.POST, "Device");
            }
        }

        //Send data to other Fhir
        try {
            sendData(organization);
            response = fhirClient.transaction(transactionBundle);

            if (response.getEntry().stream()
                    .anyMatch(entry ->
                            entry.getResponse().getStatus().startsWith("4") ||
                                    entry.getResponse().getStatus().startsWith("5"))) {
                throw new RuntimeException("Transaction failed. Check individual entries for errors.");
            }

            transferAndNotify(true);
        } catch (Exception e) {
            System.out.println("Transfer not completed: " + e);
            transferAndNotify(false);
        }
    }

    private <T extends Resource> void addEntryToBundle(Bundle transactionBundle, T resource, Bundle.HTTPVerb method, String url) {
        Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
        entry.setResource(resource);
        entry.getRequest()
                .setMethod(method)
                .setUrl(url);
        transactionBundle.addEntry(entry);
    }


    private void retriveData(){
        //Search on current organization
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8081/fhir");
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIRClient.setFhirServerUrl("http://localhost:8080/fhir");
            }
        }
    }

    private void sendData(String organization){
        if (organization.equalsIgnoreCase("Other Hospital")) {
            FHIRClient.setFhirServerUrl("http://localhost:8081/fhir");
        } else if (organization.equalsIgnoreCase("My Hospital")) {
            FHIRClient.setFhirServerUrl("http://localhost:8080/fhir");
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

    private boolean userExistsByUsername(MongoCollection<Document> usersCollection, String username) {
        return usersCollection.find(new Document("username", username)).first() != null;
    }

    private boolean userExistsByIdentifier(MongoCollection<Document> usersCollection, String fhirId) {
        return usersCollection.find(new Document("fhirID", fhirId)).first() != null;
    }

    private boolean isNameMatch(String dbPatientName, String inputPatientName) {

        String[] dbParts = dbPatientName.split(" ");
        String[] inputParts = inputPatientName.split(" ");


        if (dbParts.length <= inputParts.length) {
            for (String dbPart : dbParts) {
                if (!Arrays.asList(inputParts).contains(dbPart)) {
                    return false;
                }
            }
            return true;
        }


        for (String inputPart : inputParts) {
            if (!Arrays.asList(dbParts).contains(inputPart)) {
                return false;
            }
        }
        return true;
    }

    private String getFieldValue(Document document, String fieldName) {
        try {
            Object value = document.get(fieldName);
            if (value instanceof ObjectId) {
                return value.toString();
            } else if (value instanceof String) {
                return (String) value;
            } else if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error getting field value for '" + fieldName + "': " + e.getMessage());
        }
        return "N/A";
    }

}
