package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.Model.CDAResources.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
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

        // Estrai i dati dal paziente
        String nome = patient.getNameFirstRep().getGivenAsSingleString();
        String cognome = patient.getNameFirstRep().getFamily();
        String sesso = patient.getGender().toString();

        // Calcola il codice fiscale
        int giorno = patient.getBirthDateElement().getDay();
        String mese = patient.getBirthDateElement().getMonth().toString();
        int anno = patient.getBirthDateElement().getYear();
        CodiceFiscaleCalculator cf = new CodiceFiscaleCalculator(nome, cognome, giorno, mese, anno, sesso, "STATI UNITI", true);

        // Verifica che l'XML contenga i dati corretti
        assertTrue(xmlOutput.contains("<id root=\"2.16.840.1.113883.2.9.4.3.2\" extension=\"" + cf.calcolaCodiceFiscale() +
                "\" assigningAuthorityName=\"Ministero Economia e Finanze\"/>"));
        assertTrue(xmlOutput.contains("<given>" + nome + "</given>"));
        assertTrue(xmlOutput.contains("<family>" + cognome + "</family>"));

        // Verifica che l'indirizzo sia presente
        assertTrue(xmlOutput.contains("<addr use=\"HP\">"));
        assertTrue(xmlOutput.contains("<country>536</country>"));  // Codice ISTAT US
        assertTrue(xmlOutput.contains("<state>" + patient.getAddressFirstRep().getState() + "</state>"));
        assertTrue(xmlOutput.contains("<city>" + patient.getAddressFirstRep().getCity() + "</city>"));
        assertTrue(xmlOutput.contains("<streetAddressLine>" + patient.getAddressFirstRep().getLine().getFirst() + "</streetAddressLine>"));

        // Verifica che la data di nascita sia presente
        assertTrue(xmlOutput.contains("<birthTime value=\"" + patient.getBirthDateElement().asStringValue() + "\"/>"));

        // Verifica che il codice del genere sia corretto
        String genderDisplay = "Sconosciuto";
        if ("male".equalsIgnoreCase(sesso)) {
            genderDisplay = "Maschio";
        } else if ("female".equalsIgnoreCase(sesso)) {
            genderDisplay = "Femmina";
        } else if ("other".equalsIgnoreCase(sesso)) {
            genderDisplay = "Altro";
        }

        assertTrue(xmlOutput.contains("<administrativeGenderCode code=\"" +
                patient.getGender().toString().toLowerCase() +
                "\" codeSystem=\"2.16.840.1.113883.4.642.4.2\" codeSystemName=\"HL7 AdministrativeGender\" displayName=\""
                + genderDisplay + "\""));

    }
}
