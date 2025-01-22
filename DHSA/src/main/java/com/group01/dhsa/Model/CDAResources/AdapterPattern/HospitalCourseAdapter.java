package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Condition;
import org.hl7.fhir.r5.model.Encounter;

import java.util.ArrayList;
import java.util.List;

public class HospitalCourseAdapter implements CdaSection<Component, Encounter>{


    @Override
    public Component toCdaObject(Encounter fhirObject) {
        Component component = new Component();
        StructuredBody structuredBody = new StructuredBody();
        component.setStructuredBody(structuredBody);
        ComponentInner componentInner = new ComponentInner();
        structuredBody.setComponentInner(componentInner);

        Section section = new Section();
        componentInner.setSection(section);

        section.setCode(new Code(
                "8648-8",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Decorso ospedaliero"
        ));

        section.setTitle(new Title("Decorso Ospedaliero"));

        Text text = new Text();
        section.setText(text);
        List<Paragraph> paragraphs = new ArrayList<>();
//        List<StructuredList> structuredLists = new ArrayList<>();
//        StructuredList structuredList = new StructuredList();
//        structuredLists.add(structuredList);
//        text.setLists(structuredLists);
        text.setParagraphs(paragraphs);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();

        List<Condition> conditions = FHIRClient.getInstance().getConditionsForPatientAndEncounter(
                patientId,
                encounterId
        );

        String stringBuilder = "The patient presented to us symptomatic for " +
                fhirObject.getTypeFirstRep().getCodingFirstRep().getDisplay() +
                ", during the encounter in date " +
                fhirObject.getActualPeriod().getStartElement().toHumanDisplay() +
                ".";
        Paragraph paragraph = new Paragraph();
        paragraph.setContent(stringBuilder);
        paragraphs.add(paragraph);

        if (conditions != null && !conditions.isEmpty()) {
            Paragraph conditionParagraph = new Paragraph();
            StringBuilder conditionsBuilder = new StringBuilder();
            conditionsBuilder.append("The condition was identified as ");
            for (Condition condition : conditions) {
                conditionsBuilder.append(condition.getCode().getCoding().getFirst().getDisplay());
                conditionsBuilder.append(", diagnosed in Encounter ID:");
                conditionsBuilder.append(condition.getIdPart());
                conditionsBuilder.append("; ");
            }
            conditionParagraph.setContent(conditionsBuilder.toString());
            paragraphs.add(conditionParagraph);
        }



        return component;
    }
}
