package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.Body.HospitalCourseAdapter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HospitalCourseAdapterTest {

    private HospitalCourseAdapter hospitalCourseAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        hospitalCourseAdapter = new HospitalCourseAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "5e371258-6ec2-3615-619a-b9bb6d4a4d9a";  // Example Encounter ID
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846";  // Example Patient ID
    }

    @Test
    void toCdaObject() {
        Component component = hospitalCourseAdapter.toCdaObject(encounter);

        assertNotNull(component, "The Component object should not be null");

        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");

        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");
        assertEquals("8648-8", section.getCode().getCode(), "The Section code does not match");
        assertEquals("Decorso Ospedaliero", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text paragraphs should not be empty");

        Paragraph firstParagraph = (Paragraph) text.getValues().get(0);
        assertTrue(firstParagraph.getContent().contains("The patient " ),
                "The first paragraph should mention the patient's information");

        List<Condition> conditions = fhirClient.getConditionsForPatientAndEncounter(patientId, encounter.getIdPart());
        if (conditions != null && !conditions.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(p -> p.toString().contains("The condition was identified as")),
                    "The Text should include condition information");
        }

        List<Procedure> procedures = fhirClient.getProceduresForPatientAndEncounter(patientId, encounter.getIdPart());
        if (procedures != null && !procedures.isEmpty()) {
            List<ComponentInner> components = section.getComponent();
            assertNotNull(components, "The ComponentInner list should not be null");
            assertFalse(components.isEmpty(), "The ComponentInner list should not be empty");

            ComponentInner procedureComponent = components.get(0);
            Section procedureSection = procedureComponent.getSection();
            assertNotNull(procedureSection, "The Section for procedures should not be null");

            Text procedureText = procedureSection.getText();
            assertNotNull(procedureText, "The Text for procedures should not be null");
            assertTrue(procedureText.getValues().stream()
                            .anyMatch(p -> p.toString().contains("The patient underwent the following procedures")),
                    "The procedures section should include the list of procedures");
        }

        List<MedicationRequest> medicationRequests = fhirClient.getMedicationRequestForPatientAndEncounter(patientId, encounter.getIdPart());
        if (medicationRequests != null && !medicationRequests.isEmpty()) {
            List<ComponentInner> components = section.getComponent();
            assertNotNull(components, "The ComponentInner list should not be null");
            assertFalse(components.isEmpty(), "The ComponentInner list should not be empty");

            ComponentInner medicationComponent = components.get(1);
            Section medicationSection = medicationComponent.getSection();
            assertNotNull(medicationSection, "The Section for medication requests should not be null");

            Text medicationText = medicationSection.getText();
            assertNotNull(medicationText, "The Text for medication requests should not be null");
            assertTrue(medicationText.getValues().stream()
                            .anyMatch(p -> p instanceof Paragraph),
                    "The medication section should include paragraphs describing medications");
        }



        System.out.println("Hospital Course CDA successfully generated: " + component);
    }
}
