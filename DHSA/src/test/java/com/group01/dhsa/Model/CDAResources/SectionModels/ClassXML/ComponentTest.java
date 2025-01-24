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

public class ComponentTest {

    @Test
    public void testingComponent() {
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

            StructuredBody structuredBody = new StructuredBody();
            structuredBody.setComponentInner(componentInner);
            structuredBody.setClassCode("classCode2");
            structuredBody.setMoodCode("moodCode2");

            Component component = new Component();
            component.setStructuredBody(structuredBody);

            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(Component.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(component, writer);

            String xmlOutput = writer.toString();
            System.out.println("Serialized XML:\n" + xmlOutput);

            StringReader reader = new StringReader(xmlOutput);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Component deserializedComponent = (Component) unmarshaller.unmarshal(reader);

            if (deserializedComponent != null) {
                System.out.println("\nDeserialization successful. Component object is valid.");
            } else {
                System.out.println("\nDeserialization failed.");
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
