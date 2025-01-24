package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

public class EntryTest {

    @Test
    public void testingEntry() {
        try {
            ObservationCDA observation1 = new ObservationCDA("classCode1", "moodCode1");
            observation1.setCode(new Code("code1", "codeSystem1", "codeSystemName1", "displayName1"));
            Value value1 = new Value("code1", "codeSystem1", "codeSystemName1", "displayName1");
            Translation translation1 = new Translation("code1", "codeSystem1", "codeSystemName1", "displayName1");
            value1.setTranslation(translation1);
            observation1.setValue(value1);

            Entry entry1 = new Entry(observation1);

            ObservationCDA observation2 = new ObservationCDA("classCode2", "moodCode2");
            observation2.setCode(new Code("code2", "codeSystem2", "codeSystemName2", "displayName2"));
            observation2.setValue(new Value("code2", "codeSystem2", "codeSystemName2", "displayName2"));

            Entry entry2 = new Entry(observation2);

            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(Entry.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(entry1, writer);
            marshaller.marshal(entry2, writer);

            String xmlOutput = writer.toString();
            System.out.println("Serialized XML:\n" + xmlOutput);

            StringReader reader = new StringReader(xmlOutput);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            Entry deserializedEntry1 = (Entry) unmarshaller.unmarshal(reader);

            Entry deserializedEntry2 = (Entry) unmarshaller.unmarshal(reader);

            if (deserializedEntry1 != null && deserializedEntry2 != null) {
                System.out.println("\nDeserialization successful. Entry objects are valid.");
            } else {
                System.out.println("\nDeserialization failed.");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
