package com.group01.dhsa.Model.CDAResources;

import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import java.util.*;
import java.util.stream.Collectors;

public class CdaDocumentCreator {
    // Metodo principale per creare il documento CDA
    public void createCdaDocument(String patientId) throws JAXBException {
        // 1. Crea un oggetto CdaDocumentBuilder per costruire il documento CDA
        CdaDocumentBuilder documentBuilder = new CdaDocumentBuilder();

        FHIRClient client = new FHIRClient();

        // Recupera paziente
        Patient fhirPatient = client.getPatientById(patientId);

        // 2. Aggiungi la sezione paziente
        documentBuilder.addPatientSection(fhirPatient);

        // Recupera osservazioni
        List<Observation> fhirObservations = client.getObservationsForPatient(patientId);
//        List<ObservationAdapter> observationAdapters = fhirObservations.stream()
//                .map(ObservationAdapter::new)
//                .collect(Collectors.toList());

        // 3. Aggiungi la sezione osservazione
        documentBuilder.addObservationSection(fhirObservations.getFirst());

        // 4. Costruisci e serializza il documento CDA in XML
        documentBuilder.build();
    }
}