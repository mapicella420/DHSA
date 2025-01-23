package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.*;

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
                conditionsBuilder.append(". ");
            }
            conditionParagraph.setContent(conditionsBuilder.toString());
            paragraphs.add(conditionParagraph);
        }

        List<Observation> observations = FHIRClient.getInstance().getObservationsForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (observations != null && !observations.isEmpty()) {
            List<StructuredList> structuredLists = new ArrayList<>();
            StructuredList structuredList = new StructuredList();
            structuredList.setType("unordered");
            structuredLists.add(structuredList);
            text.setLists(structuredLists);

            Paragraph observationParagraph = new Paragraph(
                    "The following observations were recorded during the encounter:");

            paragraphs.add(observationParagraph);

            List<ListItem> items = new ArrayList<>();
            structuredList.setItems(items);
            for (Observation observation : observations) {
                String obsDetail = observation.getCode().getText();
                String obsValue = formatObservationValue(observation);
                StringBuilder listItemContent = new StringBuilder(obsDetail);
                if (obsValue != null) {
                    listItemContent.append(" (").append(obsValue).append(")");
                }
                items.add(new ListItem(listItemContent.toString()));
            }

        }


        return component;
    }

    private String formatObservationValue(Observation obs) {
        if (obs.hasValueQuantity()) {
            return obs.getValueQuantity().getValue().toPlainString() + " " +
                    obs.getValueQuantity().getUnit();
        } else if (obs.hasValueCodeableConcept()) {
            return obs.getValueCodeableConcept().getText();
        } else if (obs.hasValueStringType()) {
            return obs.getValueStringType().getValue();
        } else if (obs.hasValueBooleanType()) {
            return Boolean.toString(obs.getValueBooleanType().getValue());
        } else if (obs.hasValueIntegerType()) {
            return Integer.toString(obs.getValueIntegerType().getValue());
        } else if (obs.hasValueRange()) {
            Range range = obs.getValueRange();
            return range.getLow().getValue().toPlainString() + " " + range.getLow().getUnit() +
                    " - " +
                    range.getHigh().getValue().toPlainString() + " " + range.getHigh().getUnit();
        } else if (obs.hasValueRatio()) {
            Ratio ratio = obs.getValueRatio();
            return ratio.getNumerator().getValue().toPlainString() + " " + ratio.getNumerator().getUnit() +
                    " / " +
                    ratio.getDenominator().getValue().toPlainString() + " " + ratio.getDenominator().getUnit();
        } else if (obs.hasValueSampledData()) {
            return "Sampled Data: " + obs.getValueSampledData().getData();
        } else if (obs.hasValueTimeType()) {
            return obs.getValueTimeType().getValue();
        } else if (obs.hasValueDateTimeType()) {
            return obs.getValueDateTimeType().getValue().toString();
        } else if (obs.hasValuePeriod()) {
            Period period = obs.getValuePeriod();
            return "From " + period.getStartElement().toHumanDisplay() +
                    " to " + period.getEndElement().toHumanDisplay();
        } else if (obs.hasValueAttachment()) {
            return "Attachment: " + obs.getValueAttachment().getTitle();
        } else if (obs.hasValueReference()) {
            return "Reference: " + obs.getValueReference().getDisplay();
        } else {
            return null;
        }
    }
}

