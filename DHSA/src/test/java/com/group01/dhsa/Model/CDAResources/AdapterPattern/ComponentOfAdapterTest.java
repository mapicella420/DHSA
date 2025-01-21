package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.ComponentOf;
import org.hl7.fhir.r5.model.Encounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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


    }
}