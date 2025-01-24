package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.AllergyIntolerance;
import org.hl7.fhir.r5.model.Encounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllergyAdapterTest {
    private AllergyAdapter allergyAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        allergyAdapter = new AllergyAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "7c5d7a54-12e8-f192-550c-e8ef61db7c0e";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "ce4ce4d8-d4e2-aca2-5a92-8ce703c5077a";
    }

    @Test
    void toCdaObject() {
        Component component = allergyAdapter.toCdaObject(encounter);

        assertNotNull(component, "The Component object should not be null");

        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");

        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");

        assertNotNull(section.getCode(), "The Section code should not be null");
        assertEquals("48765-2", section.getCode().getCode(), "The Section code does not match");

        assertNotNull(section.getTitle(), "The Section title should not be null");
        assertEquals("Allergie e/o Reazioni Avverse", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text content should not be empty");

        validateAllergies(text);

        System.out.println("Allergy CDA successfully generated:\n" + component);
    }

    /**
     * Metodo per validare che il testo contenga informazioni sulle allergie.
     */
    private void validateAllergies(Text text) {
        List<AllergyIntolerance> allergies = fhirClient.getAllergiesForPatient(patientId);

        if (allergies != null && !allergies.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(value -> value.toString().contains("Allergie e/o Reazioni Avverse")),
                    "The Text should include allergy details");

            allergies.forEach(allergy -> {
                String allergyDisplay = allergy.getCode().getCodingFirstRep() != null
                        ? allergy.getCode().getCodingFirstRep().getDisplay()
                        : "Unknown allergy";

                assertTrue(
                        text.getValues().toString().contains(allergyDisplay),
                        "The Text should contain details for allergy: " + allergyDisplay
                );

                String clinicalStatus = allergy.getClinicalStatus() != null &&
                        !allergy.getClinicalStatus().getCoding().isEmpty()
                        ? allergy.getClinicalStatus().getCodingFirstRep().getCode()
                        : "Unknown clinical status";

                assertTrue(
                        text.getValues().toString().contains(clinicalStatus),
                        "The Text should contain the clinical status: " + clinicalStatus
                );
            });
        }
    }
}
