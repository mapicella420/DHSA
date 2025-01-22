package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.List;

public class AdmissionAdapter implements CdaSection<Component, Encounter>{

    /**
     * @param fhirObject
     * @return
     */
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
                "46241-6",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Diagnosi di Accettazione"
        ));

        section.setTitle(new Title("Motivo del ricovero"));

        String description = fhirObject.getTypeFirstRep().getCodingFirstRep().getDisplay();
        String reason = fhirObject.getReasonFirstRep().getUseFirstRep().getText();
        String admissionDate = (fhirObject.getActualPeriod() != null && fhirObject.getActualPeriod().hasStart())
                ? fhirObject.getActualPeriod().getStartElement().toHumanDisplay()
                : "unknown date";

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();
        List<Observation> observations = FHIRClient.getInstance().getObservationsForPatientAndEncounter(
                patientId,
                encounterId
        );
        List<Condition> conditions = FHIRClient.getInstance().getConditionsForPatientAndEncounter(
                patientId,
                encounterId
        );

        Text text = new Text();
        List<Paragraph> paragraphs = new ArrayList<>();
        List<StructuredList> lists = new ArrayList<>();


        Paragraph introParagraph = new Paragraph();
        StringBuilder introContent = new StringBuilder();
        introContent.append("The patient was admitted on ").append(admissionDate);
        if (description != null) {
            introContent.append(" for a ").append(description);
        }
        if (reason != null) {
            introContent.append(", specifically due to ").append(reason).append(".");
        } else {
            introContent.append(".");
        }
        introParagraph.setContent(introContent.toString());
        paragraphs.add(introParagraph);


        if (conditions != null && !conditions.isEmpty()) {
            Paragraph conditionIntro = new Paragraph();
            conditionIntro.setContent("The following relevant conditions were noted:");
            paragraphs.add(conditionIntro);

            StringBuilder conditionContent = new StringBuilder();

            for (Condition condition : conditions) {
                conditionContent.append("The patient has been diagnosed with ")
                        .append(condition.getCode().getCoding().getFirst().getDisplay());

                if (condition.getClinicalStatus() != null) {
                    String clinicalStatus = condition.getClinicalStatus().getCoding().getFirst().getCode();
                    conditionContent.append(" and is currently ").append(clinicalStatus.toLowerCase());
                }

                if (condition.getOnsetDateTimeType() != null) {
                    String onsetDate = condition.getOnsetDateTimeType().toHumanDisplay();
                    conditionContent.append(", which started on ").append(onsetDate).append(". ");
                } else {
                    conditionContent.append(". ");
                }
            }

            Paragraph conditionParagraph = new Paragraph();
            conditionParagraph.setContent(conditionContent.toString());
            paragraphs.add(conditionParagraph);
        }

        if (observations != null && !observations.isEmpty()) {
            Paragraph observationIntro = new Paragraph();
            observationIntro.setContent("Relevant clinical observations include:");
            paragraphs.add(observationIntro);

            StructuredList observationList = new StructuredList();
            observationList.setType("unordered");
            List<ListItem> listItems = new ArrayList<>();

            for (Observation obs : observations) {
                String obsDetail = obs.getCode().getText();
                String obsValue = formatObservationValue(obs);
                StringBuilder listItemContent = new StringBuilder(obsDetail);
                if (obsValue != null) {
                    listItemContent.append(" (").append(obsValue).append(")");
                }
                listItems.add(new ListItem(listItemContent.toString()));
            }

            observationList.setItems(listItems);
            lists.add(observationList);
        }

        text.setParagraphs(paragraphs);
        text.setLists(lists);

        section.setText(text);

        if (observations != null && !observations.isEmpty()) {
            List<Entry> entry = new ArrayList<>();
            Entry entry1 = new Entry();
            section.setEntry(entry);
            entry.add(entry1);
            List<ObservationCDA> observationCDAList = new ArrayList<>();
            for (Observation obs : observations) {
                ObservationCDA observationCDA = new ObservationCDA("OBS", "EVN");

                Code code = new Code("8646-2", "2.16.840.1.113883.6.1", "LOINC",
                        "Diagnosi di Accettazione Ospedaliera");
                observationCDA.setCode(code);
                Value value = new Value();
                observationCDA.setValue(value);

                Translation translation = new Translation(obs.getCode().getCodingFirstRep().getCode(),
                        "2.16.840.1.113883.6.1",
                        "LOINC",
                        obs.getCode().getText()
                );
                value.setTranslation(translation);

                observationCDAList.add(observationCDA);
            }
            entry1.setObservation(observationCDAList);
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
