package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.Model.CDAResources.SectionModels.PatientCDA;
import org.hl7.fhir.r5.model.Patient;

public class PatientAdapter implements CdaSection<PatientCDA, Patient> {

    @Override
    public PatientCDA toCdaObject(Patient fhirObject) {
        return new PatientCDA(
                fhirObject.getIdElement().getIdPart(),
                fhirObject.getAddressFirstRep().getCity(),
                fhirObject.getAddressFirstRep().getLine().isEmpty() ? "" : fhirObject.getAddressFirstRep().getLine().get(0).toString()
        );
    }
}