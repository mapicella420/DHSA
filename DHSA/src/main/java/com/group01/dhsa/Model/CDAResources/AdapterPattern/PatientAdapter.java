package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import org.hl7.fhir.r5.model.Patient;

public class PatientAdapter implements CdaSection {
    private Patient patient;

    public PatientAdapter(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String toCdaXml() {
        return "<recordTarget>" +
                "<patientRole>" +
                "<id root='2.16.840.1.113883.2.9.2.4.3.2' extension='" + patient.getId() + "'/>" +
                "<addr>" +
                "<city>" + patient.getAddressFirstRep().getCity() + "</city>" +
                "<streetAddressLine>" + patient.getAddressFirstRep().getLine().get(0) + "</streetAddressLine>" +
                "</addr>" +
                "</patientRole>" +
                "</recordTarget>";
    }
}
