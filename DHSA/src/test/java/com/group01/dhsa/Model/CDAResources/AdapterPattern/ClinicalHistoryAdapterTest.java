package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Condition;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.MedicationRequest;
import org.hl7.fhir.r5.model.Procedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalHistoryAdapterTest {

    private ClinicalHistoryAdapter clinicalHistoryAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        clinicalHistoryAdapter = new ClinicalHistoryAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "5e371258-6ec2-3615-619a-b9bb6d4a4d9a";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846";
    }

    @Test
    void toCdaObject() {
        Component component = clinicalHistoryAdapter.toCdaObject(encounter);

        // Validazione del componente principale
        assertNotNull(component, "The Component object should not be null");

        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");

        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");
        assertEquals("47039-3", section.getCode().getCode(), "The Section code does not match");
        assertEquals("Inquadramento Clinico Iniziale", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text paragraphs should not be empty");

        Paragraph firstParagraph = (Paragraph) text.getValues().get(0);
        assertTrue(firstParagraph.getContent().contains("The patient was admitted during a"),
                "The first paragraph should mention the patient's admission");

        // Validazione delle condizioni del paziente
        List<Condition> conditionList = fhirClient.getPreviousConditionsForPatient(
                patientId,
                encounter.getActualPeriod().getStartElement()
        );
        if (conditionList != null && !conditionList.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(p -> p.toString().contains("The patient suffers from")),
                    "The Text should include condition information");
        }

        // Validazione delle procedure
        List<Procedure> procedureList = fhirClient.getPreviousProceduresForPatient(
                patientId,
                encounter.getActualPeriod().getStartElement()
        );
        if (procedureList != null && !procedureList.isEmpty()) {
            List<ComponentInner> componentInnerList = section.getComponent();
            assertNotNull(componentInnerList, "The ComponentInner list should not be null");
            assertFalse(componentInnerList.isEmpty(), "The ComponentInner list should not be empty");

            ComponentInner procedureComponent = componentInnerList.get(0);
            Section procedureSection = procedureComponent.getSection();
            assertNotNull(procedureSection, "The Section for procedures should not be null");

            Text procedureText = procedureSection.getText();
            assertNotNull(procedureText, "The Text for procedures should not be null");
            assertTrue(procedureText.getValues().stream()
                            .anyMatch(p -> p.toString().contains("The patient underwent the following procedures")),
                    "The procedures section should include the list of procedures");
        }

        // Validazione della terapia farmacologica
        List<MedicationRequest> medicationRequestList = fhirClient.getPreviousMedicationRequestsForPatient(
                patientId,
                encounter.getActualPeriod().getStartElement()
        );
        if (medicationRequestList != null && !medicationRequestList.isEmpty()) {
            List<ComponentInner> componentInnerList = section.getComponent();
            assertNotNull(componentInnerList, "The ComponentInner list should not be null");
            assertFalse(componentInnerList.isEmpty(), "The ComponentInner list should not be empty");

            ComponentInner medicationComponent = componentInnerList.get(1);
            Section medicationSection = medicationComponent.getSection();
            assertNotNull(medicationSection, "The Section for medication requests should not be null");

            Text medicationText = medicationSection.getText();
            assertNotNull(medicationText, "The Text for medication requests should not be null");
            assertTrue(medicationText.getValues().stream()
                            .anyMatch(p -> p instanceof Paragraph),
                    "The medication section should include paragraphs describing medications");
        }

        System.out.println("Clinical History CDA successfully generated: " + component);
    }
}
