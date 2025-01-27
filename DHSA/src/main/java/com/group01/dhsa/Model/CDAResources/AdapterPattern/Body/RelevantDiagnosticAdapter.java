package com.group01.dhsa.Model.CDAResources.AdapterPattern.Body;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.CdaSection;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import com.group01.dhsa.Model.LoggedUser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.ImagingStudy;
import org.hl7.fhir.r5.model.Patient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RelevantDiagnosticAdapter implements CdaSection<Component, Encounter> {

    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "dicomFiles";

    public static void setMongoUri() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
            }
        }
    }

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

        //Optional
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

        Patient patient = FHIRClient.getInstance().getPatientFromId(patientId);
        List<Document> dicom = searchDicomFiles(patient.getName().getFirst().getGiven()
                        .getFirst().toString() + " " +
                patient.getNameFirstRep().getFamily(),
                fhirObject.getActualPeriod().getStartElement());

        if ((imagingStudies != null && !imagingStudies.isEmpty()) ||
        (dicom != null && !dicom.isEmpty())) {
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
            if (imagingStudies != null && !imagingStudies.isEmpty()) {
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
            }

            if (dicom != null && !dicom.isEmpty()) {
                for (Document dicomFile : dicom) {
                    TableRow row = new TableRow();
                    List<TableCell> rowCells = new ArrayList<>();

                    rowCells.add(new TableCell(getFieldValue(dicomFile, "seriesInstanceUID")));
                    rowCells.add(new TableCell("available"));
                    String date = getFieldValue(dicomFile, "studyDate");
                    String time = getFieldValue(dicomFile, "studyTime");

                    try {
                        Date parsedDate = new SimpleDateFormat("yyyyMMdd").parse(date);

                        Date parsedTime = new SimpleDateFormat("HHmmss").parse(time);

                        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(parsedDate);
                        String formattedTime = new SimpleDateFormat("HH:mm:ss").format(parsedTime);

                        rowCells.add(new TableCell(formattedDate + " " + formattedTime));
                    } catch (ParseException e) {
                        System.err.println("[ERROR] Error parsing date or time: " + e.getMessage());
                        rowCells.add(new TableCell("Invalid Date/Time"));
                    }
                    rowCells.add(new TableCell(getFieldValue(dicomFile, "modality")));

                    //Body Site and Sop not present in DicomFiles
                    rowCells.add(new TableCell(" "));
                    rowCells.add(new TableCell(" "));

                    row.setCells(rowCells);
                    rows.add(row);
                }

            }


            TableBody tbody = new TableBody(rows);
            imagingStudyTable.setTbody(tbody);
            imagingStudyTable.setThead(thead);

            textList.add(imagingStudyTable);

            //Entries are optional
            List<Entry> entryList = new ArrayList<>();

            if (imagingStudies != null && !imagingStudies.isEmpty()) {
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


                            String modality = series.getModality().getCodingFirstRep().getDisplay();
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
            }
            if (dicom != null && !dicom.isEmpty()) {
                for (Document dicomFile : dicom) {

                    ObservationCDA observationCDA = new ObservationCDA("OBS", "EVN");

                    Code code = new Code(
                            getFieldValue(dicomFile, "modality"),
                            "1.2.840.10008.6.1.1283",
                            "DICOM CID 33 Modality"
                    );
                    observationCDA.setCode(code);

                    String date = getFieldValue(dicomFile, "studyDate");
                    String time = getFieldValue(dicomFile, "studyTime");
                    EffectiveTime effectiveTime;

                    try {
                        String combinedDateTime = date + time;

                        Date parsedDateTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(combinedDateTime);

                        String formattedDateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(parsedDateTime);

                        effectiveTime = new EffectiveTime();
                        effectiveTime.setValue(formattedDateTime);

                    } catch (ParseException e) {
                        System.err.println("[ERROR] Error parsing date or time: " + e.getMessage());
                        effectiveTime = new EffectiveTime();
                        effectiveTime.setValue("Invalid Date");
                    }
                    observationCDA.setEffectiveTime(effectiveTime);

                    String modality = getFieldValue(dicomFile, "modality");
                    String accessionNumber = getFieldValue(dicomFile, "accessionNumber") != null
                            ? getFieldValue(dicomFile, "accessionNumber")
                            : "unknown";

                    String resultDescription = String.format(
                            "Exam type: %s. Accession number: %s.",
                            modality, accessionNumber
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
            section.setEntry(entryList);
        }

        if (textList.isEmpty()) {
            return null;
        }

        return component;
    }

    private List<Document> searchDicomFiles(String patientName, DateTimeType encounterDate) {
        setMongoUri();
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            List<Document> files = collection.find().into(new java.util.ArrayList<>());
            files.forEach(doc -> System.out.println("[DEBUG] Loaded document: " + doc.toJson()));

            return files.stream()
                    .filter(doc -> {
                        String patientNameMongo = doc.getString("patientName");
                        boolean name = isNameMatch(patientNameMongo, patientName);
                                //patientName.equalsIgnoreCase(patientNameMongo);

                        String date = getFieldValue(doc, "studyDate");

                        String encounterD = encounterDate != null && encounterDate.getValue() != null
                                ? new SimpleDateFormat("yyyyMMdd").format(encounterDate.getValue()) // Formatta la data
                                : "Invalid Date";

                        boolean isSameDate = date.equals(encounterD);

                        return name && isSameDate;
                    })
                    .toList();

        } catch (Exception e) {
            System.err.println("[ERROR] Error loading DICOM files: " + e.getMessage());
        }

        return null;
    }


    private String getFieldValue(Document document, String fieldName) {
        try {
            Object value = document.get(fieldName);
            if (value instanceof ObjectId) {
                return value.toString();
            } else if (value instanceof String) {
                return (String) value;
            } else if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error getting field value for '" + fieldName + "': " + e.getMessage());
        }
        return "N/A";
    }

    private boolean isNameMatch(String dbPatientName, String inputPatientName) {

        String[] dbParts = dbPatientName.split(" ");
        String[] inputParts = inputPatientName.split(" ");


        if (dbParts.length <= inputParts.length) {
            for (String dbPart : dbParts) {
                if (!Arrays.asList(inputParts).contains(dbPart)) {
                    return false;
                }
            }
            return true;
        }


        for (String inputPart : inputParts) {
            if (!Arrays.asList(dbParts).contains(inputPart)) {
                return false;
            }
        }
        return true;
    }

}


