package com.group01.dhsa.Model.CDAResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FHIRClientTest {

    FHIRClient fhirClient;
    IGenericClient testClient;

    @BeforeEach
    void setUp() {
        fhirClient = new FHIRClient();

        String FHIR_SERVER_URL = "http://localhost:8080/fhir";
        FhirContext fhirContext = FhirContext.forR5();
        testClient = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);
    }

    @Test
    void getPatientById() {

        String patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846";

        Patient patient = fhirClient.getPatientById(patientId);

        Bundle bundle = testClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();

        //Patient test = (Patient) bundle.getEntryFirstRep().getResource();


        // Estrazione del paziente dal bundle per il confronto
        Patient expectedPatient = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Patient) {
                expectedPatient = (Patient) entry.getResource();
                break;
            }
        }

        // Verifica che il paziente restituito sia corretto
        assertEquals(patient.getId(), expectedPatient.getId());
    }

    @Test
    void getEncountersForPatient() {
    }

    @Test
    void getObservationsForPatient() {
    }
}
