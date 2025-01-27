package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.Header.ComponentOfAdapter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.ComponentOf;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.EncompassingEncounter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.Id;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class ComponentOfAdapterTest {
    ComponentOfAdapter componentOfAdapter;
    Encounter encounter;

    @BeforeEach
    void setUp() {
        componentOfAdapter = new ComponentOfAdapter();
        FHIRClient fhirClient = FHIRClient.getInstance();

        String encounterId = "9331c45b-0beb-42aa-1a13-012a432f7c3c";
        encounter = fhirClient.getEncounterById(encounterId);
    }

    @Test
    void toCdaObject() {
        ComponentOf componentOf = componentOfAdapter.toCdaObject(encounter);
        assertNotNull(componentOf, "The CDA component should not be null");

        EncompassingEncounter encompassingEncounter = componentOf.getEncompassingEncounter();
        assertNotNull(encompassingEncounter, "The EncompassingEncounter should not be null");

        // Verify the ID is mapped correctly
        Id encounterId = encompassingEncounter.getId();
        Identifier fhirIdentifier = encounter.getIdentifierFirstRep();
        assertNotNull(encounterId, "The Encounter ID should not be null");
        assertEquals(fhirIdentifier.getValue(), encounterId.getExtension(), "The FHIR Encounter ID does not match the CDA Encounter ID");

        // Verify that the start date is mapped correctly
        String effectiveStartDate = encounter.getActualPeriod().getStart().toInstant()
                .atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ"));
        assertNotNull(encompassingEncounter.getEffectiveTime(), "The EffectiveTime should not be null");
        assertEquals(effectiveStartDate, encompassingEncounter.getEffectiveTime().getLow().getValue(),
                "The start date does not match");

        // Verify that the end date is mapped correctly (if present)
        if (encounter.getActualPeriod().hasEnd()) {
            String effectiveEndDate = encounter.getActualPeriod().getEnd().toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ"));
            assertTrue(encompassingEncounter.getEffectiveTime().getHigh().getValue().contains(effectiveEndDate),
                    "The end date does not match");
        }

        // Verify the responsible party is set correctly
        assertNotNull(encompassingEncounter.getResponsibleParty(), "The responsible party should not be null");
        assertNotNull(encompassingEncounter.getResponsibleParty().getAssignedEntity(),
                "The responsible party should have an assigned entity");

        System.out.println("ComponentOf CDA successfully generated: " + componentOf);
    }

}