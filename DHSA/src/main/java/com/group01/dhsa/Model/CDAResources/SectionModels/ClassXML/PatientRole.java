package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import com.group01.dhsa.Model.CDAResources.SectionModels.PatientCDA;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class PatientRole {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "patient")
    private PatientCDA patient;

    // Getter e Setter
    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public PatientCDA getPatient() {
        return patient;
    }

    public void setPatient(PatientCDA patient) {
        this.patient = patient;
    }
}
