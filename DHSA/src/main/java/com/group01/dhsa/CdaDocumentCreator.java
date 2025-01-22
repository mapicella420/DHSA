package com.group01.dhsa;

import com.group01.dhsa.Model.CDAResources.CdaDocumentBuilder;
import com.group01.dhsa.Controller.LoggedUser;
import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.*;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CdaDocumentCreator {
    File tempFile;

    public File createCdaDocument(String patientId, String encounterId) throws JAXBException {
        FHIRClient client = FHIRClient.getInstance();
        Integer idNumber = 1;

        Patient fhirPatient = client.getPatientById(patientId);
        CdaDocumentBuilder documentBuilder = new CdaDocumentBuilder(idNumber);

        documentBuilder.addPatientSection(fhirPatient);

        LoggedUser loggedUser = LoggedUser.getInstance();
        System.out.println(loggedUser.getFhirId());
        Practitioner practitioner = client.getPractitionerById(loggedUser.getFhirId());

        documentBuilder.addAuthorSection(practitioner);

        documentBuilder.addCustodianSection();

        Encounter encounter = client.getEncounterById(encounterId);

        documentBuilder.addLegalAuthenticatorSection(encounter);

        documentBuilder.addComponentOfSection(encounter);

        documentBuilder.addAdmissionSection(encounter);

        documentBuilder.addClinicalHistorySection(encounter);

        documentBuilder.addHospitalCourseSection(encounter);

        // Crea il file XML temporaneo
        try {
            File tempFile = documentBuilder.build();


            printFileContent(tempFile);


            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to build CDA document: " + e.getMessage());
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    // Stampa il contenuto del file XML
    private void printFileContent(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        System.out.println("CDA Document Content:\n" + content);
    }
}
