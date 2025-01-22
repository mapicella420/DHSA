package com.group01.dhsa.Model;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.CdaDocumentBuilder;
import com.group01.dhsa.Controller.LoggedUser;
import com.group01.dhsa.ObserverPattern.EventObservable;
import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CdaDocumentCreator {
    private final EventObservable eventObservable;

    public CdaDocumentCreator() {
        this.eventObservable = EventManager.getInstance().getEventObservable();
    }

    public CdaDocumentCreator(EventObservable eventObservable) {
        this.eventObservable = eventObservable;
    }

    public void createAndNotify(String patientId, String encounterId) {
        try {
            File cdaFile = createCdaDocument(patientId, encounterId);
            // Notifica il completamento della generazione
            eventObservable.notify("cda_generated", cdaFile);
        } catch (Exception e) {
            e.printStackTrace();
            // In caso di errore, notificare un fallimento
            eventObservable.notify("cda_generation_failed", null);
        }
    }

    private File createCdaDocument(String patientId, String encounterId) throws JAXBException {
        FHIRClient client = FHIRClient.getInstance();
        Integer idNumber = 1;

        Patient fhirPatient = client.getPatientById(patientId);
        CdaDocumentBuilder documentBuilder = new CdaDocumentBuilder(idNumber);

        documentBuilder.addPatientSection(fhirPatient);

        LoggedUser loggedUser = LoggedUser.getInstance();
        Practitioner practitioner = client.getPractitionerById(loggedUser.getFhirId());

        documentBuilder.addAuthorSection(practitioner);

        documentBuilder.addCustodianSection();

        Encounter encounter = client.getEncounterById(encounterId);
        documentBuilder.addLegalAuthenticatorSection(encounter);
        documentBuilder.addComponentOfSection(encounter);
        documentBuilder.addComponentSection(encounter);

        List<Observation> fhirObservations = client.getObservationsForPatient(patientId);

        // Costruisce il documento CDA
        try {
            File tempFile = documentBuilder.build();
            printFileContent(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to build CDA document: " + e.getMessage());
        }
    }

    private void printFileContent(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        System.out.println("CDA Document Content:\n" + content);
    }
}
