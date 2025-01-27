package com.group01.dhsa;

import com.group01.dhsa.Model.*;
import com.group01.dhsa.Model.FhirResources.FhirExporterFactoryManager;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporterFactory;
import com.group01.dhsa.ObserverPattern.EventObservable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * The EventManager class is a Singleton responsible for managing events in the system.
 * It initializes an {@link EventObservable} instance and registers listeners for various events.
 */
public class EventManager {

    private static EventManager instance; // Singleton instance
    private final EventObservable eventObservable;
    private List<Map<String, String>> currentResources; // Stores the current resources

    /**
     * Private constructor to ensure only one instance of EventManager is created.
     */
    private EventManager() {
        this.eventObservable = new EventObservable();
        this.currentResources = new ArrayList<>(); // Initialize empty resource list
        initializeListeners(); // Initialize and register event listeners
    }

    /**
     * Retrieves the Singleton instance of the EventManager.
     *
     * @return The Singleton instance of the EventManager.
     */
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    /**
     * Provides access to the {@link EventObservable} instance managed by the EventManager.
     *
     * @return The EventObservable instance.
     */
    public EventObservable getEventObservable() {
        return eventObservable;
    }

    /**
     * Returns the current resources stored in EventManager.
     *
     * @return A list of maps representing the resources.
     */
    public List<Map<String, String>> getCurrentResources() {
        return currentResources;
    }

    /**
     * Updates the current resources stored in EventManager.
     *
     * @param resources The list of resources to store.
     */
    public void setCurrentResources(List<Map<String, String>> resources) {
        this.currentResources = resources;
        System.out.println("[DEBUG] Current resources set: " + resources);
    }


    /**
     * Initializes and registers listeners for various events.
     */
    private void initializeListeners() {
        // Register listener for CSV uploads
        CsvImporter csvImporter = new CsvImporter();
        eventObservable.subscribe("csv_upload", csvImporter);

        // Register listener for DICOM uploads
        DicomImporter dicomImporter = new DicomImporter();
        eventObservable.subscribe("dicom_upload", dicomImporter);

        // Register listener for CDA uploads
        CdaUploader cdaUploader = new CdaUploader();
        eventObservable.subscribe("cda_upload", cdaUploader);

        // Register a listener for generating CDA documents
        CdaDocumentCreator cdaCreator = new CdaDocumentCreator(this.eventObservable);
        eventObservable.subscribe("generate_cda", (eventType, file) -> {
            // Extract patient and encounter IDs from the file name
            String[] params = file.getName().replace(".xml", "").split("_");
            cdaCreator.createAndNotify(params[0], params[1]);
        });

        // Register a listener for converting CDA files to HTML
        eventObservable.subscribe("convert_to_html", (eventType, file) -> {
            System.out.println("[DEBUG] Received convert_to_html event for file: " + file.getAbsolutePath());
            new CdaToHtmlConverter().convertAndNotify(file);
        });

        // Register FHIR resource handling listeners
        eventObservable.subscribe("load_request", (eventType, file) -> {
            String resourceType = file.getName();
            loadResources(resourceType);
        });

        eventObservable.subscribe("search_request", (eventType, file) -> {
            String[] params = file.getName().split("_");
            String resourceType = params[0];
            String searchTerm = params[1];
            searchResources(resourceType, searchTerm);
        });

        eventObservable.subscribe("view_csv", (eventType, file) -> {
            System.out.println("[DEBUG] Received view_csv event.");
            // Logica per gestire l'evento view_csv
            loadCsvResources(file); // Aggiungi un metodo per caricare le risorse CSV
        });


        // Listener per la ricerca di documenti CDA
        eventObservable.subscribe("fetch_cda_by_patient", (eventType, file) -> {
            String patientName = file.getName();
            fetchCdaDocumentsByPatient(patientName);
        });

        // Listener per la ricerca di file DICOM
        eventObservable.subscribe("fetch_dicom_by_patient", (eventType, file) -> {
            String patientName = file.getName();
            fetchDicomFilesByPatient(patientName);
        });

        eventObservable.subscribe("cda_search_complete", (eventType, file) -> {
            System.out.println("[DEBUG] CDA search complete.");
            eventObservable.notify("load_complete", null);
        });

        eventObservable.subscribe("dicom_search_complete", (eventType, file) -> {
            System.out.println("[DEBUG] DICOM search complete.");
            eventObservable.notify("load_complete", null);
        });

        PatientDataTransfer patientDataTransfer = new PatientDataTransfer(this.eventObservable);
        eventObservable.subscribe("transfer", (eventType, file) -> {
            String[] params = file.getName().replace(".xml", "").split(",");
            patientDataTransfer.transferPatient(params[0], params[1], params[2]);
        });

    }

    private void loadCsvResources(File file) {
        try {
            // Logica per leggere e processare il file CSV
            System.out.println("[DEBUG] Loading CSV file: " + file.getName());
            // Puoi implementare la logica per aggiornare le risorse correnti con il contenuto del CSV
            eventObservable.notify("csv_loaded", null); // Notifica che il caricamento è completato
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading CSV: " + e.getMessage());
            eventObservable.notify("error", null);
        }
    }


    private void fetchCdaDocumentsByPatient(String patientName) {
        String url = "";
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                url = "mongodb://admin:mongodb@localhost:27018";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                url = "mongodb://admin:mongodb@localhost:27017";
            }
        }
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase database = mongoClient.getDatabase("medicalData");
            MongoCollection<org.bson.Document> collection = database.getCollection("cdaDocuments");

            // Query per ottenere i documenti CDA associati al paziente
            List<Map<String, String>> cdaDocuments = collection.find(new Document("patientName", patientName))
                    .map(doc -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("PatientName", doc.getString("patientName"));
                        return map;
                    })
                    .into(new ArrayList<>());

            System.out.println("[DEBUG] CDA Documents for patient '" + patientName + "': " + cdaDocuments); // Stampa i risultati
            setCurrentResources(cdaDocuments);

            if (cdaDocuments.isEmpty()) {
                System.out.println("[DEBUG] No CDA documents found for patient: " + patientName);
            }

            eventObservable.notify("cda_search_complete", null);
        } catch (Exception e) {
            System.err.println("Error fetching CDA documents for patient: " + patientName);
            e.printStackTrace();
            eventObservable.notify("error", null);
        }
    }



    private void fetchDicomFilesByPatient(String patientName) {
        String url = "";
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                url = "mongodb://admin:mongodb@localhost:27018";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                url = "mongodb://admin:mongodb@localhost:27017";
            }
        }
        try (MongoClient mongoClient = MongoClients.create(url)) {
            MongoDatabase database = mongoClient.getDatabase("medicalData");
            MongoCollection<org.bson.Document> collection = database.getCollection("dicomFiles");

            // Query per ottenere i file DICOM associati al paziente
            List<Map<String, String>> dicomFiles = collection.find(new Document("patientName", patientName))
                    .map(doc -> {
                        Map<String, String> map = new HashMap<>();
                        // Aggiungi solo i campi che corrispondono alle colonne della tabella
                        map.put("ID", String.valueOf(doc.get("_id"))); // ID
                        map.put("FileName", doc.getString("fileName")); // File Name
                        map.put("PatientID", doc.getString("patientId")); // Patient ID
                        map.put("StudyID", doc.getString("studyID")); // Study ID
                        map.put("StudyDate", doc.getString("studyDate")); // Study Date
                        map.put("StudyTime", doc.getString("studyTime")); // Study Time
                        return map;
                    })
                    .into(new ArrayList<>());

            setCurrentResources(dicomFiles);
            eventObservable.notify("dicom_search_complete", null);
        } catch (Exception e) {
            System.err.println("Error fetching DICOM files for patient: " + patientName);
            e.printStackTrace();
            eventObservable.notify("error", null);
        }
    }



    private void loadResources(String resourceType) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> resources = exporter.exportResources();
            setCurrentResources(resources);

            // Notify that resources have been loaded
            eventObservable.notify("load_complete", null);

        } catch (Exception e) {
            System.err.println("Error loading resources for type: " + resourceType);
            eventObservable.notify("error", null);
        }
    }

    private void searchResources(String resourceType, String searchTerm) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            FhirResourceExporter exporter = factory.createExporter();

            List<Map<String, String>> results = exporter.searchResources(searchTerm);
            setCurrentResources(results);

            // Notify that search has been completed
            eventObservable.notify("search_complete", null);

        } catch (Exception e) {
            System.err.println("Error searching resources for type: " + resourceType + " with term: " + searchTerm);
            eventObservable.notify("error", null);
        }
    }

    /**
     * Searches resources for a specific patient using the logged user's identifier.
     *
     * @param resourceType       The type of the FHIR resource to search for.
     * @param patientId  The identifier of the logged-in patient.
     */
    public void searchResourcesForPatient(String resourceType, String patientId) {
        new Thread(() -> {
            try {
                List<?> resources = FHIRClient.getInstance().fetchResourcesForPatient(resourceType, patientId);
                setCurrentResources(convertResourcesToMaps(resources)); // Utilizza il metodo aggiunto
                eventObservable.notify("search_complete", null);
            } catch (Exception e) {
                System.err.println("[ERROR] Unable to fetch resources: " + e.getMessage());
                eventObservable.notify("error", null);
            }
        }).start();
    }


    /**
     * Converts a list of FHIR resources into a list of maps for table display.
     *
     * @param resources A list of FHIR resources.
     * @return A list of maps representing the resources.
     */
    private List<Map<String, String>> convertResourcesToMaps(List<?> resources) {
        List<Map<String, String>> resourceMaps = new ArrayList<>();

        for (Object resource : resources) {
            Map<String, String> map = new HashMap<>();

            if (resource instanceof org.hl7.fhir.r5.model.Observation) {
                handleObservation((org.hl7.fhir.r5.model.Observation) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.Encounter) {
                handleEncounter((org.hl7.fhir.r5.model.Encounter) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.Procedure) {
                handleProcedure((org.hl7.fhir.r5.model.Procedure) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.AllergyIntolerance) {
                handleAllergyIntolerance((org.hl7.fhir.r5.model.AllergyIntolerance) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.CarePlan) {
                handleCarePlan((org.hl7.fhir.r5.model.CarePlan) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.Condition) {
                handleCondition((org.hl7.fhir.r5.model.Condition) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.ImagingStudy) {
                handleImagingStudy((org.hl7.fhir.r5.model.ImagingStudy) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.Immunization) {
                handleImmunization((org.hl7.fhir.r5.model.Immunization) resource, map);
            } else if (resource instanceof org.hl7.fhir.r5.model.MedicationRequest) {
                handleMedicationRequest((org.hl7.fhir.r5.model.MedicationRequest) resource, map);
            } else {
                System.out.println("[DEBUG] Resource type not handled: " + resource.getClass().getSimpleName());
                continue;
            }

            resourceMaps.add(map);
        }

        return resourceMaps;
    }

    private void handleObservation(org.hl7.fhir.r5.model.Observation observation, Map<String, String> map) {
        map.put("ID", observation.getIdElement().getIdPart());
        map.put("Code Text", observation.getCode().hasText() ? observation.getCode().getText() : "N/A");
        if (observation.hasValueQuantity()) {
            org.hl7.fhir.r5.model.Quantity quantity = observation.getValueQuantity();
            map.put("Value", quantity.getValue().toPlainString() + " " + quantity.getUnit());
        } else {
            map.put("Value", "N/A");
        }
        map.put("Effective Date", observation.hasEffectiveDateTimeType() ? observation.getEffectiveDateTimeType().toHumanDisplay() : "N/A");
    }

    private void handleEncounter(org.hl7.fhir.r5.model.Encounter encounter, Map<String, String> map) {
        map.put("ID", encounter.getIdElement().getIdPart());
        map.put("Identifier", encounter.hasIdentifier() && !encounter.getIdentifier().isEmpty()
                ? encounter.getIdentifierFirstRep().getValue()
                : "N/A");
        map.put("Type", encounter.hasType() && !encounter.getType().isEmpty()
                ? encounter.getType().stream()
                .map(type -> type.hasCoding() && type.getCodingFirstRep().hasDisplay()
                        ? type.getCodingFirstRep().getDisplay()
                        : "N/A")
                .findFirst()
                .orElse("N/A")
                : "N/A");
        addPatientDetails(encounter.getSubject(), map);
        map.put("Service Provider", encounter.hasServiceProvider() && encounter.getServiceProvider().getReference() != null
                ? encounter.getServiceProvider().getReference()
                : "N/A");
        map.put("Participant", encounter.hasParticipant() && !encounter.getParticipant().isEmpty()
                ? encounter.getParticipantFirstRep().getActor().getReference()
                : "N/A");
        if (encounter.hasActualPeriod()) {
            map.put("Start Date", encounter.getActualPeriod().getStartElement().toHumanDisplay());
            map.put("End Date", encounter.getActualPeriod().getEndElement() != null
                    ? encounter.getActualPeriod().getEndElement().toHumanDisplay()
                    : "N/A");
        } else {
            map.put("Start Date", "N/A");
            map.put("End Date", "N/A");
        }
        addExtensions(encounter.getExtension(), map);
    }

    private void handleProcedure(org.hl7.fhir.r5.model.Procedure procedure, Map<String, String> map) {
        map.put("ID", procedure.getIdElement().getIdPart());
        map.put("Status", procedure.hasStatus() ? procedure.getStatus().toCode() : "N/A");
        if (procedure.hasCode() && procedure.getCode().hasCoding()) {
            map.put("Procedure Code", procedure.getCode().getCodingFirstRep().getCode());
            map.put("Procedure Display", procedure.getCode().getCodingFirstRep().getDisplay());
        } else {
            map.put("Procedure Code", "N/A");
            map.put("Procedure Display", "N/A");
        }
        addPatientDetails(procedure.getSubject(), map);
        map.put("Occurrence Date", procedure.hasOccurrenceDateTimeType() ? procedure.getOccurrenceDateTimeType().toHumanDisplay() : "N/A");
        addExtensions(procedure.getExtension(), map);
        map.put("Encounter Reference", procedure.hasEncounter() ? procedure.getEncounter().getReference() : "N/A");
    }

    private void handleAllergyIntolerance(org.hl7.fhir.r5.model.AllergyIntolerance allergy, Map<String, String> map) {
        map.put("ID", allergy.getIdElement().getIdPart());
        map.put("Clinical Status", allergy.hasClinicalStatus()
                ? allergy.getClinicalStatus().getCodingFirstRep().getCode()
                : "N/A");
        map.put("Verification Status", allergy.hasVerificationStatus()
                ? allergy.getVerificationStatus().getCodingFirstRep().getCode()
                : "N/A");
        if (allergy.hasCode() && allergy.getCode().hasCoding()) {
            map.put("Allergy Code", allergy.getCode().getCodingFirstRep().getCode());
            map.put("Allergy Display", allergy.getCode().getCodingFirstRep().getDisplay());
        } else {
            map.put("Allergy Code", "N/A");
            map.put("Allergy Display", "N/A");
        }
        addPatientDetails(allergy.getPatient(), map);
        map.put("Recorded Date", allergy.hasRecordedDate() ? allergy.getRecordedDate().toString() : "N/A");
        addExtensions(allergy.getExtension(), map);
    }

    private void handleCarePlan(org.hl7.fhir.r5.model.CarePlan carePlan, Map<String, String> map) {
        map.put("ID", carePlan.getIdElement().getIdPart());
        map.put("Status", carePlan.hasStatus() ? carePlan.getStatus().toCode() : "N/A");
        map.put("Category", carePlan.hasCategory() && !carePlan.getCategory().isEmpty()
                ? carePlan.getCategoryFirstRep().getCodingFirstRep().getDisplay()
                : "N/A");
        addPatientDetails(carePlan.getSubject(), map);
        if (carePlan.hasPeriod()) {
            map.put("Start Date", carePlan.getPeriod().hasStart()
                    ? carePlan.getPeriod().getStartElement().toHumanDisplay()
                    : "N/A");
            map.put("End Date", carePlan.getPeriod().hasEnd()
                    ? carePlan.getPeriod().getEndElement().toHumanDisplay()
                    : "N/A");
        } else {
            map.put("Start Date", "N/A");
            map.put("End Date", "N/A");
        }
        map.put("Encounter Reference", carePlan.hasEncounter() ? carePlan.getEncounter().getReference() : "N/A");
    }

    private void handleCondition(org.hl7.fhir.r5.model.Condition condition, Map<String, String> map) {
        map.put("ID", condition.getIdElement().getIdPart());
        map.put("Clinical Status", condition.hasClinicalStatus() && condition.getClinicalStatus().hasCoding()
                ? condition.getClinicalStatus().getCodingFirstRep().getCode()
                : "N/A");
        map.put("Verification Status", condition.hasVerificationStatus() && condition.getVerificationStatus().hasCoding()
                ? condition.getVerificationStatus().getCodingFirstRep().getCode()
                : "N/A");
        if (condition.hasCode() && condition.getCode().hasCoding()) {
            map.put("Code", condition.getCode().getCodingFirstRep().getDisplay());
        } else {
            map.put("Code", "N/A");
        }
        addPatientDetails(condition.getSubject(), map);
        map.put("Onset Date", condition.hasOnsetDateTimeType() && condition.getOnsetDateTimeType().hasValue()
                ? condition.getOnsetDateTimeType().toHumanDisplay()
                : "N/A");
        map.put("Encounter Reference", condition.hasEncounter() ? condition.getEncounter().getReference() : "N/A");
    }

    private void handleImagingStudy(org.hl7.fhir.r5.model.ImagingStudy imagingStudy, Map<String, String> map) {
        map.put("ID", imagingStudy.getIdElement().getIdPart());
        map.put("Status", imagingStudy.hasStatus() ? imagingStudy.getStatus().toCode() : "N/A");
        addPatientDetails(imagingStudy.getSubject(), map);
        map.put("Started", imagingStudy.hasStarted() ? imagingStudy.getStartedElement().toHumanDisplay() : "N/A");
        map.put("Encounter Reference", imagingStudy.hasEncounter() ? imagingStudy.getEncounter().getReference() : "N/A");
    }

    private void handleImmunization(org.hl7.fhir.r5.model.Immunization immunization, Map<String, String> map) {
        map.put("ID", immunization.getIdElement().getIdPart());
        map.put("Status", immunization.hasStatus() ? immunization.getStatus().toCode() : "N/A");
        if (immunization.hasVaccineCode() && immunization.getVaccineCode().hasCoding()) {
            map.put("Vaccine Code", immunization.getVaccineCode().getCodingFirstRep().getCode());
            map.put("Vaccine Display", immunization.getVaccineCode().getCodingFirstRep().getDisplay());
        } else {
            map.put("Vaccine Code", "N/A");
            map.put("Vaccine Display", "N/A");
        }
        addPatientDetails(immunization.getPatient(), map);
        map.put("Occurrence Date", immunization.hasOccurrenceDateTimeType() ? immunization.getOccurrenceDateTimeType().toHumanDisplay() : "N/A");
    }

    private void handleMedicationRequest(org.hl7.fhir.r5.model.MedicationRequest medicationRequest, Map<String, String> map) {
        map.put("ID", medicationRequest.getIdElement().getIdPart());
        map.put("Status", medicationRequest.hasStatus() ? medicationRequest.getStatus().toCode() : "N/A");
        if (medicationRequest.hasMedication() && medicationRequest.getMedication().getConcept() != null
                && medicationRequest.getMedication().getConcept().hasCoding()) {
            map.put("Medication Code", medicationRequest.getMedication().getConcept().getCodingFirstRep().getCode());
            map.put("Medication Display", medicationRequest.getMedication().getConcept().getCodingFirstRep().getDisplay());
        } else {
            map.put("Medication Code", "N/A");
            map.put("Medication Display", "N/A");
        }
        addPatientDetails(medicationRequest.getSubject(), map);
        map.put("Encounter Reference", medicationRequest.hasEncounter() ? medicationRequest.getEncounter().getReference() : "N/A");
    }

    private void addPatientDetails(org.hl7.fhir.r5.model.Reference subjectReference, Map<String, String> map) {
        if (subjectReference != null && subjectReference.getReference() != null) {
            String patientReference = subjectReference.getReference();
            map.put("Patient Reference", patientReference);
            org.hl7.fhir.r5.model.Patient patient = resolvePatientReference(patientReference);
            if (patient != null) {
                map.put("Patient Name", patient.getNameFirstRep().getNameAsSingleString());
                map.put("Patient Gender", patient.getGender() != null ? patient.getGender().toCode() : "N/A");
            } else {
                map.put("Patient Name", "N/A");
                map.put("Patient Gender", "N/A");
            }
        } else {
            map.put("Patient Reference", "N/A");
            map.put("Patient Name", "N/A");
            map.put("Patient Gender", "N/A");
        }
    }

    private void addExtensions(List<org.hl7.fhir.r5.model.Extension> extensions, Map<String, String> map) {
        if (extensions != null) {
            for (org.hl7.fhir.r5.model.Extension extension : extensions) {
                String url = extension.getUrl();
                if (url.contains("base-encounter-cost")) {
                    map.put("Base Encounter Cost", String.valueOf(extension.getValueQuantity().getValue()));
                } else if (url.contains("total-claim-cost")) {
                    map.put("Total Claim Cost", String.valueOf(extension.getValueQuantity().getValue()));
                } else if (url.contains("payer-coverage")) {
                    map.put("Payer Coverage", String.valueOf(extension.getValueQuantity().getValue()));
                }
            }
        }
    }

    private org.hl7.fhir.r5.model.Patient resolvePatientReference(String patientReference) {
        try {
            if (patientReference != null && patientReference.startsWith("Patient/")) {
                String patientId = patientReference.split("/")[1];
                org.hl7.fhir.r5.model.Patient patient = FHIRClient.getInstance().getPatientFromIdentifier(patientId);
                if (patient == null) {
                    System.err.println("[ERROR] Patient not found for reference: " + patientReference);
                }
                return patient;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Unable to resolve patient reference: " + patientReference);
            e.printStackTrace();
        }
        return null;
    }






}
