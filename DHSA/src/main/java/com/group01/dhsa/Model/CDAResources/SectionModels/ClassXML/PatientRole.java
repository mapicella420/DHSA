package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class PatientRole {

    @XmlElement(name = "id")
    private Id id;

    //Optional
    @XmlElement(name = "addr")
    private Addr addr;

    @XmlElement(name = "patient")
    private PatientCDA patient;

    public PatientRole() {
    }

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

    public Addr getAddr() {
        return addr;
    }

    public void setAddr(Addr addr) {
        this.addr = addr;
    }
}
