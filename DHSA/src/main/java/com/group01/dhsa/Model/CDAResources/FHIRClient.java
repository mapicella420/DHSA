package com.group01.dhsa.Model.CDAResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import java.util.List;
import java.util.stream.Collectors;

public class FHIRClient {
    private IGenericClient client;

    public FHIRClient() {
        String FHIR_SERVER_URL = "http://localhost:8080/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        this.client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);
    }

    public Patient getPatientById(String patientId) {
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();
        return (Patient) bundle.getEntryFirstRep().getResource();
    }

    public Bundle getEncountersForPatient(String patientId) {
        return client.search()
                .forResource(Encounter.class)
                .where(Encounter.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
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
}

