package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Procedure;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryOfProceduresAdapter implements CdaSection<Component, Encounter> {

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
                "47519-4",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "History of Procedures Document"
        ));

        section.setTitle(new Title("Procedure eseguite durante il ricovero"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();
        text.setValues(textList);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();

        List<Procedure> procedures = FHIRClient.getInstance().getProceduresForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (procedures != null && !procedures.isEmpty()) {
            Paragraph procedureParagraph = new Paragraph(
                    "A ");

            StringBuilder procedureItemContent = new StringBuilder();
            for (Procedure procedure : procedures) {
                String procedureDisplay = procedure.getCode().getCodingFirstRep().getDisplay();
                procedureItemContent = new StringBuilder(procedureDisplay);
                if (procedure.getOccurrenceDateTimeType() != null) {
                    String onsetDate = procedure.getOccurrenceDateTimeType().toHumanDisplay();
                    procedureItemContent.append(" was performed at ").append(onsetDate).append(". ");
                }
                if (procedure.getStatus() != null) {
                    String status = procedure.getStatus().toString();
                    procedureItemContent.append("The procedure was ").append(status);
                }
                else {
                    procedureItemContent.append(". ");
                }
            }
            textList.add(new Paragraph(procedureParagraph.getContent().concat(procedureItemContent.toString())));

            //Entries are optional
            List<Entry> entryList = new ArrayList<>();

            for (Procedure procedure : procedures) {
                Entry entry = new Entry();
                ProcedureCDA procedureCDA = new ProcedureCDA();
                procedureCDA.setCode(new Code(
                        procedure.getCode().getCodingFirstRep().getCode(),
                        "2.16.840.1.113883.4.642.3.427",
                        "SNOMED CT",
                        procedure.getCode().getCodingFirstRep().getDisplay()
                        )
                );
                EffectiveTime effectiveTime = new EffectiveTime();
                DateTimeType dateTime = procedure.getOccurrenceDateTimeType();
                ZonedDateTime zonedDateTime = dateTime.getValue().toInstant().atZone(ZoneId.of("UTC"));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String formattedDate = zonedDateTime.format(formatter);

                effectiveTime.setValue(formattedDate);
                procedureCDA.setEffectiveTime(effectiveTime);

                procedureCDA.setStatusCode(new StatusCode(procedure.getStatus().toString()));

                entry.setProcedure(procedureCDA);
                entryList.add(entry);
            }
            section.setEntry(entryList);
        }

        if (textList.isEmpty()) {
            return null;
        }

        return component;
    }
}
