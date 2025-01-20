package com.group01.dhsa.Model.CDAResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class FHIRClient {
    private static FHIRClient instance;
    private IGenericClient client;

    private FHIRClient() {
        String FHIR_SERVER_URL = "http://localhost:8080/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        this.client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);
    }

    public static FHIRClient getInstance() {
        if (instance == null) {
            instance = new FHIRClient();
        }
        return instance;
    }

    public Patient getPatientById(String patientId) {
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();
        return (Patient) bundle.getEntryFirstRep().getResource();
    }

    public Practitioner getPractitionerById(String practitionerId) {
        Bundle bundle = client.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(practitionerId))
                .returnBundle(Bundle.class)
                .execute();
        return (Practitioner) bundle.getEntryFirstRep().getResource();
    }

    public Organization getOrganizationFromId(String organizationId) {
        Bundle bundle = client.search()
                .forResource(Organization.class)
                .where(Organization.RES_ID.exactly().identifier(organizationId))
                .returnBundle(Bundle.class)
                .execute();
        return (Organization) bundle.getEntryFirstRep().getResource();
    }

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

    public Practitioner getPractitionerFromId(String practitionerId) {
        Bundle bundle = client.search()
                .forResource(Practitioner.class)
                .where(Practitioner.RES_ID.exactly().identifier(practitionerId))
                .returnBundle(Bundle.class)
                .execute();
        return (Practitioner) bundle.getEntryFirstRep().getResource();
    }

    public Patient getPatientFromId(String patientId) {
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.RES_ID.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();
        return (Patient) bundle.getEntryFirstRep().getResource();
    }

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

    public Encounter getEncounterById(String encounterId) {
        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(Encounter.IDENTIFIER.exactly().identifier(encounterId))
                .returnBundle(Bundle.class)
                .execute();
        return (Encounter) bundle.getEntryFirstRep().getResource();
    }
}

