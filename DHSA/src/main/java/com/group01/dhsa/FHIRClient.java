package com.group01.dhsa;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.r5.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The FHIRClient class provides methods to interact with a FHIR server
 * to retrieve and manipulate FHIR resources such as Patient, Practitioner, Encounter, Observation, etc.
 * This class follows the Singleton design pattern to ensure a single instance.
 */
public class FHIRClient {

    // Singleton instance of the FHIRClient
    private static FHIRClient instance;

    // HAPI FHIR Generic Client for interacting with the FHIR server
    private IGenericClient client;

    private static String FHIR_SERVER_URL;

    /**
     * Private constructor to initialize the FHIR client and set up the server URL.
     * The FhirContext is used to configure the client for FHIR R5 resources.
     */
    private FHIRClient() {

        FhirContext fhirContext = FhirContext.forR5();
        this.client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);
    }

    /**
     * Provides access to the Singleton instance of FHIRClient.
     *
     * @return The Singleton instance of FHIRClient.
     */
    public static FHIRClient getInstance() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                setFhirServerUrl("http://localhost:8081/fhir");
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                setFhirServerUrl("http://localhost:8080/fhir");
            }
        }

        if (instance == null) {
            instance = new FHIRClient();
        }
        return instance;
    }

    public static void setFhirServerUrl(String fhirServerUrl) {
        FHIR_SERVER_URL = fhirServerUrl;
        if (instance != null) {
            FhirContext fhirContext = FhirContext.forR5();
            instance.client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);
        }
    }


    public static void removeClient() {
        instance = null;
    }

    /**
     * Fetches resources of the given type for a specific patient.
     *
     * @param resourceType The type of the resource (e.g., Observation, Encounter).
     * @param patientId    The ID of the patient.
     * @return A list of resources.
     * @throws IllegalArgumentException if the resource type is unsupported.
     */
    public List<?> fetchResourcesForPatient(String resourceType, String patientId) {
        switch (resourceType) {
            case "Observation":
                return getObservationsForPatient(patientId);
            case "Encounter":
                return getEncountersForPatientIdentifier(patientId);
            case "Procedure":
                return getProceduresForPatient(patientId);
            case "AllergyIntolerance":
                return getAllergyIntolerancesForPatient(patientId);
            case "CarePlan": // Aggiungi il caso per CarePlan
                return getCarePlansForPatient(patientId);
            case "Medications": // Aggiungi il caso per CarePlan
                return getMedicationRequestsForPatient(patientId);
            case "ImagingStudy": // Aggiungi il caso per CarePlan
                return getImagingStudiesForPatient(patientId);
            case "Condition": // Aggiungi il caso per CarePlan
                return getConditionsForPatient(patientId);
            case "Immunization": // Aggiungi il caso per CarePlan
                return getImmunizationsForPatient(patientId);
            default:
                throw new IllegalArgumentException("Unsupported resource type: " + resourceType);
        }
    }

    public List<ImagingStudy> getImagingStudiesForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse ImagingStudy
        Bundle bundle = client.search()
                .forResource(ImagingStudy.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(20) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] ImagingStudy fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No ImagingStudy resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse ImagingStudy
        return bundle.getEntry().stream()
                .map(entry -> (ImagingStudy) entry.getResource()) // Converte le entry in risorse ImagingStudy
                .filter(imagingStudy -> imagingStudy.hasStarted() && imagingStudy.getStartedElement().hasValue()) // Filtro per ImagingStudy validi
                .collect(Collectors.toList());
    }

    /**
     * Fetches a Patient resource from the FHIR server using the given patient identifier.
     *
     * @param patientId The identifier of the Patient.
     * @return The Patient resource.
     */
    public Patient getPatientById(String patientId) {
        System.out.println("[DEBUG] Fetching Patient by ID: " + patientId);
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();

        System.out.println("[DEBUG] Patient fetch bundle: " + bundle);

        if (bundle.hasEntry() && !bundle.getEntry().isEmpty()) {
            Resource resource = bundle.getEntryFirstRep().getResource();
            System.out.println("[DEBUG] Retrieved Patient resource: " + resource);

            return resource instanceof Patient ? (Patient) resource : null;
        } else {
            System.err.println("[ERROR] No Patient found for ID: " + patientId);
            return null;
        }
    }


    /**
     * Fetches a Practitioner resource from the FHIR server using the given practitioner identifier.
     *
     * @param practitionerId The identifier of the Practitioner.
     * @return The Practitioner resource.
     */
    public Practitioner getPractitionerById(String practitionerId) {
        Bundle bundle = client.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(practitionerId))
                .returnBundle(Bundle.class)
                .execute();
        return (Practitioner) bundle.getEntryFirstRep().getResource();
    }

    /**
     * Fetches an Organization resource from the FHIR server using the given organization identifier.
     *
     * @param organizationId The identifier of the Organization.
     * @return The Organization resource.
     */
    public Organization getOrganizationFromId(String organizationId) {
        Bundle bundle = client.search()
                .forResource(Organization.class)
                .where(Organization.RES_ID.exactly().identifier(organizationId))
                .returnBundle(Bundle.class)
                .execute();
        return (Organization) bundle.getEntryFirstRep().getResource();
    }


    /**
     * Fetches an Encounter resource associated with a given Practitioner and Patient.
     *
     * @param practitionerId The identifier of the Practitioner.
     * @param patientId      The identifier of the Patient.
     * @return The Encounter resource.
     */
    public Encounter getEncounterFromPractitionerAndPatient(String practitionerId, String patientId) {
        Practitioner practitioner = getPractitionerById(practitionerId);
        Patient patient = getPatientById(patientId);

        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("practitioner").hasId(practitioner.getIdPart()))
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart()))
                .returnBundle(Bundle.class)
                .execute();

        return (Encounter) bundle.getEntryFirstRep().getResource();
    }

    /**
     * Retrieves a Practitioner resource from the FHIR server using its unique resource identifier (ID).
     *
     * @param practitionerId The unique identifier (resource ID) of the Practitioner.
     * @return The Practitioner resource retrieved from the FHIR server.
     *         If no resource is found, this method will return null.
     */
    public Practitioner getPractitionerFromId(String practitionerId) {
        // Perform a search query for the Practitioner resource with the specified ID.
        Bundle bundle = client.search()
                .forResource(Practitioner.class) // Specify the Practitioner resource type.
                .where(Practitioner.RES_ID.exactly().identifier(practitionerId)) // Match the exact resource ID.
                .returnBundle(Bundle.class) // Return the result as a FHIR Bundle.
                .execute(); // Execute the search query on the FHIR server.

        // Retrieve the first entry from the search results and cast it to a Practitioner resource.
        return (Practitioner) bundle.getEntryFirstRep().getResource();
    }

    /**
     * Retrieves a Patient resource from the FHIR server using its unique resource identifier (ID).
     *
     * @param patientId The unique identifier (resource ID) of the Patient.
     * @return The Patient resource retrieved from the FHIR server.
     *         If no resource is found, this method will return null.
     */
    public Patient getPatientFromId(String patientId) {
        // Perform a search query for the Patient resource with the specified ID.
        Bundle bundle = client.search()
                .forResource(Patient.class) // Specify the Patient resource type.
                .where(Patient.RES_ID.exactly().identifier(patientId)) // Match the exact resource ID.
                .returnBundle(Bundle.class) // Return the result as a FHIR Bundle.
                .execute(); // Execute the search query on the FHIR server.

        // Retrieve the first entry from the search results and cast it to a Patient resource.
        return (Patient) bundle.getEntryFirstRep().getResource();
    }

    public Patient getPatientFromIdentifier(String patientId) {
        // Perform a search query for the Patient resource with the specified ID.
        Bundle bundle = client.search()
                .forResource(Patient.class) // Specify the Patient resource type.
                .where(Patient.RES_ID.exactly().identifier(patientId)) // Match the exact resource ID.
                .returnBundle(Bundle.class) // Return the result as a FHIR Bundle.
                .execute(); // Execute the search query on the FHIR server.

        // Retrieve the first entry from the search results and cast it to a Patient resource.
        return (Patient) bundle.getEntryFirstRep().getResource();
    }

    /**
     * Resolves the Patient ID from a given identifier.
     *
     * @param patientIdentifier The identifier of the Patient.
     * @return The Patient ID or null if not found.
     */
    public String getPatientIdByIdentifier(String patientIdentifier) {
        System.out.println("[DEBUG] Searching for Patient with identifier: " + patientIdentifier);
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientIdentifier))
                .returnBundle(Bundle.class)
                .execute();

        System.out.println("[DEBUG] Bundle returned: " + bundle);
        if (!bundle.hasEntry()){
            bundle = client.search()
                    .forResource(Patient.class)
                    .where(Patient.RES_ID.exactly().identifier(patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();

            System.out.println("[DEBUG] Bundle returned: " + bundle);
        }
        if (bundle.hasEntry() && !bundle.getEntry().isEmpty()) {
            Resource resource = bundle.getEntryFirstRep().getResource();
            System.out.println("[DEBUG] Resource retrieved: " + resource);

            if (resource instanceof Patient) {
                Patient patient = (Patient) resource;
                return patient.getIdElement().getIdPart();
            } else {
                System.err.println("[ERROR] Retrieved resource is not a Patient.");
                return null;
            }
        } else {
            System.err.println("[ERROR] No Patient found for identifier: " + patientIdentifier);
            return null;
        }
    }

    public String getPatientIdById(String patientIdentifier) {
        System.out.println("[DEBUG] Searching for Patient with identifier: " + patientIdentifier);
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientIdentifier))
                .returnBundle(Bundle.class)
                .execute();

        System.out.println("[DEBUG] Bundle returned: " + bundle);
        if (!bundle.hasEntry()){
            bundle = client.search()
                    .forResource(Patient.class)
                    .where(Patient.RES_ID.exactly().identifier(patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();

            System.out.println("[DEBUG] Bundle returned: " + bundle);
        }
        if (bundle.hasEntry() && !bundle.getEntry().isEmpty()) {
            Resource resource = bundle.getEntryFirstRep().getResource();
            System.out.println("[DEBUG] Resource retrieved: " + resource);

            if (resource instanceof Patient) {
                Patient patient = (Patient) resource;
                return patient.getIdPart();
            } else {
                System.err.println("[ERROR] Retrieved resource is not a Patient.");
                return null;
            }
        } else {
            System.err.println("[ERROR] No Patient found for identifier: " + patientIdentifier);
            return null;
        }
    }


    /**
     * Fetches a list of Observations associated with a given Patient.
     *
     * @param patientId The identifier of the Patient.
     * @return A list of Observation resources.
     */
    public List<Observation> getObservationsForPatient(String patientId) {
        Bundle bundle = client.search()
                .forResource(Observation.class)
                .where(Observation.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        return bundle.getEntry().stream()
                .map(entry -> (Observation) entry.getResource())
                .collect(Collectors.toList());
    }

    /**
     * Fetches a list of Encounters associated with a given Patient.
     *
     * @param patientId The identifier of the Patient.
     * @return A list of Encounter resources.
     */
    public List<Encounter> getEncountersForPatient(String patientId) {
        Patient patient = getPatientById(patientId);

        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart()))
                .count(20)
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(entry -> (Encounter) entry.getResource())
                .filter(encounter -> encounter.getSubject() != null)
                .collect(Collectors.toList());

    }

    public List<Encounter> getEncountersForPatientIdentifier(String patientId) {
        Patient patient = getPatientFromIdentifier(patientId);

        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart()))
                .count(100)
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(entry -> (Encounter) entry.getResource())
                .filter(encounter -> encounter.getSubject() != null)
                .collect(Collectors.toList());

    }


    public List<CarePlan> getCarePlansForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse CarePlan
        Bundle bundle = client.search()
                .forResource(CarePlan.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] CarePlan fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No CarePlan resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse CarePlan
        return bundle.getEntry().stream()
                .map(entry -> (CarePlan) entry.getResource()) // Converte le entry in risorse CarePlan
                .filter(carePlan -> {
                    // Controlla se il periodo è valido
                    if (carePlan.hasPeriod() && carePlan.getPeriod().hasStart()) {
                        return true;
                    } else {
                        System.err.println("[ERROR] CarePlan missing or invalid period: " + carePlan.getIdElement().getIdPart());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }



    /**
     * Fetches a list of Encounters associated with a given Practitioner.
     *
     * @param practitionerId The identifier of the Practitioner.
     * @return A list of Encounter resources.
     */
    public List<Encounter> getEncountersForPractitioner(String practitionerId) {
        Practitioner practitioner = getPractitionerById(practitionerId);

        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("practitioner").hasId(practitioner.getIdPart()))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(entry -> (Encounter) entry.getResource())
                .collect(Collectors.toList());

    }

    /**
     * Fetches an Encounter resource using its unique identifier.
     *
     * @param encounterId The identifier of the Encounter.
     * @return The Encounter resource.
     */
    public Encounter getEncounterById(String encounterId) {
        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(Encounter.IDENTIFIER.exactly().identifier(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return (Encounter) bundle.getEntryFirstRep().getResource();
    }

    /**
     * Fetches a list of Observations associated with a given Patient and Encounter.
     *
     * @param patientId   The identifier of the Patient.
     * @param encounterId The identifier of the Encounter.
     * @return A list of Observation resources.
     */
    public List<Observation> getObservationsForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(Observation.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .and(new ReferenceClientParam("encounter").hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return bundle.getEntry().stream()
                .map(entry -> (Observation) entry.getResource())
                .collect(Collectors.toList());
    }

    /**
     * Fetches a list of Conditions associated with a given Patient and Encounter.
     *
     * @param patientId   The identifier of the Patient.
     * @param encounterId The identifier of the Encounter.
     * @return A list of Condition resources.
     */
    public List<Condition> getConditionsForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(Condition.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .and(new ReferenceClientParam("encounter").hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return bundle.getEntry().stream()
                .map(entry -> (Condition) entry.getResource())
                .collect(Collectors.toList());
    }

    public List<Condition> getConditionsForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse Condition
        Bundle bundle = client.search()
                .forResource(Condition.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] Condition fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No Condition resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse Condition
        return bundle.getEntry().stream()
                .map(entry -> (Condition) entry.getResource()) // Converte le entry in risorse Condition
                .filter(condition -> {
                    // Controlla se `onsetDateTime` è valida
                    if (condition.hasOnsetDateTimeType() && condition.getOnsetDateTimeType().hasValue()) {
                        return true; // Include solo risorse con onset date valida
                    } else {
                        System.err.println("[ERROR] Condition missing or invalid onset date: " + condition.getIdElement().getIdPart());
                        return false; // Esclude risorse con onset date mancante
                    }
                })
                .collect(Collectors.toList());
    }


    /**
     * Fetches a list of Conditions associated with a given Patient and preceding the given date
     *
     * @param patientId The identifier of the Patient.
     * @param date The date to compare
     *
     * @return A list of Condition resources.
     */
    public List<Condition> getPreviousConditionsForPatient(String patientId, DateTimeType date) {
        Bundle bundle = client.search()
                .forResource(Condition.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(entry -> (Condition) entry.getResource())
                .filter(condition -> {
                    DateTimeType onset = condition.getOnsetDateTimeType();
                    return onset != null && onset.before(date);
                })
                .toList();
    }

    public List<Procedure> getPreviousProceduresForPatient(String patientId, DateTimeType date) {
        Bundle bundle = client.search()
                .forResource(Procedure.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        Set<String> uniqueIds = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (Procedure) entry.getResource())
                .filter(procedure -> {
                    DateTimeType onset = procedure.getOccurrenceDateTimeType();
                    return onset != null && onset.before(date);
                })
                .filter(procedure -> uniqueIds.add(procedure.getOccurrenceDateTimeType().toHumanDisplay()))
                .toList();

    }

    public List<MedicationRequest> getPreviousMedicationRequestsForPatient(String patientId, DateTimeType date) {
        Bundle encounterBundle = client.search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .and(new DateClientParam("date").before().day(date.getValue()))
                .returnBundle(Bundle.class)
                .execute();

        Set<String> encounterIds = encounterBundle.getEntry().stream()
                .map(entry -> entry.getResource().getIdElement().getIdPart())
                .collect(Collectors.toSet());

        Bundle medicationRequestBundle = client.search()
                .forResource(MedicationRequest.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        Set<String> uniqueIds = new HashSet<>();
        return medicationRequestBundle.getEntry().stream()
                .map(entry -> (MedicationRequest) entry.getResource())
                .filter(medicationRequest -> {
                    boolean isUnique = uniqueIds.add(
                            medicationRequest.getMedication().getConcept()
                                    .getCodingFirstRep().getCode());

                    boolean isFromPreviousEncounter =
                            encounterIds.contains(
                            medicationRequest.getEncounter().getReference().split("/")[1]);

                    return isUnique && isFromPreviousEncounter;
                })
                .toList();
    }

    public List<Procedure> getProceduresForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(Procedure.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .and(new ReferenceClientParam("encounter").hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        Set<String> uniqueIds = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (Procedure) entry.getResource())
                .filter(procedure -> uniqueIds.add(procedure.getOccurrenceDateTimeType().toHumanDisplay()))
                .toList();
    }

    public List<Immunization> getImmunizationsForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(Immunization.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        Set<String> uniqueDates = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (Immunization) entry.getResource())
                .filter(immunization -> {
                    boolean idMatch = encounterId.matches(immunization.getEncounter()
                            .getReference().split("/")[1]);
                    boolean dateDistinct = uniqueDates.add(
                            immunization.getOccurrenceDateTimeType().toHumanDisplay()
                    );

                        return idMatch && dateDistinct;
                })
                .toList();
    }

    public List<CarePlan> getCarePlansForPatientAndEncounter(String patientId, String encounterId) {
        // Recupera il paziente per validare l'ID
        Patient patient = getPatientById(patientId);

        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of();
        }

        // Esegui la query per le risorse CarePlan
        Bundle bundle = client.search()
                .forResource(CarePlan.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart()))
                .and(new ReferenceClientParam("encounter").hasId(encounterId))
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        System.out.println("[DEBUG] CarePlan Bundle: " + new FhirContext().newJsonParser().encodeResourceToString(bundle));

        // Restituisci l'elenco di risorse CarePlan
        return bundle.getEntry().stream()
                .map(entry -> (CarePlan) entry.getResource())
                .filter(carePlan -> carePlan.getSubject() != null)
                .collect(Collectors.toList());
    }


    public List<AllergyIntolerance> getAllergiesForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(AllergyIntolerance.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        Set<String> uniqueAllergies = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (AllergyIntolerance) entry.getResource())
                .filter(allergyIntolerance -> {
                            boolean thisEncounter = allergyIntolerance
                                    .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/encounter-reference")
                                    .getValue().toString().split("/")[1].replace("]","").equals(encounterId);

                            boolean unique = uniqueAllergies.add(allergyIntolerance.getCode()
                                    .getCodingFirstRep().getDisplay());
                            return thisEncounter && unique;
                        }
                )
                .toList();
    }

    public List<ImagingStudy> getImagingStudiesForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(ImagingStudy.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .and(new ReferenceClientParam("encounter").hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return bundle.getEntry().stream()
                .map(entry -> (ImagingStudy) entry.getResource())
                .toList();
    }

    public List<MedicationRequest> getMedicationRequestForPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(MedicationRequest.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .and(new ReferenceClientParam("encounter").hasId(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        Set<String> uniqueMedicationRequests = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (MedicationRequest) entry.getResource())
                .filter(medicationRequest ->
                        uniqueMedicationRequests.add(medicationRequest
                                        .getMedication().getConcept().getCodingFirstRep()
                                        .getDisplay()))
                .toList();
    }


    public List<Device> getDeviceByPatientAndEncounter(String patientId, String encounterId) {
        Bundle bundle = client.search()
                .forResource(Device.class)
                .returnBundle(Bundle.class)
                .execute();

        Set<String> uniqueDevices = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (Device) entry.getResource())
                .filter(device -> {
                    boolean thisPatient = device
                            .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient")
                            .getValue().toString().split("/")[1].replace("]","").equals(patientId);

                    boolean thisEncounter = device
                            .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter")
                            .getValue().toString().split("/")[1].replace("]","").equals(encounterId);

                    boolean unique = uniqueDevices.add(device.getUdiCarrierFirstRep()
                                    .getCarrierHRF());
                            return thisEncounter && thisPatient && unique;
                        }
                )
                .toList();
    }
    public List<AllergyIntolerance> getAllergyIntolerancesForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse AllergyIntolerance
        Bundle bundle = client.search()
                .forResource(AllergyIntolerance.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] AllergyIntolerance fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No AllergyIntolerance resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse AllergyIntolerance
        return bundle.getEntry().stream()
                .map(entry -> (AllergyIntolerance) entry.getResource()) // Converte le entry in risorse AllergyIntolerance
                .collect(Collectors.toList());
    }


    public List<AllergyIntolerance> getAllergiesForPatient(String patientId) {
        Bundle bundle = client.search()
                .forResource(AllergyIntolerance.class)
                .where(new ReferenceClientParam("patient").hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
        Set<String> uniqueAllergies = new HashSet<>();
        return bundle.getEntry().stream()
                .map(entry -> (AllergyIntolerance) entry.getResource())
                .filter(allergyIntolerance -> uniqueAllergies.add(allergyIntolerance.getCode()
                        .getCodingFirstRep().getDisplay()))
                .toList();
    }


    /**
     * Fetches a list of Procedures for a given Patient.
     *
     * @param patientId The ID of the Patient.
     * @return A list of Procedure resources.
     */
    public List<Procedure> getProceduresForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse Procedure
        Bundle bundle = client.search()
                .forResource(Procedure.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] Procedure fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No Procedure resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse Procedure
        return bundle.getEntry().stream()
                .map(entry -> (Procedure) entry.getResource()) // Converte le entry in risorse Procedure
                .collect(Collectors.toList());
    }


    public List<Immunization> getImmunizationsForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse Immunization
        Bundle bundle = client.search()
                .forResource(Immunization.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] Immunization fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No Immunization resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse Immunization
        return bundle.getEntry().stream()
                .map(entry -> (Immunization) entry.getResource()) // Converte le entry in risorse Immunization
                .filter(immunization -> immunization.hasOccurrenceDateTimeType()) // Filtro per risorse con data valida
                .collect(Collectors.toList());
    }

    public List<MedicationRequest> getMedicationRequestsForPatient(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            throw new IllegalArgumentException("Patient ID must not be null or empty");
        }

        // Recupera il paziente per verificare che l'ID sia valido
        Patient patient = getPatientFromIdentifier(patientId);
        if (patient == null) {
            System.err.println("[ERROR] Patient not found for ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se il paziente non esiste
        }

        // Esegui la query al server FHIR per le risorse MedicationRequest
        Bundle bundle = client.search()
                .forResource(MedicationRequest.class)
                .where(new ReferenceClientParam("patient").hasId(patient.getIdPart())) // Filtra per ID paziente
                .count(100) // Limita il numero di risultati
                .returnBundle(Bundle.class)
                .execute();

        // Log per il debug
        System.out.println("[DEBUG] MedicationRequest fetch bundle: " + bundle);

        if (bundle == null || !bundle.hasEntry()) {
            System.out.println("[DEBUG] No MedicationRequest resources found for patient ID: " + patientId);
            return List.of(); // Restituisce una lista vuota se non ci sono entry nel bundle
        }

        // Estrai e restituisci le risorse MedicationRequest
        return bundle.getEntry().stream()
                .map(entry -> (MedicationRequest) entry.getResource()) // Converte le entry in risorse MedicationRequest
                .filter(medicationRequest -> medicationRequest.hasStatus()) // Filtro per risorse con stato valido
                .collect(Collectors.toList());
    }

    public Bundle transaction (Bundle bundle) {
        return client.transaction().withBundle(bundle).execute();
    }

    public MethodOutcome updateResource(Object object){
        if (object instanceof Encounter encounter) {
            return client.update().resource(encounter).execute();
        }
        return null;
    }

}

