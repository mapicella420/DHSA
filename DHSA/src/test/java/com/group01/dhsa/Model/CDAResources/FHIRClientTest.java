package com.group01.dhsa.Model.CDAResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Patient;
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
        Patient patient = fhirClient.getPatientById("8b0484cd-3dbd-8b8d-1b72-a32f74a5a846");

        Bundle bundle = testClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly().identifier("8b0484cd-3dbd-8b8d-1b72-a32f74a5a846"))
                .returnBundle(Bundle.class)
                .execute();

        Patient test = (Patient) bundle.getEntryFirstRep().getResource();

    }

    @Test
    void getEncountersForPatient() {
    }

    @Test
    void getObservationsForPatient() {
    }
}
