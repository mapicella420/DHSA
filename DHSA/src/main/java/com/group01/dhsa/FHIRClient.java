package com.group01.dhsa;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.r5.model.*;

import java.util.List;
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

    /**
     * Private constructor to initialize the FHIR client and set up the server URL.
     * The FhirContext is used to configure the client for FHIR R5 resources.
     */
    private FHIRClient() {
        String FHIR_SERVER_URL = "http://localhost:8080/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        this.client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);
    }

    /**
     * Provides access to the Singleton instance of FHIRClient.
     *
     * @return The Singleton instance of FHIRClient.
     */
    public static FHIRClient getInstance() {
        if (instance == null) {
            instance = new FHIRClient();
        }
        return instance;
    }

    /**
     * Fetches a Patient resource from the FHIR server using the given patient identifier.
     *
     * @param patientId The identifier of the Patient.
     * @return The Patient resource.
     */
    public Patient getPatientById(String patientId) {
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();
        return (Patient) bundle.getEntryFirstRep().getResource();
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
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(entry -> (Encounter) entry.getResource())
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
}

