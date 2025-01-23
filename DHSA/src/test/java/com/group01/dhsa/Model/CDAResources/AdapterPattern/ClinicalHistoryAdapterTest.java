package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalHistoryAdapterTest {

    ClinicalHistoryAdapter clinicalHistoryAdapter;
    FHIRClient fhirClient;
    Encounter encounter;
    String patientId;

    @BeforeEach
    void setUp() {
        clinicalHistoryAdapter = new ClinicalHistoryAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "5e371258-6ec2-3615-619a-b9bb6d4a4d9a";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846"; // Nuovo Patient ID

    }

    @Test
    void toCdaObject() {
        Component component = clinicalHistoryAdapter.toCdaObject(encounter);
        assertNotNull(component, "The Component object should not be null");


        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");


        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");
        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");


        assertEquals("Inquadramento Clinico Iniziale", section.getTitle().getTitle(),
                "The Section title does not match");
        assertEquals("47039-3", section.getCode().getCode(),
                "The Section code does not match");


        List<Paragraph> paragraphs = section.getText().getParagraphs();
        assertNotNull(paragraphs, "The Paragraphs list should not be null");
        assertFalse(paragraphs.isEmpty(), "The Paragraphs list should not be empty");

        String introContent = paragraphs.get(0).getContent();
        assertTrue(introContent.contains("The patient was admitted during a"),
                "The intro content should mention the patient's admission");


        List<StructuredList> structuredLists = section.getText().getLists();
        if (structuredLists != null && !structuredLists.isEmpty()) {
            StructuredList structuredList = structuredLists.get(0);
            assertEquals("unordered", structuredList.getType(), "The list type should be unordered");

            List<ListItem> listItems = structuredList.getItems();
            assertNotNull(listItems, "The ListItems should not be null");
            assertFalse(listItems.isEmpty(), "The ListItems should not be empty");

            String conditionContent = listItems.get(0).getContent();
            assertTrue(conditionContent.contains("and is currently"),
                    "The condition content should include the clinical status");
        }


        List<ComponentInner> componentInnerList = section.getComponent();
        if (componentInnerList != null && !componentInnerList.isEmpty()) {
            ComponentInner componentAnamnesi = componentInnerList.get(0);
            Section sectionAnamnesi = componentAnamnesi.getSection();
            assertNotNull(sectionAnamnesi, "The Section for procedures should not be null");

            Text textAnamnesi = sectionAnamnesi.getText();
            assertNotNull(textAnamnesi, "The Text for procedures should not be null");
            List<Paragraph> paragraphsAnamnesi = textAnamnesi.getParagraphs();
            assertNotNull(paragraphsAnamnesi, "The Paragraphs for procedures should not be null");
            assertFalse(paragraphsAnamnesi.isEmpty(), "The Paragraphs for procedures should not be empty");

            String procedureIntroContent = paragraphsAnamnesi.get(0).getContent();
            assertTrue(procedureIntroContent.contains("The patient underwent the following procedures"),
                    "The paragraph should introduce the procedures");


            StructuredList procedureStructuredList = textAnamnesi.getLists().get(0);
            assertNotNull(procedureStructuredList, "The StructuredList for procedures should not be null");
            assertEquals("unordered", procedureStructuredList.getType(), "The list type for procedures should be unordered");

            List<ListItem> procedureListItems = procedureStructuredList.getItems();
            assertNotNull(procedureListItems, "The ListItems for procedures should not be null");
            assertFalse(procedureListItems.isEmpty(), "The ListItems for procedures should not be empty");

            String procedureContent = procedureListItems.get(0).getContent();
            assertTrue(procedureContent.contains("performed at"), "The procedure content should include the date of the procedure");
        }


        componentInnerList = section.getComponent();
        if (componentInnerList != null && !componentInnerList.isEmpty()) {
            ComponentInner componentMedicationRequest = componentInnerList.get(1);
            Section sectionMedicationRequest = componentMedicationRequest.getSection();
            assertNotNull(sectionMedicationRequest, "The Section for medication request should not be null");

            Text textMedicationRequest = sectionMedicationRequest.getText();
            assertNotNull(textMedicationRequest, "The Text for medication request should not be null");
            List<Paragraph> paragraphsMedicationRequest = textMedicationRequest.getParagraphs();
            assertNotNull(paragraphsMedicationRequest, "The Paragraphs for medication request should not be null");
            assertFalse(paragraphsMedicationRequest.isEmpty(), "The Paragraphs for medication request should not be empty");


        }

        System.out.println("Clinical History CDA successfully generated: " + component);
    }
}