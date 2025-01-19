package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "recordTarget")
public class RecordTarget {

    @XmlElement(name = "patientRole")
    private PatientRole patientRole;

    // Getter e Setter
    public PatientRole getPatientRole() {
        return patientRole;
    }

    public void setPatientRole(PatientRole patientRole) {
        this.patientRole = patientRole;
    }
}
