package com.group01.dhsa;

import com.group01.dhsa.Model.CDAResources.CdaDocumentBuilder;
import com.group01.dhsa.Controller.LoggedUser;
import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;

public class CdaDocumentCreator {
    File tempFile;
    // Metodo principale per creare il documento CDA
    public void createCdaDocument(String patientId, String encounterId) throws JAXBException {
        FHIRClient client = FHIRClient.getInstance();

        Patient fhirPatient = client.getPatientById(patientId);

        //CERCARE QUANTE DISCHARGE CI SONO PER IL NUMERO
        Integer idNumber = 1;

        CdaDocumentBuilder documentBuilder = new CdaDocumentBuilder(idNumber);

        documentBuilder.addPatientSection(fhirPatient);

        LoggedUser loggedUser = LoggedUser.getInstance();
        System.out.println(loggedUser.getFhirId());
        Practitioner practitioner = client.getPractitionerById(loggedUser.getFhirId());

        documentBuilder.addAuthorSection(practitioner);

        documentBuilder.addCustodianSection();

//        Encounter encounter = client.getEncounterFromPractitionerAndPatient(practitioner.getIdentifierFirstRep().getValue(), fhirPatient.getIdentifierFirstRep().getValue());
        Encounter encounter = client.getEncounterById(encounterId);

        //Lemuel paziente di questo id
        documentBuilder.addLegalAuthenticatorSection(encounter);

        documentBuilder.addComponentOfSection(encounter);

        documentBuilder.addComponentSection(encounter);

        // Recupera osservazioni
        List<Observation> fhirObservations = client.getObservationsForPatient(patientId);
//        List<ObservationAdapter> observationAdapters = fhirObservations.stream()
//                .map(ObservationAdapter::new)
//                .collect(Collectors.toList());

        // 3. Aggiungi la sezione osservazione
//        documentBuilder.addObservationSection(fhirObservations.getFirst());

        // 4. Costruisci e serializza il documento CDA in XML
        // Crea il file XML temporaneo
        try {
            this.tempFile = documentBuilder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(tempFile.getAbsolutePath())));
            System.out.println("File content:\n" + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo che decide se salvare il file nel database o meno
    public void handleFileSave() {
        boolean shouldSaveInDb = shouldSaveToDb();

        if (shouldSaveInDb) {
            saveFileToDb(this.tempFile);
        } else {
            System.out.println("File temporaneo creato: " + this.tempFile.getAbsolutePath());
        }

        // Puoi anche decidere di eliminare il file temporaneo manualmente dopo l'uso
        deleteTempFile(this.tempFile);
    }

    private void saveFileToDb(File tempFile) {
        // Logica per salvare il contenuto del file nel database
        System.out.println("Salvando il file nel database...");
    }

    private void deleteTempFile(File tempFile) {
        if (tempFile.delete()) {
            System.out.println("File temporaneo eliminato.");
        } else {
            System.out.println("Errore nell'eliminazione del file temporaneo.");
        }
    }

    private boolean shouldSaveToDb() {
        return true;
    }
}