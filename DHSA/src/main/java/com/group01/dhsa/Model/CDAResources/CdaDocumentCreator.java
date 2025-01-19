package com.group01.dhsa.Model.CDAResources;

import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;

public class CdaDocumentCreator {
    File tempFile;
    // Metodo principale per creare il documento CDA
    public void createCdaDocument(String patientId) throws JAXBException {
        FHIRClient client = new FHIRClient();

        // Recupera paziente
        Patient fhirPatient = client.getPatientById(patientId);

        //CERCARE QUANTE DISCHARGE CI SONO PER IL NUMERO
        Integer idNumber = 1;

        // 1. Crea un oggetto CdaDocumentBuilder per costruire il documento CDA
        CdaDocumentBuilder documentBuilder = new CdaDocumentBuilder(idNumber);

        // 2. Aggiungi la sezione paziente
//        documentBuilder.addPatientSection(fhirPatient);

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