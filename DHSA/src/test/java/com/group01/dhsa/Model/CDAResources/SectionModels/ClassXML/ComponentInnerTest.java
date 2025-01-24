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

public class ComponentInnerTest {

    @Test
    public void testingComponentInner() {
        try {
            Section section = new Section();
            section.setClassCode("classCode1");
            section.setMoodCode("moodCode1");

            Code code = new Code("code1", "codeSystem1", "codeSystemName1", "displayName1");
            section.setCode(code);

            Title title = new Title("Title1");
            section.setTitle(title);

            List<Entry> entryList = new ArrayList<>();

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

            entryList.add(entry1);
            entryList.add(entry2);
            section.setEntry(entryList);

            ComponentInner componentInner = new ComponentInner();
            componentInner.setTypeCode("typeCode1");
            componentInner.setSection(section);

            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(ComponentInner.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(componentInner, writer);

            String xmlOutput = writer.toString();
            System.out.println("Serialized XML:\n" + xmlOutput);

            StringReader reader = new StringReader(xmlOutput);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ComponentInner deserializedComponentInner = (ComponentInner) unmarshaller.unmarshal(reader);

            if (deserializedComponentInner != null) {
                System.out.println("\nDeserialization successful. ComponentInner object is valid.");
            } else {
                System.out.println("\nDeserialization failed.");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
