package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.Model.CDAResources.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.hl7.fhir.r5.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class PatientAdapterTest {

    private PatientAdapter patientAdapter;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // Inizializza l'adapter
        patientAdapter = new PatientAdapter();

        // Crea un client FHIR e recupera un paziente
        FHIRClient fhirClient = new FHIRClient();
        String patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846"; // esempio di ID del paziente

        // Recupera il paziente tramite il FHIRClient
        patient = fhirClient.getPatientById(patientId);
    }

    @AfterEach
    void tearDown() {
        // Eventuali risorse da liberare
    }

    @Test
    void toCdaObject() throws JAXBException {
        // Converte il paziente FHIR in un RecordTarget CDA
        RecordTarget recordTarget = patientAdapter.toCdaObject(patient);

        // Verifica che il RecordTarget non sia null
        assertNotNull(recordTarget);

        // Verifica che il PatientRole sia stato impostato correttamente
        assertNotNull(recordTarget.getPatientRole());

        // Serializzazione in XML
        StringWriter stringWriter = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(RecordTarget.class);
        Marshaller marshaller = context.createMarshaller();

        // Imposta la formattazione per una visualizzazione leggibile
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Serializza l'oggetto in XML
        marshaller.marshal(recordTarget, stringWriter);

        // Ottieni l'XML risultante come stringa
        String xmlOutput = stringWriter.toString();
        System.out.println(xmlOutput);

        // Verifica che l'XML contenga i dati corretti
        assertTrue(xmlOutput.contains("<id extension=\"2.16.840.1.113883.2.9.4.3.2\"/>")); // Assicurati che l'ID sia presente
        assertTrue(xmlOutput.contains("<given>" + patient.getNameFirstRep().getGivenAsSingleString() + "</given>"));
        assertTrue(xmlOutput.contains("<family>" + patient.getNameFirstRep().getFamily() + "</family>"));
        assertTrue(xmlOutput.contains("<administrativeGenderCode code=\"" + patient.getGender().toCode() + "\"/>"));

        // Puoi anche aggiungere altre asserzioni per verificare il contenuto completo dell'XML
    }
}
