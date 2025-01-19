package com.group01.dhsa.Model.CDAResources;

import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Practitioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    void setUp() {
        builder = new CdaDocumentBuilder(1);
        FHIRClient fhirClient = new FHIRClient();
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
        } catch (JAXBException | IOException e) {
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
        } catch (JAXBException | IOException e) {
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
        } catch (JAXBException | IOException e) {
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
