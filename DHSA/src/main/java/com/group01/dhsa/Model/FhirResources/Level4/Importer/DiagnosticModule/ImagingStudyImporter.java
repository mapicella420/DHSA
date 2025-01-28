package com.group01.dhsa.Model.FhirResources.Level4.Importer.DiagnosticModule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Controller.LoggedUser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.hl7.fhir.r5.model.*;
import com.group01.dhsa.Model.FhirResources.*;
import java.io.FileReader;
import java.io.Reader;

public class ImagingStudyImporter implements FhirResourceImporter {

    private static String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    private static void setFhirServerUrl() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIR_SERVER_URL = "http://localhost:8081/fhir";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIR_SERVER_URL = "http://localhost:8080/fhir";
            }
        }
    }
    @Override
    public void importCsvToFhir(String csvFilePath) {
        setFhirServerUrl();
        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Read CSV file
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .withFirstRecordAsHeader()
                    .parse(in);

            // Iterate over CSV records
            for (CSVRecord record : records) {
                ImagingStudy imagingStudy = new ImagingStudy();

                // Set Identifier (Study UID)
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    imagingStudy.addIdentifier(new Identifier().setValue(record.get("Id")));
                }

                // Verify if ImagingStudy already exists
                if (record.isMapped("Id") && imagingStudyExistsByIdentifier(client, record.get("Id"))) {
                    System.out.println("ImagingStudy with identifier " + record.get("Id") + " already exists. Skipping.");
                    continue;
                }

                // Validate and set Patient
                if (record.isMapped("PATIENT") && !record.get("PATIENT").isEmpty()) {
                    String patientIdentifier = record.get("PATIENT");
                    if (patientExistsByIdentifier(client, patientIdentifier)) {
                        imagingStudy.setSubject(new Reference("Patient?identifier=" + patientIdentifier));
                    } else {
                        System.out.println("Patient not found: " + patientIdentifier + ". Skipping imaging study.");
                        continue;
                    }
                }

                // Validate and set Encounter
                if (record.isMapped("ENCOUNTER") && !record.get("ENCOUNTER").isEmpty()) {
                    String encounterIdentifier = record.get("ENCOUNTER");
                    if (encounterExistsByIdentifier(client, encounterIdentifier)) {
                        imagingStudy.setEncounter(new Reference("Encounter?identifier=" + encounterIdentifier));
                    } else {
                        System.out.println("Encounter not found: " + encounterIdentifier + ". Skipping imaging study.");
                        continue;
                    }
                }

                // Set Started Date
                if (record.isMapped("DATE") && !record.get("DATE").isEmpty()) {
                    imagingStudy.setStartedElement(new DateTimeType(record.get("DATE")));
                }

                // Add BodySite
                if (record.isMapped("BODYSITE_CODE") && !record.get("BODYSITE_CODE").isEmpty()) {
                    ImagingStudy.ImagingStudySeriesComponent series = imagingStudy.addSeries();
                    CodeableReference bodySiteReference = new CodeableReference(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setCode(record.get("BODYSITE_CODE"))
                                    .setDisplay(record.get("BODYSITE_DESCRIPTION"))));
                    series.setBodySite(bodySiteReference);
                }

                // Add Modality
                if (record.isMapped("MODALITY_CODE") && !record.get("MODALITY_CODE").isEmpty()) {
                    ImagingStudy.ImagingStudySeriesComponent series = imagingStudy.getSeries().get(0);
                    CodeableConcept modality = new CodeableConcept().addCoding(new Coding()
                            .setCode(record.get("MODALITY_CODE"))
                            .setDisplay(record.get("MODALITY_DESCRIPTION")));
                    series.setModality(modality);
                }


                // Add SOP Instance
                if (record.isMapped("SOP_CODE") && !record.get("SOP_CODE").isEmpty()) {
                    ImagingStudy.ImagingStudySeriesComponent series = imagingStudy.getSeries().get(0);
                    ImagingStudy.ImagingStudySeriesInstanceComponent instance = series.addInstance();
                    instance.setSopClass(new Coding().setCode(record.get("SOP_CODE")).setDisplay(record.get("SOP_DESCRIPTION")));
                }

                // Set Status (default to "available")
                imagingStudy.setStatus(ImagingStudy.ImagingStudyStatus.AVAILABLE);

                // Send ImagingStudy to FHIR server
                client.create().resource(imagingStudy).execute();

                // Log success
                System.out.println("ImagingStudy with ID " + record.get("Id") + " uploaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error during CSV import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates if a Patient exists on the FHIR server by identifier.
     */
    private boolean patientExistsByIdentifier(IGenericClient client, String patientIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Patient")
                    .where(Patient.IDENTIFIER.exactly().identifier(patientIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Patient by identifier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if an Encounter exists on the FHIR server by identifier.
     */
    private boolean encounterExistsByIdentifier(IGenericClient client, String encounterIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Encounter")
                    .where(Encounter.IDENTIFIER.exactly().identifier(encounterIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking Encounter by identifier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if an ImagingStudy exists on the FHIR server by identifier.
     */
    private boolean imagingStudyExistsByIdentifier(IGenericClient client, String studyIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("ImagingStudy")
                    .where(ImagingStudy.IDENTIFIER.exactly().identifier(studyIdentifier))
                    .returnBundle(Bundle.class)
                    .execute();
            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking ImagingStudy by identifier: " + e.getMessage());
            return false;
        }
    }
}
