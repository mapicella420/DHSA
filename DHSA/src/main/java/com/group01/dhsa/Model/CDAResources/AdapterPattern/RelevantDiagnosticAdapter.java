package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.ImagingStudy;
import org.hl7.fhir.r5.model.Observation;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RelevantDiagnosticAdapter implements CdaSection<Component, Encounter> {

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
                "30954-2",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Esami diagnostici e/o di laboratorio significativi"
        ));

        section.setTitle(new Title("Esami eseguiti durante il ricovero"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();
        text.setValues(textList);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();

        List<ImagingStudy> imagingStudies = FHIRClient.getInstance().getImagingStudiesForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (imagingStudies != null && !imagingStudies.isEmpty()) {
            Table imagingStudyTable = new Table();

            List<TableCell> headerCells = new ArrayList<>();
            headerCells.add(new TableCell("Study ID"));
            headerCells.add(new TableCell("Status"));
            headerCells.add(new TableCell("Started"));
            headerCells.add(new TableCell("Modality"));
            headerCells.add(new TableCell("Body Site"));
            headerCells.add(new TableCell("SOP Class"));

            TableRow headerRow = new TableRow(headerCells);
            TableHead thead = new TableHead(List.of(headerRow));


            List<TableRow> rows = new ArrayList<>();
            for (ImagingStudy imagingStudy : imagingStudies) {
                for (ImagingStudy.ImagingStudySeriesComponent series : imagingStudy.getSeries()) {
                    for (ImagingStudy.ImagingStudySeriesInstanceComponent instance : series.getInstance()) {
                        TableRow row = new TableRow();
                        List<TableCell> rowCells = new ArrayList<>();


                        rowCells.add(new TableCell(imagingStudy.getIdPart()));
                        rowCells.add(new TableCell(imagingStudy.getStatus().toString()));
                        rowCells.add(new TableCell(imagingStudy
                                .getStartedElement() != null ? imagingStudy
                                .getStartedElement().toHumanDisplay() : "N/A"));
                        rowCells.add(new TableCell(series.getModality().getCodingFirstRep().getDisplay()));
                        rowCells.add(new TableCell(series.getBodySite().getConcept().getCodingFirstRep()
                                .getDisplay()));
                        rowCells.add(new TableCell(instance.getSopClass().getDisplay()));

                        row.setCells(rowCells);
                        rows.add(row);
                    }
                }
            }
            TableBody tbody = new TableBody(rows);
            imagingStudyTable.setTbody(tbody);
            imagingStudyTable.setThead(thead);

            textList.add(imagingStudyTable);

            List<Entry> entryList = new ArrayList<>();

            for (ImagingStudy imagingStudy : imagingStudies) {
                for (ImagingStudy.ImagingStudySeriesComponent series : imagingStudy.getSeries()) {
                    for (ImagingStudy.ImagingStudySeriesInstanceComponent instance : series.getInstance()) {


                        ObservationCDA observationCDA = new ObservationCDA("OBS", "EVN");


                        Code code = new Code(
                                series.getModality().getCodingFirstRep().getCode(),
                                "1.2.840.10008.6.1.1283",
                                "DICOM CID 33 Modality",
                                series.getModality().getCodingFirstRep().getDisplay()
                        );
                        observationCDA.setCode(code);

                        ZonedDateTime zonedDateTime = imagingStudy.getStarted()
                                .toInstant()
                                .atZone(ZoneId.of("UTC"));
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        String formattedDate = zonedDateTime.format(formatter);
                        EffectiveTime effectiveTime = new EffectiveTime();
                        effectiveTime.setValue(formattedDate);

                        observationCDA.setEffectiveTime(effectiveTime);


                        String modality = series.getModality().getCodingFirstRep().getDisplay().toLowerCase();
                        String bodySite = series.hasBodySite() && series.getBodySite().hasConcept() &&
                                series.getBodySite().getConcept().hasCoding()
                                ? series.getBodySite().getConcept().getCodingFirstRep().getDisplay().toLowerCase()
                                : "unknown body site";
                        String sopClassDisplay = instance.getSopClass().getDisplay().toLowerCase();

                        String resultDescription = String.format(
                                "Exam type: %s. Body site: %s. Image type: %s.",
                                modality, bodySite, sopClassDisplay
                        );


                        ValueContent value = new ValueContent();
                        value.setType("ST");
                        value.setContent(resultDescription);
                        observationCDA.setValueContent(value);


                        StatusCode statusCode = new StatusCode("completed");
                        observationCDA.setStatusCode(statusCode);


                        Entry entry = new Entry();
                        entry.setObservation(observationCDA);
                        entryList.add(entry);
                    }
                }
            }

            section.setEntry(entryList);
        }

        if (textList.isEmpty()) {
            return null;
        }

        return component;
    }
}
