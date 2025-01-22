package com.group01.dhsa;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.r5.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
}

