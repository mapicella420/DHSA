package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValueTest {

    @Test
    public void testValueSerializationWithTranslation() throws JAXBException {
        // Create a Value object with Translation
        Value value = new Value("12345", "2.16.840.1.113883.6.1", "LOINC", "Test Display Name");

        Translation translation = new Translation();
        translation.setCode("54321");
        translation.setCodeSystem("2.16.840.1.113883.6.96");
        translation.setCodeSystemName("SNOMED CT");
        translation.setDisplayName("Translation Display Name");

        value.setTranslation(translation);

        // Serialize the Value object to XML
        JAXBContext context = JAXBContext.newInstance(Value.class, Translation.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter writer = new StringWriter();
        marshaller.marshal(value, writer);

        String xmlOutput = writer.toString();

        // Print the XML for manual inspection
        System.out.println(xmlOutput);

        // Assertions
        assertTrue(xmlOutput.contains("<translation"));
        assertTrue(xmlOutput.contains("Translation Display Name"));
    }

    @Test
    public void testValueSerializationWithoutTranslation() throws JAXBException {
        // Create a Value object without Translation
        Value value = new Value("12345", "2.16.840.1.113883.6.1", "LOINC", "Test Display Name");

        // Serialize the Value object to XML
        JAXBContext context = JAXBContext.newInstance(Value.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter writer = new StringWriter();
        marshaller.marshal(value, writer);

        String xmlOutput = writer.toString();

        // Print the XML for manual inspection
        System.out.println(xmlOutput);

        // Assertions
        assertTrue(!xmlOutput.contains("<translation"));
    }
}
