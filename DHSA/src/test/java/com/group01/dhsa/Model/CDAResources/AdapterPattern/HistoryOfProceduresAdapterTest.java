package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.Body.HistoryOfProceduresAdapter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Procedure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryOfProceduresAdapterTest {

    private HistoryOfProceduresAdapter historyOfProceduresAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        historyOfProceduresAdapter = new HistoryOfProceduresAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "11fa206c-ccd5-cff4-8faa-52eaa7915c16";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "0f8fa0de-b75f-4e09-d8be-e8c8946be4c0";
    }

    @Test
    void toCdaObject() {
        Component component = historyOfProceduresAdapter.toCdaObject(encounter);

        assertNotNull(component, "The Component object should not be null");

        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");

        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");

        assertNotNull(section.getCode(), "The Section code should not be null");
        assertEquals("47519-4", section.getCode().getCode(), "The Section code does not match");
        assertEquals("History of Procedures Document", section.getCode().getDisplayName(), "The Section code display does not match");

        assertNotNull(section.getTitle(), "The Section title should not be null");
        assertEquals("Procedure eseguite durante il ricovero", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text content should not be empty");

        validateProcedures(text);

        System.out.println("History of Procedures CDA successfully generated:\n" + component);
    }

    /**
     * Metodo per validare che il testo contenga informazioni sulle procedure.
     */
    private void validateProcedures(Text text) {
        List<Procedure> procedures = fhirClient.getProceduresForPatientAndEncounter(patientId, encounter.getIdPart());

        if (procedures != null && !procedures.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(value -> value.toString().contains("A")),
                    "The Text should include a summary of the procedures");

            procedures.forEach(procedure -> {
                String procedureDisplay = procedure.getCode().getCodingFirstRep().getDisplay();
                assertTrue(text.getValues().toString().contains(procedureDisplay),
                        "The Text should contain details for Procedure: " + procedureDisplay);

                if (procedure.getOccurrenceDateTimeType() != null) {
                    String onsetDate = procedure.getOccurrenceDateTimeType().toHumanDisplay();
                    assertTrue(text.getValues().toString().contains(onsetDate),
                            "The Text should contain the occurrence date for Procedure: " + procedureDisplay);
                }

                if (procedure.getStatus() != null) {
                    String status = procedure.getStatus().toString();
                    assertTrue(text.getValues().toString().contains(status),
                            "The Text should contain the status for Procedure: " + procedureDisplay);
                }
            });
        }
    }
}
