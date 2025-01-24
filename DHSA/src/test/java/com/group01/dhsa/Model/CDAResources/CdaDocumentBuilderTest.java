package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.FHIRClient;
import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Practitioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class CdaDocumentBuilderTest {
    File tempFile;
    CdaDocumentBuilder builder;
    Patient patient;
    Practitioner practitioner;
    FHIRClient fhirClient = FHIRClient.getInstance();
    Encounter encounter = fhirClient.getEncounterById("9331c45b-0beb-42aa-1a13-012a432f7c3c");


    @BeforeEach
    void setUp() {
        builder = new CdaDocumentBuilder(1);
        FHIRClient fhirClient = FHIRClient.getInstance();
        String patientId = "8b0484cd-3dbd-8b8d-1b72-a32f74a5a846";

        patient = fhirClient.getPatientById(patientId);

        String practitionerId = "7a0d9463-9b7b-3c24-b14f-928d19dd5a32";
        practitioner = fhirClient.getPractitionerById(practitionerId);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            System.out.println("Deleting temporary file: " + tempFile.getAbsolutePath());
            tempFile.delete();
        }
    }

    @Test
    void build() {
        try {
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }

        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");
    }

    @Test
    void buildWithPatient() {
        try {
            builder.addPatientSection(patient);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }

        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");
    }

    @Test
    void buildWithAuthor(){
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }

        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");
    }

    @Test
    void buildWithCustodian() {
        try {

            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }


        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");
    }

    @Test
    void buildWithLegalAuthenticator() {
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            builder.addLegalAuthenticatorSection(encounter);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");

    }

    @Test
    void buildWithComponentOf() {
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            builder.addLegalAuthenticatorSection(encounter);
            builder.addComponentOfSection(encounter);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");

    }

    @Test
    void buildWithAdmission() {
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            builder.addLegalAuthenticatorSection(encounter);
            builder.addComponentOfSection(encounter);
            builder.addAdmissionSection(encounter);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
      }
        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");

    }

    @Test
    void buildWithClinicalHistory(){
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            builder.addLegalAuthenticatorSection(encounter);
            builder.addComponentOfSection(encounter);
            builder.addAdmissionSection(encounter);
            builder.addClinicalHistorySection(encounter);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");

    }

    @Test
    void buidHospitalCourse(){
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            builder.addLegalAuthenticatorSection(encounter);
            builder.addComponentOfSection(encounter);
            builder.addAdmissionSection(encounter);
            builder.addClinicalHistorySection(encounter);
            builder.addHospitalCourseSection(encounter);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");

    }

    @Test
    void buildHospitalDischargeStudies(){
        try {
            builder.addPatientSection(patient);
            builder.addAuthorSection(practitioner);
            builder.addCustodianSection();
            builder.addLegalAuthenticatorSection(encounter);
            builder.addComponentOfSection(encounter);
            builder.addAdmissionSection(encounter);
            builder.addClinicalHistorySection(encounter);
            builder.addHospitalCourseSection(encounter);
            builder.addHospitalDischargeStudiesSection(encounter);
            tempFile = builder.build();
        } catch (JAXBException | IOException | TransformerException e) {
            throw new RuntimeException(e);
        }
        assertTrue(tempFile.exists());

        System.out.println("Temporary file path: " + tempFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(tempFile.length() > 0, "The generated file is empty.");

    }
}
