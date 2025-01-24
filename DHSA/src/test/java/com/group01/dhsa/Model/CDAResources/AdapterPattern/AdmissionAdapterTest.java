package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Condition;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdmissionAdapterTest {
    private AdmissionAdapter admissionAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    String patientId;

    @BeforeEach
    void setUp() {
        admissionAdapter = new AdmissionAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "5e371258-6ec2-3615-619a-b9bb6d4a4d9a";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846";
    }

    @Test
    void toCdaObject() {
        Component component = admissionAdapter.toCdaObject(encounter);


        assertNotNull(component, "The Component object should not be null");


        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");


        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");
        assertEquals("46241-6", section.getCode().getCode(), "The Section code does not match");
        assertEquals("Motivo del ricovero", section.getTitle().getTitle(), "The Section title does not match");


        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text paragraphs should not be empty");


        Paragraph ParagraphContent = (Paragraph) text.getValues().get(0);
        String firstParagraphContent = ParagraphContent.getContent();

        assertTrue(firstParagraphContent.contains("The patient was admitted on"),
                "The first paragraph should contain the admission date");

        // Validate Conditions
        List<Condition> conditions = fhirClient.getConditionsForPatientAndEncounter(
                encounter.getSubject().getReference().split("/")[1],
                encounter.getIdElement().getIdPart()
        );
        if (conditions != null && !conditions.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(p -> p.toString().contains("The patient has been diagnosed with")),
                    "The Text should include condition information");
        }




        List<Entry> entries = section.getEntry();
        assertNotNull(entries, "The Section entries should not be null");
        assertFalse(entries.isEmpty(), "The Section entries should not be empty");


        System.out.println("Component CDA successfully generated: " + component);
    }

}
