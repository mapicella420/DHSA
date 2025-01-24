package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Encounter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RelevantDiagnosticAdapterTest {
    private RelevantDiagnosticAdapter relevantDiagnosticAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        relevantDiagnosticAdapter = new RelevantDiagnosticAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "11fa206c-ccd5-cff4-8faa-52eaa7915c16";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "0f8fa0de-b75f-4e09-d8be-e8c8946be4c0";
    }

    @Test
    void toCdaObject() {
        Component component = relevantDiagnosticAdapter.toCdaObject(encounter);

        assertNotNull(component, "The Component object should not be null");

        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");

        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");

        assertNotNull(section.getCode(), "The Section code should not be null");
        assertEquals("30954-2", section.getCode().getCode(), "The Section code does not match");

        assertNotNull(section.getTitle(), "The Section title should not be null");
        assertEquals("Esami eseguiti durante il ricovero", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text content should not be empty");

        validateImagingStudyTable(text);

        validateEntries(section.getEntry());

        System.out.println("Relevant Diagnostic CDA successfully generated:\n" + component);
    }

    /**
     * Metodo per validare la tabella degli ImagingStudy nel contenuto testuale.
     */
    private void validateImagingStudyTable(Text text) {
        // Verifica che il testo includa una tabella
        boolean hasTable = text.getValues().stream().anyMatch(value -> value instanceof Table);
        assertTrue(hasTable, "The Text should include a Table for ImagingStudy");

        Table table = (Table) text.getValues().stream()
                .filter(value -> value instanceof Table)
                .findFirst()
                .orElse(null);
        assertNotNull(table, "The Table should not be null");

        TableHead thead = table.getThead();
        assertNotNull(thead, "The TableHead should not be null");
        assertEquals(1, thead.getRows().size(), "The TableHead should have one row");

        List<TableCell> headerCells = thead.getRows().get(0).getCells();
        assertEquals(6, headerCells.size(), "The TableHead should have six columns");

        TableBody tbody = table.getTbody();
        assertNotNull(tbody, "The TableBody should not be null");
        assertFalse(tbody.getRows().isEmpty(), "The TableBody should not be empty");

        tbody.getRows().forEach(row -> {
            assertEquals(6, row.getCells().size(), "Each row in the TableBody should have six columns");
        });
    }

    /**
     * Metodo per validare le Entry generate per gli ImagingStudy.
     */
    private void validateEntries(List<Entry> entries) {
        assertNotNull(entries, "The Entry list should not be null");
        assertFalse(entries.isEmpty(), "The Entry list should not be empty");

        entries.forEach(entry -> {
            ObservationCDA observation = entry.getObservation();
            assertNotNull(observation, "The ObservationCDA should not be null");

            Code code = observation.getCode();
            assertNotNull(code, "The Code should not be null");

            EffectiveTime effectiveTime = observation.getEffectiveTime();
            assertNotNull(effectiveTime, "The EffectiveTime should not be null");

            ValueContent value = observation.getValueContent();
            assertNotNull(value, "The ValueContent should not be null");
            assertTrue(value.getContent().contains("Exam type"), "The ValueContent should describe the exam");
        });
    }
}
