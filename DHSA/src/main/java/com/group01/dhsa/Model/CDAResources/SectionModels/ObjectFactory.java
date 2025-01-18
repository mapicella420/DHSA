package com.group01.dhsa.Model.CDAResources.SectionModels;

import jakarta.xml.bind.annotation.*;

@XmlRegistry
public class ObjectFactory {

    // Costruttore di default
    public ObjectFactory() {}

    // Metodo per creare un'istanza di ClinicalDocument
    public ClinicalDocument createClinicalDocument() {
        return new ClinicalDocument();
    }

    // Metodo per creare un'istanza di Patient
    public PatientCDA createPatient() {
        return new PatientCDA();
    }

    // Metodo per creare un'istanza di Observation
    public ObservationCDA createObservation() {
        return new ObservationCDA();
    }
}