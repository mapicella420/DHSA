package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class AssignedAuthor {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "assignedPerson")
    private AssignedPerson assignedPerson;

    //Empty because we don't have data, but it's mandatory to have
    @XmlElement(name = "telecom")
    private Telecom telecom;

    public AssignedAuthor() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public AssignedPerson getAssignedPerson() {
        return assignedPerson;
    }

    public void setAssignedPerson(AssignedPerson assignedPerson) {
        this.assignedPerson = assignedPerson;
    }

    public Telecom getTelecom() {
        return telecom;
    }

    public void setTelecom(Telecom telecom) {
        this.telecom = telecom;
    }
}
