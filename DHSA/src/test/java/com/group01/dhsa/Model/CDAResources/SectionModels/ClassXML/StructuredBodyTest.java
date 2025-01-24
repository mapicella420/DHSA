package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class StructuredBodyTest {

    @Test
    public void testingStructuredBody() {
        try {
            // Creazione della sezione
            Section section = new Section();
            section.setClassCode("classCode1");
            section.setMoodCode("moodCode1");

            // Creazione del codice per la sezione
            Code code = new Code("code1", "codeSystem1", "codeSystemName1", "displayName1");
            section.setCode(code);

            // Creazione del titolo per la sezione
            Title title = new Title("Title1");
            section.setTitle(title);

            // Creazione della lista di Entry
            List<Entry> entryList = new ArrayList<>();

            // Creazione di un'osservazione
            ObservationCDA observation1 = new ObservationCDA("classCode1", "moodCode1");
            observation1.setCode(new Code("code1", "codeSystem1", "codeSystemName1", "displayName1"));
            Value value1 = new Value("code1", "codeSystem1", "codeSystemName1", "displayName1");
            Translation translation1 = new Translation("code1", "codeSystem1", "codeSystemName1", "displayName1");
            value1.setTranslation(translation1);
            observation1.setValue(value1);
            List<ObservationCDA> observationList = new ArrayList<>();
            observationList.add(observation1);

            // Aggiunta dell'osservazione all'Entry
            Entry entry1 = new Entry(observation1);
            entryList.add(entry1);
            section.setEntry(entryList);

            // Creazione di un ComponentInner e assegnazione alla StructuredBody
            ComponentInner componentInner = new ComponentInner();
            componentInner.setTypeCode("typeCode1");
            componentInner.setSection(section);

            StructuredBody structuredBody = new StructuredBody();
            structuredBody.setComponentInner(componentInner);
            structuredBody.setClassCode("classCode2");
            structuredBody.setMoodCode("moodCode2");

            // Serializzazione dell'oggetto StructuredBody in XML
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(StructuredBody.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(structuredBody, writer);

            // Stampa dell'output XML serializzato
            String xmlOutput = writer.toString();
            System.out.println("Serialized XML:\n" + xmlOutput);

            // Deserializzazione del XML in un oggetto StructuredBody
            StringReader reader = new StringReader(xmlOutput);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StructuredBody deserializedStructuredBody = (StructuredBody) unmarshaller.unmarshal(reader);

            // Verifica che la deserializzazione sia avvenuta con successo
            if (deserializedStructuredBody != null) {
                System.out.println("\nDeserialization successful. StructuredBody object is valid.");
            } else {
                System.out.println("\nDeserialization failed.");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
