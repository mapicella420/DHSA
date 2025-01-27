package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.Body.HospitalDischargeStudiesAdapter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Device;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.ImagingStudy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HospitalDischargeStudiesAdapterTest {
    private HospitalDischargeStudiesAdapter hospitalDischargeStudiesAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        hospitalDischargeStudiesAdapter = new HospitalDischargeStudiesAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "11fa206c-ccd5-cff4-8faa-52eaa7915c16";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "0f8fa0de-b75f-4e09-d8be-e8c8946be4c0";
    }

    @Test
    void toCdaObject() {

        Component component = hospitalDischargeStudiesAdapter.toCdaObject(encounter);


        assertNotNull(component, "The Component object should not be null");


        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");


        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");


        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");

        assertNotNull(section.getCode(), "The Section code should not be null");
        assertEquals("11493-4", section.getCode().getCode(), "The Section code does not match");

        assertNotNull(section.getTitle(), "The Section title should not be null");
        assertEquals("Riscontri ed accertamenti significativi", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text content should not be empty");

        validateImagingStudies(text);

        validateDevices(text);

        System.out.println("Hospital Discharge Studies CDA successfully generated:\n" + component);
    }

    /**
     * Metodo per validare che il testo contenga informazioni sugli Imaging Studies.
     */
    private void validateImagingStudies(Text text) {
        List<ImagingStudy> imagingStudies = fhirClient.getImagingStudiesForPatientAndEncounter(patientId, encounter.getIdPart());

        if (imagingStudies != null && !imagingStudies.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(value -> value.toString().contains("Summary of key investigations during Hospitalization")),
                    "The Text should include imaging studies summary");

            imagingStudies.forEach(imagingStudy -> assertTrue(
                    text.getValues().toString().contains(imagingStudy.getIdPart()),
                    "The Text should contain details for ImagingStudy ID: " + imagingStudy.getIdPart()
            ));
        }
    }

    /**
     * Metodo per validare che il testo contenga informazioni sui dispositivi.
     */
    private void validateDevices(Text text) {
        List<Device> devices = fhirClient.getDeviceByPatientAndEncounter(patientId, encounter.getIdPart());

        if (devices != null && !devices.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(value -> value.toString().contains("The findings led to the need for the following devices")),
                    "The Text should include device information");

            devices.forEach(device -> assertTrue(
                    text.getValues().toString().contains(device.getIdPart()),
                    "The Text should contain details for Device ID: " + device.getIdPart()
            ));
        }
    }
}
