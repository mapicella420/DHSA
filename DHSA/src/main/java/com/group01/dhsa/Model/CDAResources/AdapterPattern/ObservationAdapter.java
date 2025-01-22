package com.group01.dhsa.Model.CDAResources.AdapterPattern;


import org.hl7.fhir.r5.model.Observation;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.ObservationCDA;

public class ObservationAdapter implements CdaSection<ObservationCDA, Observation>{

    @Override
    public ObservationCDA toCdaObject(Observation fhirObject) {
        // Mappa direttamente l'oggetto FHIR al modello CDA
        return new ObservationCDA(
//                fhirObject.getIdElement().getIdPart(),
//                fhirObject.getAddressFirstRep().getCity(),
//                fhirObject.getAddressFirstRep().getLine().isEmpty() ? "" : fhirObject.getAddressFirstRep().getLine().get(0).toString()
        );
    }
}