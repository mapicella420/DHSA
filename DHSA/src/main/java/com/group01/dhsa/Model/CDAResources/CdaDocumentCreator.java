package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.ObservationAdapter;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.PatientAdapter;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;

import java.util.List;
import java.util.stream.Collectors;

public class CdaDocumentCreator {

    public static void createCdaDocument(String patientId) {
        FHIRClient client = new FHIRClient();

        // Recupera paziente
        Patient patient = client.getPatientById(patientId);
        PatientAdapter patientAdapter = new PatientAdapter(patient);

        // Recupera osservazioni
        List<Observation> observations = client.getObservationsForPatient("123456789");
        List<ObservationAdapter> observationAdapters = observations.stream()
                .map(ObservationAdapter::new)
                .collect(Collectors.toList());

//        // Recupera incontri
//        Bundle encounterBundle = client.getEncountersForPatient("123456789");
//        Encounter encounter = (Encounter) encounterBundle.getEntryFirstRep().getResource();
//        EncounterAdapter encounterAdapter = new EncounterAdapter(encounter);

        // Costruisci il documento CDA
        CdaDocumentBuilder cdaBuilder = new CdaDocumentBuilder();
        cdaBuilder.addPatient(patientAdapter);
        observationAdapters.forEach(cdaBuilder::addObservation);
//        cdaBuilder.addEncounter(encounterAdapter);

        // Ottieni il documento CDA finale
        String cdaDocument = cdaBuilder.build();

        // Stampa il risultato (o salvalo in un file)
        System.out.println(cdaDocument);
    }
}
