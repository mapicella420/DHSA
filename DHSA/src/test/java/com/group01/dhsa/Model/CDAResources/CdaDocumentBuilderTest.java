package com.group01.dhsa.Model.CDAResources;

import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.Patient;
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

    @BeforeEach
    void setUp() {
        builder = new CdaDocumentBuilder(1);
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
}
