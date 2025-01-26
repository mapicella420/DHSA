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

        // Listener per risorsa linkata selezionata
        eventObservable.subscribe("linked_resource_selected", (eventType, file) -> {
            String resourceDetails = file.getName();
            String[] parts = resourceDetails.split("_"); // Formato: "ResourceType_ResourceId"
            if (parts.length == 2) {
                String resourceType = parts[0];
                String resourceId = parts[1];
                fetchLinkedResource(resourceType, resourceId);
            } else {
                System.err.println("[ERROR] Invalid linked resource details: " + resourceDetails);
            }
        });

    }

    private void fetchLinkedResource(String resourceType, String resourceId) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            if (factory == null) {
                System.err.println("[ERROR] No factory found for resource type: " + resourceType);
                return;
            }

            FhirResourceExporter exporter = factory.createExporter();
            List<Map<String, String>> results = exporter.searchResources("Id", resourceId);
            if (results.isEmpty()) {
                System.out.println("[DEBUG] No resource found for Type=" + resourceType + ", ID=" + resourceId);
            } else {
                System.out.println("[DEBUG] Resource found: " + results);
                EventManager.getInstance().setCurrentResources(results);
            }

            // Notifica che la ricerca è completata
            EventManager.getInstance().getEventObservable().notify("search_complete", null);
        } catch (Exception e) {
            System.err.println("[ERROR] Error fetching resource: " + e.getMessage());
            e.printStackTrace();
        }
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
                        map.put("ID", String.valueOf(doc.get("id"))); // ID
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

    private void fetchEncounterResources(String encounterId) {
        System.out.println("[DEBUG] fetchEncounterResources started for encounterId: " + encounterId);

        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
            System.out.println("[DEBUG] Connected to MongoDB for encounter resources.");

            MongoDatabase database = mongoClient.getDatabase("medicalData");
            MongoCollection<Document> collection = database.getCollection("encounters");
            System.out.println("[DEBUG] Using database 'medicalData' and collection 'encounters'.");

            // Esegui la query per ottenere i dettagli dell'incontro
            Document encounter = collection.find(eq("id", encounterId)).first(); // Usa "id" invece di "encounterId"
            System.out.println("[DEBUG] Query executed. Encounter found: " + (encounter != null));

            if (encounter != null) {
                Map<String, String> encounterMap = new HashMap<>();
                encounterMap.put("Encounter ID", encounter.getString("id"));
                encounterMap.put("Patient Reference", encounter.get("subject", Document.class).getString("reference"));
                encounterMap.put("Service Provider", encounter.get("serviceProvider", Document.class).getString("reference"));
                encounterMap.put("Start Date", encounter.get("actualPeriod", Document.class).getString("start"));
                encounterMap.put("End Date", encounter.get("actualPeriod", Document.class).getString("end"));
                encounterMap.put("Type", encounter.get("type", List.class).toString());

                // Log dei dati estratti
                System.out.println("[DEBUG] Encounter data extracted: " + encounterMap);

                // Aggiorna le risorse correnti con i dati dell'incontro
                setCurrentResources(List.of(encounterMap));
            } else {
                System.out.println("[DEBUG] No encounter found with ID: " + encounterId);
            }

            // Notifica che la ricerca è completata
            eventObservable.notify("search_complete", null);
        } catch (Exception e) {
            System.err.println("[ERROR] Error fetching encounter with ID: " + encounterId);
            e.printStackTrace();
            eventObservable.notify("error", null);
        }
    }


    private void fetchPatientResources(String resId) {
        // Rimuovi eventuali caratteri non validi
        resId = resId.replace("]", "").trim();
        System.out.println("[DEBUG] fetchPatientResources started for resId: " + resId);

        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
            System.out.println("[DEBUG] Connected to MongoDB for patient resources.");

            MongoDatabase database = mongoClient.getDatabase("medicalData");
            MongoCollection<Document> collection = database.getCollection("patients");
            System.out.println("[DEBUG] Using database 'medicalData' and collection 'patients'.");
            searchResourcesForPatient("Patient",resId);

            // Esegui la query per cercare il paziente nel campo `identifier.value`
            Document patient = collection.find(eq("Id", resId)).first();
            System.out.println("[DEBUG] Query executed. Patient found: " + (patient != null));

            if (patient != null) {
                // Mappa i dati del paziente
                Map<String, String> patientMap = new HashMap<>();
                patientMap.put("ID", patient.getString("Id")); // ID della risorsa
                patientMap.put("Name", patient.get("name", List.class).toString()); // Nome del paziente
                patientMap.put("Gender", patient.getString("gender")); // Genere
                patientMap.put("BirthDate", patient.getString("birthDate")); // Data di nascita
                patientMap.put("Address", patient.get("address", List.class).toString()); // Indirizzo
                patientMap.put("Identifier", patient.get("identifier", List.class).toString()); // Identificatori

                // Log dei dati trovati
                System.out.println("[DEBUG] Patient data extracted: " + patientMap);

                // Aggiorna le risorse correnti con i dati del paziente
                //setCurrentResources(List.of(patientMap));
            } else {
                System.out.println("[DEBUG] No patient resources found for resId: " + resId);
            }

            // Notifica che la ricerca è completa
            eventObservable.notify("search_complete", null);
        } catch (Exception e) {
            System.err.println("[ERROR] Error fetching patient resources: " + e.getMessage());
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
        // ID dell'osservazione
        map.put("ID", observation.getIdElement().getIdPart());

        // Stato dell'osservazione
        map.put("Status", observation.hasStatus() ? observation.getStatus().toCode() : "N/A");

        // Meta informazioni (versionId e lastUpdated)
        if (observation.hasMeta()) {
            org.hl7.fhir.r5.model.Meta meta = observation.getMeta();
            map.put("Meta Version ID", meta.hasVersionId() ? meta.getVersionId() : "N/A");
            map.put("Meta Last Updated", meta.hasLastUpdated() ? meta.getLastUpdated().toString() : "N/A");
        } else {
            map.put("Meta Version ID", "N/A");
            map.put("Meta Last Updated", "N/A");
        }

        // Codice dell'osservazione
        if (observation.hasCode()) {
            map.put("Code Text", observation.getCode().hasText() ? observation.getCode().getText() : "N/A");
            if (observation.getCode().hasCoding() && !observation.getCode().getCoding().isEmpty()) {
                org.hl7.fhir.r5.model.Coding coding = observation.getCode().getCodingFirstRep();
                map.put("Code", coding.hasCode() ? coding.getCode() : "N/A");
            } else {
                map.put("Code", "N/A");
            }
        } else {
            map.put("Code Text", "N/A");
            map.put("Code", "N/A");
        }

        // Riferimento Encounter
        if (observation.hasEncounter()) {
            map.put("Encounter Reference", observation.getEncounter().hasReference() ? observation.getEncounter().getReference() : "N/A");
        } else {
            map.put("Encounter Reference", "N/A");
        }

        // Data effettiva
        map.put("Effective Date", observation.hasEffectiveDateTimeType() ? observation.getEffectiveDateTimeType().toHumanDisplay() : "N/A");

        // Valore quantitativo dell'osservazione
        if (observation.hasValueQuantity()) {
            org.hl7.fhir.r5.model.Quantity quantity = observation.getValueQuantity();
            map.put("Value", quantity.hasValue() ? quantity.getValue().toPlainString() : "N/A");
            map.put("Unit", quantity.hasUnit() ? quantity.getUnit() : "N/A");
        } else {
            map.put("Value", "N/A");
            map.put("Unit", "N/A");
        }
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
        // ID della procedura
        map.put("ID", procedure.getIdElement().getIdPart());

        // Stato della procedura
        map.put("Status", procedure.hasStatus() ? procedure.getStatus().toCode() : "N/A");

        // Meta informazioni (versionId e lastUpdated)
        if (procedure.hasMeta()) {
            org.hl7.fhir.r5.model.Meta meta = procedure.getMeta();
            map.put("Meta Version ID", meta.hasVersionId() ? meta.getVersionId() : "N/A");
            map.put("Meta Last Updated", meta.hasLastUpdated() ? meta.getLastUpdated().toString() : "N/A");
        } else {
            map.put("Meta Version ID", "N/A");
            map.put("Meta Last Updated", "N/A");
        }

        // Codice della procedura
        if (procedure.hasCode() && procedure.getCode().hasCoding()) {
            org.hl7.fhir.r5.model.Coding coding = procedure.getCode().getCodingFirstRep();
            map.put("Procedure Code", coding.hasCode() ? coding.getCode() : "N/A");
            map.put("Procedure Display", coding.hasDisplay() ? coding.getDisplay() : "N/A");
        } else {
            map.put("Procedure Code", "N/A");
            map.put("Procedure Display", "N/A");
        }

        // Occurrence date della procedura
        map.put("Occurrence Date", procedure.hasOccurrenceDateTimeType() ? procedure.getOccurrenceDateTimeType().toHumanDisplay() : "N/A");

        // Estensioni della procedura
        if (procedure.hasExtension()) {
            for (org.hl7.fhir.r5.model.Extension extension : procedure.getExtension()) {
                if ("http://hl7.org/fhir/StructureDefinition/base-cost".equals(extension.getUrl()) && extension.hasValueQuantity()) {
                    org.hl7.fhir.r5.model.Quantity quantity = extension.getValueQuantity();
                    map.put("Base Cost", quantity.hasValue() ? quantity.getValue().toPlainString() : "N/A");
                }
            }
        } else {
            map.put("Base Cost", "N/A");
        }

        // Riferimento Encounter
        map.put("Encounter Reference", procedure.hasEncounter() ? procedure.getEncounter().getReference() : "N/A");
    }


    private void handleAllergyIntolerance(org.hl7.fhir.r5.model.AllergyIntolerance allergy, Map<String, String> map) {
        // ID dell'allergia
        map.put("ID", allergy.getIdElement().getIdPart());

        // Stato clinico
        map.put("Clinical Status", allergy.hasClinicalStatus()
                ? allergy.getClinicalStatus().getCodingFirstRep().getCode()
                : "N/A");

        // Stato di verifica
        map.put("Verification Status", allergy.hasVerificationStatus()
                ? allergy.getVerificationStatus().getCodingFirstRep().getCode()
                : "N/A");

        // Meta informazioni (versionId e lastUpdated)
        if (allergy.hasMeta()) {
            org.hl7.fhir.r5.model.Meta meta = allergy.getMeta();
            map.put("Meta Version ID", meta.hasVersionId() ? meta.getVersionId() : "N/A");
            map.put("Meta Last Updated", meta.hasLastUpdated() ? meta.getLastUpdated().toString() : "N/A");
        } else {
            map.put("Meta Version ID", "N/A");
            map.put("Meta Last Updated", "N/A");
        }

        // Codice dell'allergia
        if (allergy.hasCode() && allergy.getCode().hasCoding()) {
            org.hl7.fhir.r5.model.Coding coding = allergy.getCode().getCodingFirstRep();
            map.put("Allergy Code", coding.hasCode() ? coding.getCode() : "N/A");
            map.put("Allergy Display", coding.hasDisplay() ? coding.getDisplay() : "N/A");
        } else {
            map.put("Allergy Code", "N/A");
            map.put("Allergy Display", "N/A");
        }

        // Data di registrazione
        map.put("Recorded Date", allergy.hasRecordedDate() ? allergy.getRecordedDate().toString() : "N/A");

        // Estensioni
        if (allergy.hasExtension()) {
            for (org.hl7.fhir.r5.model.Extension extension : allergy.getExtension()) {
                if ("http://hl7.org/fhir/StructureDefinition/encounter-reference".equals(extension.getUrl())
                        && extension.hasValueReference()) {
                    map.put("Encounter Reference", extension.getValueReference().getReference());
                }
            }
        } else {
            map.put("Encounter Reference", "N/A");
        }
    }


    private void handleCarePlan(org.hl7.fhir.r5.model.CarePlan carePlan, Map<String, String> map) {
        // ID del CarePlan
        map.put("ID", carePlan.getIdElement().getIdPart());

        // Stato del CarePlan
        map.put("Status", carePlan.hasStatus() ? carePlan.getStatus().toCode() : "N/A");

        // Categoria del CarePlan
        if (carePlan.hasCategory() && !carePlan.getCategory().isEmpty()) {
            org.hl7.fhir.r5.model.Coding categoryCoding = carePlan.getCategoryFirstRep().getCodingFirstRep();
            map.put("Category Code", categoryCoding.hasCode() ? categoryCoding.getCode() : "N/A");
            map.put("Category Display", categoryCoding.hasDisplay() ? categoryCoding.getDisplay() : "N/A");
        } else {
            map.put("Category Code", "N/A");
            map.put("Category Display", "N/A");
        }

        // Meta informazioni (versionId e lastUpdated)
        if (carePlan.hasMeta()) {
            org.hl7.fhir.r5.model.Meta meta = carePlan.getMeta();
            map.put("Meta Version ID", meta.hasVersionId() ? meta.getVersionId() : "N/A");
            map.put("Meta Last Updated", meta.hasLastUpdated() ? meta.getLastUpdated().toString() : "N/A");
        } else {
            map.put("Meta Version ID", "N/A");
            map.put("Meta Last Updated", "N/A");
        }

        // Periodo del CarePlan (start e end date)
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

        // Riferimento all'Encounter
        map.put("Encounter Reference", carePlan.hasEncounter() ? carePlan.getEncounter().getReference() : "N/A");

        // Identificatori
        if (carePlan.hasIdentifier()) {
            StringBuilder identifiers = new StringBuilder();
            for (org.hl7.fhir.r5.model.Identifier identifier : carePlan.getIdentifier()) {
                if (identifier.hasValue()) {
                    identifiers.append(identifier.getValue()).append(", ");
                }
            }
            map.put("Identifiers", identifiers.length() > 0
                    ? identifiers.substring(0, identifiers.length() - 2)
                    : "N/A");
        } else {
            map.put("Identifiers", "N/A");
        }


    }


    private void handleCondition(org.hl7.fhir.r5.model.Condition condition, Map<String, String> map) {
        // ID della condizione
        map.put("ID", condition.getIdElement().getIdPart());

        // Clinical Status
        map.put("Clinical Status", condition.hasClinicalStatus() && condition.getClinicalStatus().hasCoding()
                ? condition.getClinicalStatus().getCodingFirstRep().getCode()
                : "N/A");

        // Verification Status
        map.put("Verification Status", condition.hasVerificationStatus() && condition.getVerificationStatus().hasCoding()
                ? condition.getVerificationStatus().getCodingFirstRep().getCode()
                : "N/A");

        // Codice della condizione
        if (condition.hasCode() && condition.getCode().hasCoding()) {
            org.hl7.fhir.r5.model.Coding code = condition.getCode().getCodingFirstRep();
            map.put("Condition Code", code.hasCode() ? code.getCode() : "N/A");
            map.put("Condition Display", code.hasDisplay() ? code.getDisplay() : "N/A");
        } else {
            map.put("Condition Code", "N/A");
            map.put("Condition Display", "N/A");
        }

        // Meta informazioni
        if (condition.hasMeta()) {
            org.hl7.fhir.r5.model.Meta meta = condition.getMeta();
            map.put("Meta Version ID", meta.hasVersionId() ? meta.getVersionId() : "N/A");
            map.put("Meta Last Updated", meta.hasLastUpdated() ? meta.getLastUpdated().toString() : "N/A");
        } else {
            map.put("Meta Version ID", "N/A");
            map.put("Meta Last Updated", "N/A");
        }

        // Onset Date
        map.put("Onset Date", condition.hasOnsetDateTimeType() && condition.getOnsetDateTimeType().hasValue()
                ? condition.getOnsetDateTimeType().toHumanDisplay()
                : "N/A");

        // Encounter Reference
        map.put("Encounter Reference", condition.hasEncounter() ? condition.getEncounter().getReference() : "N/A");
    }


    private void handleImagingStudy(org.hl7.fhir.r5.model.ImagingStudy imagingStudy, Map<String, String> map) {
        // ID dello studio
        map.put("ID", imagingStudy.getIdElement().getIdPart());

        // Status dello studio
        map.put("Status", imagingStudy.hasStatus() ? imagingStudy.getStatus().toCode() : "N/A");

        // Meta informazioni
        if (imagingStudy.hasMeta()) {
            org.hl7.fhir.r5.model.Meta meta = imagingStudy.getMeta();
            map.put("Meta Version ID", meta.hasVersionId() ? meta.getVersionId() : "N/A");
            map.put("Meta Last Updated", meta.hasLastUpdated() ? meta.getLastUpdated().toString() : "N/A");
        } else {
            map.put("Meta Version ID", "N/A");
            map.put("Meta Last Updated", "N/A");
        }

        // Identificatore dello studio
        if (imagingStudy.hasIdentifier() && !imagingStudy.getIdentifier().isEmpty()) {
            map.put("Identifier", imagingStudy.getIdentifierFirstRep().hasValue()
                    ? imagingStudy.getIdentifierFirstRep().getValue()
                    : "N/A");
        } else {
            map.put("Identifier", "N/A");
        }

        // Data di inizio dello studio
        map.put("Started", imagingStudy.hasStarted() ? imagingStudy.getStartedElement().toHumanDisplay() : "N/A");

        // Encounter associato
        map.put("Encounter Reference", imagingStudy.hasEncounter() ? imagingStudy.getEncounter().getReference() : "N/A");

        // Serie dello studio
        if (imagingStudy.hasSeries() && !imagingStudy.getSeries().isEmpty()) {
            org.hl7.fhir.r5.model.ImagingStudy.ImagingStudySeriesComponent series = imagingStudy.getSeriesFirstRep();

            // Modalità (es. Ultrasound)
            if (series.hasModality() && series.getModality().hasCoding()) {
                map.put("Series Modality Code", series.getModality().getCodingFirstRep().getCode());
                map.put("Series Modality Display", series.getModality().getCodingFirstRep().getDisplay());
            } else {
                map.put("Series Modality Code", "N/A");
                map.put("Series Modality Display", "N/A");
            }

            // Body Site (es. Heart structure)
            if (series.hasBodySite() && series.getBodySite().hasConcept() && series.getBodySite().getConcept().hasCoding()) {
                map.put("Body Site Code", series.getBodySite().getConcept().getCodingFirstRep().getCode());
                map.put("Body Site Display", series.getBodySite().getConcept().getCodingFirstRep().getDisplay());
            } else {
                map.put("Body Site Code", "N/A");
                map.put("Body Site Display", "N/A");
            }

            // Istanza della serie
            if (series.hasInstance() && !series.getInstance().isEmpty()) {
                org.hl7.fhir.r5.model.ImagingStudy.ImagingStudySeriesInstanceComponent instance = series.getInstanceFirstRep();
                if (instance.hasSopClass()) {
                    map.put("Instance SOP Class Code", instance.getSopClass().getCode());
                    map.put("Instance SOP Class Display", instance.getSopClass().getDisplay());
                } else {
                    map.put("Instance SOP Class Code", "N/A");
                    map.put("Instance SOP Class Display", "N/A");
                }
            } else {
                map.put("Instance SOP Class Code", "N/A");
                map.put("Instance SOP Class Display", "N/A");
            }
        } else {
            map.put("Series Modality Code", "N/A");
            map.put("Series Modality Display", "N/A");
            map.put("Body Site Code", "N/A");
            map.put("Body Site Display", "N/A");
            map.put("Instance SOP Class Code", "N/A");
            map.put("Instance SOP Class Display", "N/A");
        }
    }


    private void handleImmunization(org.hl7.fhir.r5.model.Immunization immunization, Map<String, String> map) {
        // ID dell'immunizzazione
        map.put("ID", immunization.getIdElement().getIdPart());

        // Stato dell'immunizzazione
        map.put("Status", immunization.hasStatus() ? immunization.getStatus().toCode() : "N/A");

        // Codice e descrizione del vaccino
        if (immunization.hasVaccineCode() && immunization.getVaccineCode().hasCoding()) {
            map.put("Vaccine Code", immunization.getVaccineCode().getCodingFirstRep().getCode());
            map.put("Vaccine Display", immunization.getVaccineCode().getCodingFirstRep().getDisplay());
        } else {
            map.put("Vaccine Code", "N/A");
            map.put("Vaccine Display", "N/A");
        }

        // Data dell'occorrenza
        map.put("Occurrence Date", immunization.hasOccurrenceDateTimeType()
                ? immunization.getOccurrenceDateTimeType().toHumanDisplay()
                : "N/A");

        // Riferimento all'Encounter associato
        map.put("Encounter Reference", immunization.hasEncounter()
                ? immunization.getEncounter().getReference()
                : "N/A");

        // Costo base (estensione)
        if (immunization.hasExtension()) {
            for (org.hl7.fhir.r5.model.Extension extension : immunization.getExtension()) {
                if ("http://hl7.org/fhir/StructureDefinition/base-cost".equals(extension.getUrl())
                        && extension.hasValueQuantity()) {
                    org.hl7.fhir.r5.model.Quantity baseCost = extension.getValueQuantity();
                    map.put("Base Cost", baseCost.getValue().toPlainString());
                    break;
                }
            }
        } else {
            map.put("Base Cost", "N/A");
        }
    }


    private void handleMedicationRequest(org.hl7.fhir.r5.model.MedicationRequest medicationRequest, Map<String, String> map) {
        // ID della richiesta
        map.put("ID", medicationRequest.getIdElement().getIdPart());

        // Stato della richiesta
        map.put("Status", medicationRequest.hasStatus() ? medicationRequest.getStatus().toCode() : "N/A");

        // Codice e descrizione del farmaco
        if (medicationRequest.hasMedication() && medicationRequest.getMedication().getConcept() != null
                && medicationRequest.getMedication().getConcept().hasCoding()) {
            map.put("Medication Code", medicationRequest.getMedication().getConcept().getCodingFirstRep().getCode());
            map.put("Medication Display", medicationRequest.getMedication().getConcept().getCodingFirstRep().getDisplay());
        } else {
            map.put("Medication Code", "N/A");
            map.put("Medication Display", "N/A");
        }

        // Riferimento all'Encounter associato
        map.put("Encounter Reference", medicationRequest.hasEncounter()
                ? medicationRequest.getEncounter().getReference()
                : "N/A");

        // Costo base e costo totale (estensioni)
        if (medicationRequest.hasExtension()) {
            for (org.hl7.fhir.r5.model.Extension extension : medicationRequest.getExtension()) {
                if ("http://hl7.org/fhir/StructureDefinition/base-cost".equals(extension.getUrl())
                        && extension.hasValueQuantity()) {
                    org.hl7.fhir.r5.model.Quantity baseCost = extension.getValueQuantity();
                    map.put("Base Cost", baseCost.getValue().toPlainString());
                } else if ("http://hl7.org/fhir/StructureDefinition/total-cost".equals(extension.getUrl())
                        && extension.hasValueQuantity()) {
                    org.hl7.fhir.r5.model.Quantity totalCost = extension.getValueQuantity();
                    map.put("Total Cost", totalCost.getValue().toPlainString());
                }
            }
        }

        // Valori predefiniti se i costi non sono presenti
        map.putIfAbsent("Base Cost", "N/A");
        map.putIfAbsent("Total Cost", "N/A");
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







}
