package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Practitioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class AuthorAdapterTest {

    private AuthorAdapter authorAdapter;
    private Practitioner practitioner;

    @BeforeEach
    void setUp() {
        // Inizializza l'adapter
        authorAdapter = new AuthorAdapter();

        FHIRClient fhirClient = FHIRClient.getInstance();
        String practitionerId = "7a0d9463-9b7b-3c24-b14f-928d19dd5a32";

        practitioner = fhirClient.getPractitionerById(practitionerId);

    }

    @AfterEach
    void tearDown() {
        // Eventuali risorse da liberare
    }

    @Test
    void toCdaObject() throws JAXBException {
        // Converte il Practitioner in un Author CDA
        Author author = authorAdapter.toCdaObject(practitioner);

        // Verifica che l'Author non sia null
        assertNotNull(author);

        // Verifica che l'AssignedAuthor sia stato correttamente impostato
        assertNotNull(author.getAssignedAuthor());

        // Verifica che il time sia stato correttamente impostato
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String expectedTime = now.atOffset(ZoneOffset.ofHours(1)).format(formatter);
        assertEquals(expectedTime, author.getTime());


        // Verifica che l'ID sia stato impostato correttamente (codice fiscale)
        String nome = practitioner.getNameFirstRep().getGivenAsSingleString();
        String cognome = practitioner.getNameFirstRep().getFamily();

        // Serializzazione in XML
        StringWriter stringWriter = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(Author.class);
        Marshaller marshaller = context.createMarshaller();

        // Imposta la formattazione per una visualizzazione leggibile
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Serializza l'oggetto in XML
        marshaller.marshal(author, stringWriter);

        // Ottieni l'XML risultante come stringa
        String xmlOutput = stringWriter.toString();
        System.out.println(xmlOutput);

        // Verifica che l'XML contenga i dati corretti
        assertTrue(xmlOutput.contains("<id root=\"2.16.840.1.113883.2.9.4.3.2\" extension=\"" + author.getAssignedAuthor().getId().getExtension() +
                "\" assigningAuthorityName=\"Ministero Economia e Finanze\"/>"));
        assertTrue(xmlOutput.contains("<given>" + nome + "</given>"));
        assertTrue(xmlOutput.contains("<family>" + cognome + "</family>"));
    }
}
