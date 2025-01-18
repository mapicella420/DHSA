package com.group01.dhsa.Model.CDAResources;

import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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
        tempFile.delete();
    }

    @Test
    void build() {
        try {
            tempFile = builder.build();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(tempFile.exists());
        System.out.println(tempFile.getAbsolutePath());
        System.out.println(tempFile);
    }
}