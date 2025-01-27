package com.group01.dhsa.Model.CDAResources.AdapterPattern.Body;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.CdaSection;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClinicalHistoryAdapter implements CdaSection<Component, Encounter> {
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
                "47039-3",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Ricovero Ospedaliero, anamnesi ed esame obiettivo"
        ));

        section.setTitle(new Title("Inquadramento Clinico Iniziale"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        List<Condition> conditionList = FHIRClient.getInstance().getPreviousConditionsForPatient(
                patientId,
                fhirObject.getActualPeriod().getStartElement()
        );
        String description = fhirObject.getTypeFirstRep().getCodingFirstRep().getDisplay();
        String descriptionClass = fhirObject.getClass_FirstRep().getCodingFirstRep().getCode();
        String reason = fhirObject.getReasonFirstRep().getUseFirstRep().getText();

        Paragraph introParagraph = new Paragraph();
        StringBuilder introContent = new StringBuilder();
        introContent.append("The patient was admitted during a ")
                .append(descriptionClass).append(" encounter,");
        if (description != null) {
            introContent.append(" due to a ").append(description);
        }
        if (reason != null) {
            introContent.append(", specifically due to ").append(reason).append(".");
        } else {
            introContent.append(".");
        }
        introParagraph.setContent(introContent.toString());
        textList.add(introParagraph);
        text.setValues(textList);

        if (conditionList != null && !conditionList.isEmpty()) {
            StructuredList structuredList = new StructuredList();
            Paragraph conditionIntro = new Paragraph();
            conditionIntro.setContent("The patient suffers from: ");
            textList.add(conditionIntro);

            List<ListItem> listItems = new ArrayList<>();
            structuredList.setItems(listItems);

            for (Condition condition : conditionList) {
                StringBuilder conditionContent = new StringBuilder();
                conditionContent.append(condition.getCode().getCoding().getFirst().getDisplay());

                if (condition.getClinicalStatus() != null) {
                    String clinicalStatus = condition.getClinicalStatus().getCoding().getFirst().getCode();
                    conditionContent.append(" and is currently ").append(clinicalStatus.toLowerCase());
                }

                if (condition.getOnsetDateTimeType() != null) {
                    String onsetDate = condition.getOnsetDateTimeType().toHumanDisplay();
                    conditionContent.append(", which started on ").append(onsetDate).append("; ");
                }
                listItems.add(new ListItem(conditionContent.toString()));

            }

            textList.add(structuredList);
        }

        List<Procedure> procedureList = FHIRClient.getInstance().getPreviousProceduresForPatient(
                patientId,
                fhirObject.getActualPeriod().getStartElement()
        );

        //Section is optional
        List<ComponentInner> componentInnerList = new ArrayList<>();
        if (procedureList != null && !procedureList.isEmpty()) {
            ComponentInner componentAnamnesi = new ComponentInner();
            Section sectionAnamnesi = new Section();

            componentInnerList.add(componentAnamnesi);
            componentAnamnesi.setSection(sectionAnamnesi);

            Text textAnamnesi = new Text();
            sectionAnamnesi.setText(textAnamnesi);
            List<Object> textAnamnesiList = new ArrayList<>();

            sectionAnamnesi.setCode(new Code(
                    "11329-0",
                    "2.16.840.1.113883.6.1",
                    "LOINC",
                    "Anamnesi  Generale"
            ));
            sectionAnamnesi.setTitle(new Title("Anamnesi"));

            Paragraph introParagraphAnamnesi = new Paragraph();

            introParagraphAnamnesi.setContent("The patient underwent the following procedures:");
            textAnamnesiList.add(introParagraphAnamnesi);

            StructuredList procedureStructuredList = new StructuredList();
            List<ListItem> listItemsProcedure = new ArrayList<>();

            textAnamnesiList.add(procedureStructuredList);

            //Entry (optional)
            List<Entry> entryListAnamnesi = new ArrayList<>();
            sectionAnamnesi.setEntry(entryListAnamnesi);

            for (Procedure procedure : procedureList) {
                String procedureDisplay = procedure.getCode().getCodingFirstRep().getDisplay();
                StringBuilder listItemContent = new StringBuilder(procedureDisplay);
                if (procedure.getOccurrenceDateTimeType() != null) {
                    String onsetDate = procedure.getOccurrenceDateTimeType().toHumanDisplay();
                    listItemContent.append(", performed at ").append(onsetDate).append("; ");
                } else {
                    listItemContent.append(". ");
                }
                listItemsProcedure.add(new ListItem(listItemContent.toString()));

                ObservationCDA observationCDA = new ObservationCDA("OBS", "EVN");

                Code code = new Code("75326-9", "2.16.840.1.113883.6.1", "LOINC",
                        "Problem");
                observationCDA.setCode(code);
                StatusCode statusCodeAnamnesi = new StatusCode("completed");
                observationCDA.setStatusCode(statusCodeAnamnesi);
                entryListAnamnesi.add(new Entry(observationCDA));

                Low lowAnamnesi = new Low();
                lowAnamnesi.setNullFlavor("UNK");
                ZonedDateTime zonedDateTime = procedure.getOccurrenceDateTimeType().getValue()
                        .toInstant()
                        .atZone(ZoneId.of("UTC"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");
                String formattedDate = zonedDateTime.format(formatter);
                EffectiveTime effectiveTime = new EffectiveTime(
                        lowAnamnesi,
                        new High(formattedDate)
                );
                observationCDA.setEffectiveTime(effectiveTime);

                Value valueAnamnesi = new Value(
                        procedure.getCode().getCodingFirstRep().getCode(),
                        "2.16.840.1.113883.4.642.3.427",
                        "SNOMED CT",
                        "Procedure Codes"
                );
                valueAnamnesi.setCode("CD");
                observationCDA.setValue(valueAnamnesi);

            }

            procedureStructuredList.setItems(listItemsProcedure);
            textAnamnesi.setValues(textAnamnesiList);

        }else{
            componentInnerList = null;
        }

        if (componentInnerList == null) {
            componentInnerList = new ArrayList<>();
        }

        List<MedicationRequest> medicationRequestList = FHIRClient.getInstance()
                .getPreviousMedicationRequestsForPatient(patientId, fhirObject
                        .getActualPeriod().getStartElement());

        //Section is optional
        if (medicationRequestList != null && !medicationRequestList.isEmpty()) {
            ComponentInner componentMedicationRequest = new ComponentInner();
            componentInnerList.add(componentMedicationRequest);
            Section sectionMedicationRequest = new Section();
            componentMedicationRequest.setSection(sectionMedicationRequest);

            Text textMedicationRequest = new Text();
            sectionMedicationRequest.setText(textMedicationRequest);

            List<Object> listMedicationRequest = new ArrayList<>();

            sectionMedicationRequest.setCode(new Code(
                    "42346-7",
                    "2.16.840.1.113883.6.1",
                    "LOINC",
                    "Terapia Farmacologica all’ingresso"
            ));
            sectionMedicationRequest.setTitle(new Title("Terapia Farmacologica all’Ingresso"));

            for (MedicationRequest medicationRequest : medicationRequestList) {
                Paragraph paragraphMedicationRequest = new Paragraph();
                paragraphMedicationRequest.setContent(medicationRequest.getMedication().getConcept()
                        .getCodingFirstRep().getDisplay());
                listMedicationRequest.add(paragraphMedicationRequest);
            }
            textMedicationRequest.setValues(listMedicationRequest);
        }else {
            componentInnerList = null;
        }

        if(componentInnerList != null){
            section.setComponent(componentInnerList);
        }


        return component;
    }
}
