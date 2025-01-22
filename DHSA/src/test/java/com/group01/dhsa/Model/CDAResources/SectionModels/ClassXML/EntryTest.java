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

public class EntryTest {

    @Test
    public void testingEntry() {
        try {
            Entry entry = new Entry();

            EntryRelationship entryRelationship = new EntryRelationship();
            Act act = new Act();

            entry.setEntryRelationship(entryRelationship);
            entry.setAct(act);

            List<ObservationCDA> observationList = new ArrayList<>();

            ObservationCDA observation1 = new ObservationCDA("classCode1", "moodCode1");
            observation1.setCode(new Code("code1", "codeSystem1", "codeSystemName1", "displayName1"));
            Value value = new Value("code1", "codeSystem1", "codeSystemName1", "displayName1");
            Translation  translation = new Translation("code1", "codeSystem1", "codeSystemName1", "displayName1");
            value.setTranslation(translation);
            observation1.setValue(value);


            observationList.add(observation1);

            ObservationCDA observation2 = new ObservationCDA("classCode2", "moodCode2");
            observation2.setCode(new Code("code2", "codeSystem2", "codeSystemName2", "displayName2"));
            observation2.setValue(new Value("code2", "codeSystem2", "codeSystemName2", "displayName2"));
            observationList.add(observation2);

            entry.setObservation(observationList);

            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(Entry.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(entry, writer);

            String xmlOutput = writer.toString();
            System.out.println("Serialized XML:\n" + xmlOutput);

            StringReader reader = new StringReader(xmlOutput);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Entry deserializedEntry = (Entry) unmarshaller.unmarshal(reader);

            if (deserializedEntry != null) {
                System.out.println("\nDeserialization successful. Entry object is valid.");
            } else {
                System.out.println("\nDeserialization failed.");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
