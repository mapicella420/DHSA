package com.group01.dhsa.Model.CDAResources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.r5.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FHIRClientTest {

    FHIRClient fhirClient;
    IGenericClient testClient;

    @BeforeEach
    void setUp() {
        fhirClient = FHIRClient.getInstance();

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

        Patient expectedPatient = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Patient) {
                expectedPatient = (Patient) entry.getResource();
                break;
            }
        }

        assert expectedPatient != null;
        assertEquals(patient.getId(), expectedPatient.getId());
    }

    @Test
    void getEncountersForPatient() {
    }

    @Test
    void getObservationsForPatient() {
    }

    @Test
    void getPractitionerById() {
        String practitionerId = "5db62284-9e52-3c8e-bde0-53d81bd39963";

        Practitioner practitioner = fhirClient.getPractitionerById(practitionerId);

        Bundle bundle = testClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(practitionerId))
                .returnBundle(Bundle.class)
                .execute();

        Practitioner expectedPractitioner = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Practitioner) {
                expectedPractitioner = (Practitioner) entry.getResource();
                break;
            }
        }

        assert expectedPractitioner != null;
        assertEquals(practitioner.getId(), expectedPractitioner.getId());
    }

    @Test
    void getOrganizationFromId() {
        String orgId = "4722";

        Organization organization = fhirClient.getOrganizationFromId(orgId);

        Bundle bundle = testClient.search()
                .forResource(Organization.class)
                .where(Organization.RES_ID.exactly().identifier(orgId))
                .returnBundle(Bundle.class)
                .execute();

        Organization expected = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Organization) {
                expected = (Organization) entry.getResource();
                break;
            }
        }

        assert expected != null;
        assertEquals(organization.getId(), expected.getId());
    }

    @Test
    void getEncounterFromPractitionerAndPatient() {
        String practitionerId = "5db62284-9e52-3c8e-bde0-53d81bd39963";
        String patientId = "b8eb8d31-1031-fb5b-e207-b9815f80744c";

        Encounter encounter = fhirClient.getEncounterFromPractitionerAndPatient(practitionerId, patientId);

        Practitioner pr = fhirClient.getPractitionerById(practitionerId);
        Patient pat = fhirClient.getPatientById(patientId);

        Bundle bundle = testClient.search()
                .forResource(Encounter.class)
                .where(new ReferenceClientParam("practitioner").hasId(pr.getIdPart()))
                .where(new ReferenceClientParam("patient").hasId(pat.getIdPart()))
                .returnBundle(Bundle.class)
                .execute();

        Encounter expected = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Encounter) {
                expected = (Encounter) entry.getResource();
                break;
            }
        }

        assert expected != null;
        assertEquals(encounter.getId(), expected.getId());
    }

    @Test
    void getPractitionerFromId() {
        String practitionerId = "16771";

        Practitioner practitioner = fhirClient.getPractitionerFromId(practitionerId);

        Bundle bundle = testClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.RES_ID.exactly().identifier(practitionerId))
                .returnBundle(Bundle.class)
                .execute();

        Practitioner expectedPractitioner = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Practitioner) {
                expectedPractitioner = (Practitioner) entry.getResource();
                break;
            }
        }

        assert expectedPractitioner != null;
        assertEquals(practitioner.getId(), expectedPractitioner.getId());
    }

    @Test
    void getPatientFromId() {
        String patientId = "2";

        Patient patient = fhirClient.getPatientFromId(patientId);

        Bundle bundle = testClient.search()
                .forResource(Patient.class)
                .where(Patient.RES_ID.exactly().identifier(patientId))
                .returnBundle(Bundle.class)
                .execute();

        Patient expectedPatient = null;
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType() == ResourceType.Patient) {
                expectedPatient = (Patient) entry.getResource();
                break;
            }
        }

        assert expectedPatient != null;
        assertEquals(patient.getId(), expectedPatient.getId());
    }
}
