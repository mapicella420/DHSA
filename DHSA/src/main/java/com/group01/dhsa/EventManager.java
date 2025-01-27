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
            String resourceDetails = file.getPath(); // Usa getPath() per ottenere l'intero percorso come stringa
            System.out.println("[DEBUG] Resource details received: " + resourceDetails);

            try {
                // Normalizza eventuali separatori di percorso (\\ o /)
                resourceDetails = resourceDetails.replace("\\", "/");

                // Estrarre il ResourceType e altre parti
                String[] typeSplit = resourceDetails.split("_Combined");
                if (typeSplit.length != 2) {
                    System.err.println("[ERROR] Invalid resource format: " + resourceDetails);
                    return;
                }

                String resourceType = typeSplit[0]; // ResourceType è prima di "_Combined"
                String resourceSuffix = typeSplit[1]; // Tutto ciò che segue "_Combined"

                // Cerca EncounterId, PatientId, PractitionerId e OrganizationId (se presenti)
                String encounterId = null;
                String patientId = null;
                String practitionerId = null;
                String organizationId = null;

                if (resourceSuffix.contains("/Encounter/")) {
                    String[] encounterSplit = resourceSuffix.split("/Encounter/");
                    if (encounterSplit.length == 2) {
                        resourceSuffix = encounterSplit[1]; // Riduci il suffix dopo Encounter
                        encounterId = encounterSplit[1].split("/")[0].trim();
                        encounterId = encounterId.replace("Encounter/", "").trim(); // Rimuove duplicati se presenti
                    }
                }

                if (resourceSuffix.contains("/Patient/")) {
                    String[] patientSplit = resourceSuffix.split("/Patient/");
                    if (patientSplit.length == 2) {
                        patientId = patientSplit[1].trim();
                        patientId = patientId.replace("Patient/", "").trim(); // Rimuove duplicati se presenti
                    }
                }

                if (resourceSuffix.contains("/Practitioner/")) {
                    String[] practitionerSplit = resourceSuffix.split("/Practitioner/");
                    if (practitionerSplit.length == 2) {
                        practitionerId = practitionerSplit[1].trim();
                        practitionerId = practitionerId.replace("Practitioner/", "").trim(); // Rimuove duplicati se presenti
                    }
                }

                if (resourceSuffix.contains("/Organization/")) {
                    String[] organizationSplit = resourceSuffix.split("/Organization/");
                    if (organizationSplit.length == 2) {
                        organizationId = organizationSplit[1].trim();
                        organizationId = organizationId.replace("Organization/", "").trim(); // Rimuove duplicati se presenti
                    }
                }

                // Debug per verificare i valori estratti
                System.out.println("[DEBUG] Extracted ResourceType: " + resourceType +
                        ", EncounterId: " + (encounterId != null ? encounterId : "N/A") +
                        ", PatientId: " + (patientId != null ? patientId : "N/A") +
                        ", PractitionerId: " + (practitionerId != null ? practitionerId : "N/A") +
                        ", OrganizationId: " + (organizationId != null ? organizationId : "N/A"));

                // Determina i parametri per fetchLinkedResource
                if ("Practitioner".equalsIgnoreCase(resourceType) && practitionerId != null) {
                    fetchLinkedResource(resourceType, new String[]{"Practitioner"}, new String[]{practitionerId});
                } else if ("Organization".equalsIgnoreCase(resourceType) && organizationId != null) {
                    fetchLinkedResource(resourceType, new String[]{"Organization"}, new String[]{organizationId});
                } else if (encounterId != null && patientId != null) {
                    fetchLinkedResource(resourceType, new String[]{"Encounter", "Patient"}, new String[]{encounterId, patientId});
                } else if (encounterId != null) {
                    fetchLinkedResource(resourceType, new String[]{"Encounter"}, new String[]{encounterId});
                } else if (patientId != null) {
                    fetchLinkedResource(resourceType, new String[]{"Patient"}, new String[]{patientId});
                } else {
                    System.err.println("[ERROR] No valid IDs found. Cannot process resource.");
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Exception while processing resource details: " + e.getMessage());
                e.printStackTrace();
            }
        });
        PatientDataTransfer patientDataTransfer = new PatientDataTransfer(this.eventObservable);
        eventObservable.subscribe("transfer", (eventType, file) -> {
            System.out.println("[DEBUG] Transfer event.:" + file.getName());
            String[] params = file.getName().replace(".txt", "").split(",");
            patientDataTransfer.transferPatient(params[0], params[1], params[2]);
        });

        eventObservable.subscribe("cda_upload_to_other_mongo", cdaUploader);




    }

    private void fetchLinkedResource(String resourceType, String[] searchFields, String[] searchValues) {
        try {
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            if (factory == null) {
                System.err.println("[ERROR] No factory found for resource type: " + resourceType);
                return;
            }

            FhirResourceExporter exporter = factory.createExporter();

            // Esegui la ricerca
            System.out.println("[DEBUG] Fetching resources for Type=" + resourceType +
                    ", Fields=" + String.join(", ", searchFields) +
                    ", Values=" + String.join(", ", searchValues));

            List<Map<String, String>> results = exporter.searchResources(searchFields, searchValues);
            if (results.isEmpty()) {
                System.out.println("[DEBUG] No resource found for Type=" + resourceType +
                        ", Fields=" + String.join(", ", searchFields) +
                        ", Values=" + String.join(", ", searchValues));
            } else {
                System.out.println("[DEBUG] Resources found: " + results);
                setCurrentResources(results);
            }

            // Notifica che la ricerca è completata
            eventObservable.notify("search_complete", null);
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
                // Recupera le risorse per il paziente
                List<?> resources = FHIRClient.getInstance().fetchResourcesForPatient(resourceType, patientId);

                // Passa il tipo di risorsa e le risorse alla funzione aggiornata
                setCurrentResources(convertResourcesToMaps(resourceType, resources));

                // Notifica il completamento
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
     * @param resourceType Il tipo di risorsa FHIR.
     * @param resources    Una lista di risorse FHIR.
     * @return Una lista di mappe rappresentanti i dati delle risorse.
     */
    private List<Map<String, String>> convertResourcesToMaps(String resourceType, List<?> resources) {
        List<Map<String, String>> resourceMaps = new ArrayList<>();

        try {
            // Ottieni il factory appropriato per il tipo di risorsa
            FhirResourceExporterFactory factory = FhirExporterFactoryManager.getFactory(resourceType);
            if (factory == null) {
                System.err.println("[ERROR] Factory non trovato per il tipo di risorsa: " + resourceType);
                return resourceMaps;
            }

            // Crea un exporter per il tipo di risorsa
            FhirResourceExporter exporter = factory.createExporter();

            // Converti ogni risorsa in una mappa usando l'exporter
            for (Object resource : resources) {
                Map<String, String> resourceMap = exporter.convertResourceToMap(resource);
                if (resourceMap != null) {
                    resourceMaps.add(resourceMap);
                } else {
                    System.err.println("[ERROR] Conversione della risorsa fallita per: " + resource.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Errore durante la conversione delle risorse: " + e.getMessage());
            e.printStackTrace();
        }

        return resourceMaps;
    }







}
