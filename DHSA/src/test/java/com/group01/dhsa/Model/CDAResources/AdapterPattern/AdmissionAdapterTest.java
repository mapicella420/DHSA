//package com.group01.dhsa.Model.CDAResources.AdapterPattern;
//
//import com.group01.dhsa.FHIRClient;
//import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
//import org.hl7.fhir.r5.model.Condition;
//import org.hl7.fhir.r5.model.Encounter;
//import org.hl7.fhir.r5.model.Observation;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class AdmissionAdapterTest {
//    private AdmissionAdapter admissionAdapter;
//    private FHIRClient fhirClient;
//    private Encounter encounter;
//
//    @BeforeEach
//    void setUp() {
//        admissionAdapter = new AdmissionAdapter();
//        fhirClient = FHIRClient.getInstance();
//
//        // Replace with an actual Encounter ID for testing purposes
//        String encounterId = "d14de31f-ad35-d0e4-b76e-4d9a548539c3";
//        encounter = fhirClient.getEncounterById(encounterId);
//    }
//
//    @Test
//    void toCdaObject() {
//        // Call the method to test
//        Component component = admissionAdapter.toCdaObject(encounter);
//
//        // Validate the Component object
//        assertNotNull(component, "The Component object should not be null");
//
//        // Validate the StructuredBody and its inner components
//        StructuredBody structuredBody = component.getStructuredBody();
//        assertNotNull(structuredBody, "The StructuredBody should not be null");
//        ComponentInner componentInner = structuredBody.getComponentInner();
//        assertNotNull(componentInner, "The ComponentInner should not be null");
//
//        // Validate the Section and its attributes
//        Section section = componentInner.getSection();
//        assertNotNull(section, "The Section should not be null");
//        assertEquals("46241-6", section.getCode().getCode(), "The Section code does not match");
//        assertEquals("Motivo del ricovero", section.getTitle().getTitle(), "The Section title does not match");
//
//        // Validate the Text object
//        Text text = section.getText();
//        assertNotNull(text, "The Text should not be null");
//        assertFalse(text.getParagraphs().isEmpty(), "The Text paragraphs should not be empty");
//
//        // Verify that the text contains admission details
//        String firstParagraphContent = text.getParagraphs().get(0).getContent();
//        assertTrue(firstParagraphContent.contains("The patient was admitted on"),
//                "The first paragraph should contain the admission date");
//
//        // Validate Conditions
//        List<Condition> conditions = fhirClient.getConditionsForPatientAndEncounter(
//                encounter.getSubject().getReference().split("/")[1],
//                encounter.getIdElement().getIdPart()
//        );
//        if (conditions != null && !conditions.isEmpty()) {
//            assertTrue(text.getParagraphs().stream()
//                            .anyMatch(p -> p.getContent().contains("The patient has been diagnosed with")),
//                    "The Text should include condition information");
//        }
//
//        // Validate Observations
//        List<Observation> observations = fhirClient.getObservationsForPatientAndEncounter(
//                encounter.getSubject().getReference().split("/")[1],
//                encounter.getIdElement().getIdPart()
//        );
//        if (observations != null && !observations.isEmpty()) {
//            assertTrue(text.getLists().stream()
//                            .flatMap(list -> list.getItems().stream())
//                            .anyMatch(item -> item.getContent().contains("Blood Pressure")),
//                    "The Text should include observation details");
//        }
//
//        // Validate Entry object
//        List<Entry> entries = section.getEntry();
//        assertNotNull(entries, "The Section entries should not be null");
//        assertFalse(entries.isEmpty(), "The Section entries should not be empty");
//
//        // Print a success message
//        System.out.println("Component CDA successfully generated: " + component);
//    }
//}