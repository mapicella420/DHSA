//package com.group01.dhsa.Model.CDAResources.AdapterPattern;
//
//import com.group01.dhsa.FHIRClient;
//import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
//import org.hl7.fhir.r5.model.Device;
//import org.hl7.fhir.r5.model.Encounter;
//import org.hl7.fhir.r5.model.ImagingStudy;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class HospitalDischargeStudiesAdapterTest {
//    private HospitalDischargeStudiesAdapter hospitalDischargeStudiesAdapter;
//    private FHIRClient fhirClient;
//    private Encounter encounter;
//    private String patientId;
//
//    @BeforeEach
//    void setUp() {
//        hospitalDischargeStudiesAdapter = new HospitalDischargeStudiesAdapter();
//        fhirClient = FHIRClient.getInstance();
//
//        String encounterId = "155aa73b-46da-5808-c218-80a5ed671009"; // Esempio Encounter ID
//        encounter = fhirClient.getEncounterById(encounterId);
//        patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846"; // Esempio Patient ID
//    }
//
//    @Test
//    void toCdaObject() {
//        // Chiamata al metodo da testare
//        Component component = hospitalDischargeStudiesAdapter.toCdaObject(encounter);
//
//        // Validazione dell'oggetto Component
//        assertNotNull(component, "The Component object should not be null");
//
//        // Validazione del corpo strutturato nel componente
//        StructuredBody structuredBody = component.getStructuredBody();
//        assertNotNull(structuredBody, "The StructuredBody should not be null");
//
//        // Validazione del componente interno del corpo strutturato
//        ComponentInner componentInner = structuredBody.getComponentInner();
//        assertNotNull(componentInner, "The ComponentInner should not be null");
//
//        // Validazione della sezione del componente interno
//        Section section = componentInner.getSection();
//        assertNotNull(section, "The Section should not be null");
//        assertEquals("11493-4", section.getCode().getCode(), "The Section code does not match");
//        assertEquals("Riscontri ed accertamenti significativi", section.getTitle().getTitle(), "The Section title does not match");
//
//        // Validazione del testo nella sezione
//        Text text = section.getText();
//        assertNotNull(text, "The Text should not be null");
//        assertFalse(text.getValues().isEmpty(), "The Text content should not be empty");
//
//        // Validazione degli Imaging Studies
//        List<ImagingStudy> imagingStudies = fhirClient.getImagingStudiesForPatientAndEncounter(patientId, encounter.getIdPart());
//        if (imagingStudies != null && !imagingStudies.isEmpty()) {
//            assertTrue(text.getValues().stream()
//                            .anyMatch(p -> p.toString().contains("Summary of key investigations during Hospitalization")),
//                    "The Text should include imaging studies summary");
//
//            imagingStudies.forEach(imagingStudy -> assertTrue(
//                    text.getValues().toString().contains(imagingStudy.getIdPart()),
//                    "The Text should contain details for each ImagingStudy"
//            ));
//        }
//
//        // Validazione dei dispositivi
//        List<Device> devices = fhirClient.getDeviceByPatientAndEncounter(patientId, encounter.getIdPart());
//        if (devices != null && !devices.isEmpty()) {
//            assertTrue(text.getValues().stream()
//                            .anyMatch(p -> p.toString().contains("The findings led to the need for the following devices")),
//                    "The Text should include device information");
//
//            devices.forEach(device -> assertTrue(
//                    text.getValues().toString().contains(device.getIdPart()),
//                    "The Text should contain details for each Device"
//            ));
//        }
//
//        // Stampa per verifica visiva (opzionale)
//        System.out.println("Hospital Discharge Studies CDA successfully generated: " + component);
//    }
//}
