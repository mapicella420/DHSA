package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class AssignedAuthor {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "assignedPerson")
    private AssignedPerson assignedPerson;

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
}
