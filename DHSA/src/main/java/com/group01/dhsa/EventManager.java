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
        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
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
        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
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
                org.hl7.fhir.r5.model.Observation observation = (org.hl7.fhir.r5.model.Observation) resource;
                map.put("ID", observation.getIdElement().getIdPart());

                // Aggiungi il codice testuale
                String text = observation.getCode().hasText() ? observation.getCode().getText() : "N/A";
                map.put("Code Text", text);

                // Estrai il valore e l'unità di misura
                if (observation.hasValueQuantity()) {
                    org.hl7.fhir.r5.model.Quantity quantity = observation.getValueQuantity();
                    String valueWithUnit = quantity.getValue().toPlainString() + " " + quantity.getUnit();
                    map.put("Value", valueWithUnit);
                } else {
                    map.put("Value", "N/A");
                }

                // Estrai la data
                if (observation.hasEffectiveDateTimeType()) {
                    map.put("Effective Date", observation.getEffectiveDateTimeType().toHumanDisplay());
                } else {
                    map.put("Effective Date", "N/A");
                }

            } else if (resource instanceof org.hl7.fhir.r5.model.Encounter) {
                org.hl7.fhir.r5.model.Encounter encounter = (org.hl7.fhir.r5.model.Encounter) resource;

                // ID Encounter
                map.put("ID", encounter.getIdElement().getIdPart());

                // Estrai l'identificativo
                if (encounter.hasIdentifier() && !encounter.getIdentifier().isEmpty()) {
                    String identifier = encounter.getIdentifierFirstRep().getValue();
                    map.put("Identifier", identifier);
                } else {
                    map.put("Identifier", "N/A");
                }

                if (encounter.hasType() && !encounter.getType().isEmpty()) {
                    String typeDisplay = encounter.getType().stream()
                            .map(type -> type.hasCoding() && type.getCodingFirstRep().hasDisplay()
                                    ? type.getCodingFirstRep().getDisplay()
                                    : "N/A")
                            .findFirst()
                            .orElse("N/A");
                    map.put("Type", typeDisplay);
                } else {
                    map.put("Type", "N/A");
                }


                // Tipo Encounter
                if (encounter.hasType() && !encounter.getType().isEmpty()) {
                    String type = encounter.getTypeFirstRep().getCodingFirstRep().getDisplay();
                    map.put("Type", type);
                } else {
                    map.put("Type", "N/A");
                }

                // Paziente associato
                if (encounter.hasSubject() && encounter.getSubject().getReference() != null) {
                    String patientReference = encounter.getSubject().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
                    org.hl7.fhir.r5.model.Patient patient = resolvePatientReference(patientReference);
                    if (patient != null) {
                        map.put("Patient Name", patient.getNameFirstRep().getNameAsSingleString());
                        map.put("Patient Gender", patient.getGender() != null ? patient.getGender().toCode() : "N/A");
                    } else {
                        map.put("Patient Name", "N/A");
                        map.put("Patient Gender", "N/A");
                    }
                } else {
                System.out.println("Patien null \n");
                    map.put("Patient Reference", "N/A");
                    map.put("Patient Name", "N/A");
                    map.put("Patient Gender", "N/A");
                }

                // Service Provider
                if (encounter.hasServiceProvider() && encounter.getServiceProvider().getReference() != null) {
                    String serviceProviderReference = encounter.getServiceProvider().getReference();
                    map.put("Service Provider", serviceProviderReference);
                } else {
                    map.put("Service Provider", "N/A");
                }

                // Partecipanti
                if (encounter.hasParticipant() && !encounter.getParticipant().isEmpty()) {
                    String participantReference = encounter.getParticipantFirstRep().getActor().getReference();
                    map.put("Participant", participantReference);
                } else {
                    map.put("Participant", "N/A");
                }

                // Periodo Effettivo
                if (encounter.hasActualPeriod()) {
                    map.put("Start Date", encounter.getActualPeriod().getStartElement().toHumanDisplay());
                    map.put("End Date", encounter.getActualPeriod().getEndElement() != null
                            ? encounter.getActualPeriod().getEndElement().toHumanDisplay()
                            : "N/A");
                } else {
                    map.put("Start Date", "N/A");
                    map.put("End Date", "N/A");
                }

                // Estensioni (cost, total claim cost, etc.)
                if (encounter.hasExtension()) {
                    encounter.getExtension().forEach(extension -> {
                        String url = extension.getUrl();
                        if (url.contains("base-encounter-cost")) {
                            map.put("Base Encounter Cost", String.valueOf(extension.getValueQuantity().getValue()));
                        } else if (url.contains("total-claim-cost")) {
                            map.put("Total Claim Cost", String.valueOf(extension.getValueQuantity().getValue()));
                        } else if (url.contains("payer-coverage")) {
                            map.put("Payer Coverage", String.valueOf(extension.getValueQuantity().getValue()));
                        }
                    });
                }
            } else if (resource instanceof org.hl7.fhir.r5.model.Procedure) {
                org.hl7.fhir.r5.model.Procedure procedure = (org.hl7.fhir.r5.model.Procedure) resource;

                // ID Procedure
                map.put("ID", procedure.getIdElement().getIdPart());

                // Stato della procedura
                map.put("Status", procedure.hasStatus()
                        ? procedure.getStatus().toCode()
                        : "N/A");

                // Codice della procedura
                if (procedure.hasCode() && procedure.getCode().hasCoding()) {
                    map.put("Procedure Code", procedure.getCode().getCodingFirstRep().getCode());
                    map.put("Procedure Display", procedure.getCode().getCodingFirstRep().getDisplay());
                } else {
                    map.put("Procedure Code", "N/A");
                    map.put("Procedure Display", "N/A");
                }

                // Paziente associato
                if (procedure.hasSubject()) {
                    String patientReference = procedure.getSubject().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
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

                // Occurrence DateTime
                map.put("Occurrence Date", procedure.hasOccurrenceDateTimeType()
                        ? procedure.getOccurrenceDateTimeType().toHumanDisplay()
                        : "N/A");

                // Estensioni (base cost)
                if (procedure.hasExtension()) {
                    procedure.getExtension().forEach(extension -> {
                        if ("http://hl7.org/fhir/StructureDefinition/base-cost".equals(extension.getUrl())) {
                            String baseCost = extension.getValueQuantity().getValue().toPlainString();
                            map.put("Base Cost", baseCost);
                        }
                    });
                } else {
                    map.put("Base Cost", "N/A");
                }

                // Encounter associato
                if (procedure.hasEncounter()) {
                    map.put("Encounter Reference", procedure.getEncounter().getReference());
                } else {
                    map.put("Encounter Reference", "N/A");
                }
            }
            else if (resource instanceof org.hl7.fhir.r5.model.AllergyIntolerance) {
                org.hl7.fhir.r5.model.AllergyIntolerance allergy = (org.hl7.fhir.r5.model.AllergyIntolerance) resource;

                // ID AllergyIntolerance
                map.put("ID", allergy.getIdElement().getIdPart());

                // Stato clinico
                map.put("Clinical Status", allergy.hasClinicalStatus()
                        ? allergy.getClinicalStatus().getCodingFirstRep().getCode()
                        : "N/A");

                // Stato di verifica
                map.put("Verification Status", allergy.hasVerificationStatus()
                        ? allergy.getVerificationStatus().getCodingFirstRep().getCode()
                        : "N/A");

                // Codice allergia
                if (allergy.hasCode() && allergy.getCode().hasCoding()) {
                    map.put("Allergy Code", allergy.getCode().getCodingFirstRep().getCode());
                    map.put("Allergy Display", allergy.getCode().getCodingFirstRep().getDisplay());
                } else {
                    map.put("Allergy Code", "N/A");
                    map.put("Allergy Display", "N/A");
                }

                // Paziente associato
                if (allergy.hasPatient()) {
                    String patientReference = allergy.getPatient().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
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

                // Data registrata
                map.put("Recorded Date", allergy.hasRecordedDate()
                        ? allergy.getRecordedDate().toString()
                        : "N/A");

                // Estensioni (riferimento Encounter)
                if (allergy.hasExtension()) {
                    allergy.getExtension().forEach(extension -> {
                        if ("http://hl7.org/fhir/StructureDefinition/encounter-reference".equals(extension.getUrl())) {
                            String encounterReference = extension.getValueReference().getReference();
                            map.put("Encounter Reference", encounterReference);
                        }
                    });
                } else {
                    map.put("Encounter Reference", "N/A");
                }
            }
            else if (resource instanceof org.hl7.fhir.r5.model.CarePlan) {
                org.hl7.fhir.r5.model.CarePlan carePlan = (org.hl7.fhir.r5.model.CarePlan) resource;

                // ID CarePlan
                map.put("ID", carePlan.getIdElement().getIdPart());

                // Stato della CarePlan
                map.put("Status", carePlan.hasStatus() ? carePlan.getStatus().toCode() : "N/A");

                // Categoria
                if (carePlan.hasCategory() && !carePlan.getCategory().isEmpty()) {
                    String categoryDisplay = carePlan.getCategoryFirstRep().getCodingFirstRep().getDisplay();
                    map.put("Category", categoryDisplay);
                } else {
                    map.put("Category", "N/A");
                }

                // Paziente associato
                if (carePlan.hasSubject()) {
                    String patientReference = carePlan.getSubject().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente
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

                // Periodo della CarePlan
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

                // Problemi indirizzati dalla CarePlan
                if (carePlan.hasAddresses() && !carePlan.getAddresses().isEmpty()) {
                    String addressedConditions = carePlan.getAddresses().stream()
                            .map(address -> address.getConcept().hasCoding()
                                    ? address.getConcept().getCodingFirstRep().getDisplay()
                                    : "N/A")
                            .collect(Collectors.joining(", "));
                    map.put("Addresses", addressedConditions);
                } else {
                    map.put("Addresses", "N/A");
                }

                // Riferimento all'Encounter associato
                map.put("Encounter Reference", carePlan.hasEncounter() ? carePlan.getEncounter().getReference() : "N/A");
            } else if (resource instanceof org.hl7.fhir.r5.model.Condition) {
                org.hl7.fhir.r5.model.Condition condition = (org.hl7.fhir.r5.model.Condition) resource;

                // ID Condition
                map.put("ID", condition.getIdElement().getIdPart());

                // Clinical Status
                if (condition.hasClinicalStatus() && condition.getClinicalStatus().hasCoding()) {
                    String clinicalStatus = condition.getClinicalStatus().getCodingFirstRep().getCode();
                    map.put("Clinical Status", clinicalStatus);
                } else {
                    map.put("Clinical Status", "N/A");
                }

                // Verification Status
                if (condition.hasVerificationStatus() && condition.getVerificationStatus().hasCoding()) {
                    String verificationStatus = condition.getVerificationStatus().getCodingFirstRep().getCode();
                    map.put("Verification Status", verificationStatus);
                } else {
                    map.put("Verification Status", "N/A");
                }

                // Codice e descrizione della condizione
                if (condition.hasCode() && condition.getCode().hasCoding()) {
                    String codeDisplay = condition.getCode().getCodingFirstRep().getDisplay();
                    map.put("Code", codeDisplay);
                } else {
                    map.put("Code", "N/A");
                }

                // Paziente associato
                if (condition.hasSubject()) {
                    String patientReference = condition.getSubject().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
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

                // Onset Date
                if (condition.hasOnsetDateTimeType() && condition.getOnsetDateTimeType().hasValue()) {
                    map.put("Onset Date", condition.getOnsetDateTimeType().toHumanDisplay());
                } else {
                    map.put("Onset Date", "N/A");
                }

                // Riferimento all'Encounter associato
                map.put("Encounter Reference", condition.hasEncounter() ? condition.getEncounter().getReference() : "N/A");
            }else if (resource instanceof org.hl7.fhir.r5.model.ImagingStudy) {
                org.hl7.fhir.r5.model.ImagingStudy imagingStudy = (org.hl7.fhir.r5.model.ImagingStudy) resource;

                // ID ImagingStudy
                map.put("ID", imagingStudy.getIdElement().getIdPart());

                // Stato
                map.put("Status", imagingStudy.hasStatus() ? imagingStudy.getStatus().toCode() : "N/A");

                // Paziente associato
                if (imagingStudy.hasSubject()) {
                    String patientReference = imagingStudy.getSubject().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
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

                // Data di inizio dello studio
                map.put("Started", imagingStudy.hasStarted() ? imagingStudy.getStartedElement().toHumanDisplay() : "N/A");

                // Riferimento all'Encounter associato
                map.put("Encounter Reference", imagingStudy.hasEncounter() ? imagingStudy.getEncounter().getReference() : "N/A");

                // Dettagli della serie
                if (imagingStudy.hasSeries() && !imagingStudy.getSeries().isEmpty()) {
                    org.hl7.fhir.r5.model.ImagingStudy.ImagingStudySeriesComponent series = imagingStudy.getSeriesFirstRep();

                    // Modality
                    if (series.hasModality() && series.getModality().hasCoding()) {
                        String modalityDisplay = series.getModality().getCodingFirstRep().getDisplay();
                        map.put("Modality", modalityDisplay);
                    } else {
                        map.put("Modality", "N/A");
                    }

                    // Body Site
                    if (series.hasBodySite() && series.getBodySite().getConcept().hasCoding()) {
                        String bodySiteDisplay = series.getBodySite().getConcept().getCodingFirstRep().getDisplay();
                        map.put("Body Site", bodySiteDisplay);
                    } else {
                        map.put("Body Site", "N/A");
                    }

                    // Dettagli dell'istanza
                    if (series.hasInstance() && !series.getInstance().isEmpty()) {
                        org.hl7.fhir.r5.model.ImagingStudy.ImagingStudySeriesInstanceComponent instance = series.getInstanceFirstRep();
                        if (instance.hasSopClass()) {
                            String sopClassDisplay = instance.getSopClass().getDisplay();
                            map.put("Instance SOP Class", sopClassDisplay);
                        } else {
                            map.put("Instance SOP Class", "N/A");
                        }
                    } else {
                        map.put("Instance SOP Class", "N/A");
                    }
                } else {
                    map.put("Modality", "N/A");
                    map.put("Body Site", "N/A");
                    map.put("Instance SOP Class", "N/A");
                }
            }else if (resource instanceof org.hl7.fhir.r5.model.Immunization) {
                org.hl7.fhir.r5.model.Immunization immunization = (org.hl7.fhir.r5.model.Immunization) resource;

                // ID Immunization
                map.put("ID", immunization.getIdElement().getIdPart());

                // Stato dell'immunizzazione
                map.put("Status", immunization.hasStatus() ? immunization.getStatus().toCode() : "N/A");

                // Codice e descrizione del vaccino
                if (immunization.hasVaccineCode() && immunization.getVaccineCode().hasCoding()) {
                    String vaccineCode = immunization.getVaccineCode().getCodingFirstRep().getCode();
                    String vaccineDisplay = immunization.getVaccineCode().getCodingFirstRep().getDisplay();
                    map.put("Vaccine Code", vaccineCode);
                    map.put("Vaccine Display", vaccineDisplay);
                } else {
                    map.put("Vaccine Code", "N/A");
                    map.put("Vaccine Display", "N/A");
                }

                // Paziente associato
                if (immunization.hasPatient()) {
                    String patientReference = immunization.getPatient().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
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

                // Data dell'immunizzazione
                map.put("Occurrence Date", immunization.hasOccurrenceDateTimeType() ? immunization.getOccurrenceDateTimeType().toHumanDisplay() : "N/A");

                // Riferimento all'Encounter associato
                map.put("Encounter Reference", immunization.hasEncounter() ? immunization.getEncounter().getReference() : "N/A");

                // Costo (estensione)
                if (immunization.hasExtension()) {
                    immunization.getExtension().forEach(extension -> {
                        if ("http://hl7.org/fhir/StructureDefinition/base-cost".equals(extension.getUrl())) {
                            map.put("Cost", String.valueOf(extension.getValueQuantity().getValue()));
                        }
                    });
                } else {
                    map.put("Cost", "N/A");
                }
            }else if (resource instanceof org.hl7.fhir.r5.model.MedicationRequest) {
                org.hl7.fhir.r5.model.MedicationRequest medicationRequest = (org.hl7.fhir.r5.model.MedicationRequest) resource;

                // ID MedicationRequest
                map.put("ID", medicationRequest.getIdElement().getIdPart());

                // Stato della richiesta
                map.put("Status", medicationRequest.hasStatus() ? medicationRequest.getStatus().toCode() : "N/A");

                // Dettagli del farmaco
                if (medicationRequest.hasMedication() && medicationRequest.getMedication().getConcept() != null &&
                        medicationRequest.getMedication().getConcept().hasCoding()) {
                    String medicationCode = medicationRequest.getMedication().getConcept().getCodingFirstRep().getCode();
                    String medicationDisplay = medicationRequest.getMedication().getConcept().getCodingFirstRep().getDisplay();
                    map.put("Medication Code", medicationCode);
                    map.put("Medication Display", medicationDisplay);
                } else {
                    map.put("Medication Code", "N/A");
                    map.put("Medication Display", "N/A");
                }

                // Paziente associato
                if (medicationRequest.hasSubject()) {
                    String patientReference = medicationRequest.getSubject().getReference();
                    map.put("Patient Reference", patientReference);

                    // Risolvi i dettagli del paziente se necessario
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

                // Riferimento all'Encounter associato
                map.put("Encounter Reference", medicationRequest.hasEncounter() ? medicationRequest.getEncounter().getReference() : "N/A");

                // Estensioni (costo base e costo totale)
                if (medicationRequest.hasExtension()) {
                    medicationRequest.getExtension().forEach(extension -> {
                        if ("http://hl7.org/fhir/StructureDefinition/base-cost".equals(extension.getUrl())) {
                            map.put("Base Cost", String.valueOf(extension.getValueQuantity().getValue()));
                        } else if ("http://hl7.org/fhir/StructureDefinition/total-cost".equals(extension.getUrl())) {
                            map.put("Total Cost", String.valueOf(extension.getValueQuantity().getValue()));
                        }
                    });
                } else {
                    map.put("Base Cost", "N/A");
                    map.put("Total Cost", "N/A");
                }
            }




            else {
                // Risorsa non gestita
                System.out.println("[DEBUG] Resource type not handled: " + resource.getClass().getSimpleName());
                continue;
            }


            resourceMaps.add(map);
        }

        return resourceMaps;
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
