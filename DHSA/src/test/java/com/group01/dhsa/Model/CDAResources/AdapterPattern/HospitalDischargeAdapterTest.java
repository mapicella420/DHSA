package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.Body.HospitalDischargeAdapter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.CarePlan;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.MedicationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HospitalDischargeAdapterTest {

    private HospitalDischargeAdapter hospitalDischargeAdapter;
    private FHIRClient fhirClient;
    private Encounter encounter;
    private String patientId;

    @BeforeEach
    void setUp() {
        hospitalDischargeAdapter = new HospitalDischargeAdapter();
        fhirClient = FHIRClient.getInstance();

        String encounterId = "11fa206c-ccd5-cff4-8faa-52eaa7915c16";
        encounter = fhirClient.getEncounterById(encounterId);
        patientId = "0f8fa0de-b75f-4e09-d8be-e8c8946be4c0";
    }

    @Test
    void toCdaObject() {
        Component component = hospitalDischargeAdapter.toCdaObject(encounter);

        assertNotNull(component, "The Component object should not be null");

        StructuredBody structuredBody = component.getStructuredBody();
        assertNotNull(structuredBody, "The StructuredBody should not be null");

        ComponentInner componentInner = structuredBody.getComponentInner();
        assertNotNull(componentInner, "The ComponentInner should not be null");

        Section section = componentInner.getSection();
        assertNotNull(section, "The Section should not be null");

        assertNotNull(section.getCode(), "The Section code should not be null");
        assertEquals("11535-2", section.getCode().getCode(), "The Section code does not match");
        assertEquals("Diagnosi di Dimissione", section.getCode().getDisplayName(), "The Section code display does not match");

        assertNotNull(section.getTitle(), "The Section title should not be null");
        assertEquals("Condizioni del paziente e diagnosi alla dimissione", section.getTitle().getTitle(), "The Section title does not match");

        Text text = section.getText();
        assertNotNull(text, "The Text should not be null");
        assertFalse(text.getValues().isEmpty(), "The Text content should not be empty");

        validateCarePlans(text);
        validateMedications(text);

        System.out.println("Hospital Discharge CDA successfully generated:\n" + component);
    }

    /**
     * Metodo per validare che il testo contenga informazioni sui CarePlan.
     */
    private void validateCarePlans(Text text) {
        List<CarePlan> carePlans = fhirClient.getCarePlansForPatientAndEncounter(patientId, encounter.getIdPart());

        if (carePlans != null && !carePlans.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(value -> value.toString().contains("The patient is advised to follow the prescribed care plans")),
                    "The Text should include a summary of the care plans");

            carePlans.forEach(carePlan -> {
                String carePlanDisplay = carePlan.getCategoryFirstRep().getCodingFirstRep().getDisplay();
                assertTrue(text.getValues().toString().contains(carePlanDisplay),
                        "The Text should contain details for CarePlan: " + carePlanDisplay);

                if (carePlan.getAddressesFirstRep().getConcept() != null) {
                    String condition = carePlan.getAddressesFirstRep().getConcept().getCodingFirstRep().getDisplay();
                    assertTrue(text.getValues().toString().contains(condition),
                            "The Text should contain the condition addressed by the CarePlan: " + condition);
                }

                if (carePlan.getPeriod().hasStart()) {
                    String startDate = carePlan.getPeriod().getStartElement().toHumanDisplay();
                    assertTrue(text.getValues().toString().contains(startDate),
                            "The Text should contain the start date for CarePlan: " + carePlanDisplay);
                }
            });
        }
    }

    /**
     * Metodo per validare che il testo contenga informazioni sui MedicationRequest.
     */
    private void validateMedications(Text text) {
        List<MedicationRequest> medicationRequests = fhirClient.getMedicationRequestForPatientAndEncounter(patientId, encounter.getIdPart());

        if (medicationRequests != null && !medicationRequests.isEmpty()) {
            assertTrue(text.getValues().stream()
                            .anyMatch(value -> value.toString().contains("The indicated medications are to be taken")),
                    "The Text should include a summary of the medications");

            medicationRequests.forEach(medicationRequest -> {
                String medicationDisplay = medicationRequest.getMedication().getConcept().getCodingFirstRep().getDisplay();
                assertTrue(text.getValues().toString().contains(medicationDisplay),
                        "The Text should contain details for Medication: " + medicationDisplay);

                medicationRequest.getExtension().stream()
                        .filter(extension -> extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/base-cost"))
                        .findFirst()
                        .ifPresent(extension -> {
                            Double baseCost = extension.getValueQuantity().getValue().doubleValue();
                            assertTrue(text.getValues().toString().contains("Base cost: $" + String.format("%.2f", baseCost)),
                                    "The Text should contain the base cost for Medication: " + medicationDisplay);
                        });

                medicationRequest.getExtension().stream()
                        .filter(extension -> extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/total-cost"))
                        .findFirst()
                        .ifPresent(extension -> {
                            Double totalCost = extension.getValueQuantity().getValue().doubleValue();
                            assertTrue(text.getValues().toString().contains("Total cost: $" + String.format("%.2f", totalCost)),
                                    "The Text should contain the total cost for Medication: " + medicationDisplay);
                        });

                String status = medicationRequest.getStatus().toCode();
                assertTrue(text.getValues().toString().contains("Status: " + status),
                        "The Text should contain the status for Medication: " + medicationDisplay);
            });
        }
    }
}
