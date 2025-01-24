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

public class SectionTest {

    @Test
    public void testingSection() {
        try {
            Section section = new Section();
            section.setClassCode("classCode1");
            section.setMoodCode("moodCode1");

            Code code = new Code("code1", "codeSystem1", "codeSystemName1", "displayName1");
            section.setCode(code);

            Title title = new Title("Title1");
            section.setTitle(title);

            Entry entry1 = new Entry();
            EntryRelationship entryRelationship1 = new EntryRelationship();
            Act act1 = new Act();
            entry1.setEntryRelationship(entryRelationship1);
            entry1.setAct(act1);

            ObservationCDA observation1 = new ObservationCDA("classCode1", "moodCode1");
            observation1.setCode(new Code("code1", "codeSystem1", "codeSystemName1", "displayName1"));
            Value value1 = new Value("code1", "codeSystem1", "codeSystemName1", "displayName1");
            Translation translation1 = new Translation("code1", "codeSystem1", "codeSystemName1", "displayName1");
            value1.setTranslation(translation1);
            observation1.setValue(value1);
            entry1.setObservation(observation1);

            Entry entry2 = new Entry();
            EntryRelationship entryRelationship2 = new EntryRelationship();
            Act act2 = new Act();
            entry2.setEntryRelationship(entryRelationship2);
            entry2.setAct(act2);

            ObservationCDA observation2 = new ObservationCDA("classCode2", "moodCode2");
            observation2.setCode(new Code("code2", "codeSystem2", "codeSystemName2", "displayName2"));
            observation2.setValue(new Value("code2", "codeSystem2", "codeSystemName2", "displayName2"));
            entry2.setObservation(observation2);

            List<Entry> entryList = new ArrayList<>();
            entryList.add(entry1);
            entryList.add(entry2);
            section.setEntry(entryList);

            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(Section.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(section, writer);

            String xmlOutput = writer.toString();
            System.out.println("Serialized XML:\n" + xmlOutput);

            StringReader reader = new StringReader(xmlOutput);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Section deserializedSection = (Section) unmarshaller.unmarshal(reader);

            if (deserializedSection != null) {
                System.out.println("\nDeserialization successful. Section object is valid.");
            } else {
                System.out.println("\nDeserialization failed.");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
