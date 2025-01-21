package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResponsibleParty {

    @XmlElement(name = "assignedEntity")
    private AssignedEntity assignedEntity;

    public ResponsibleParty() {
    }

    public AssignedEntity getAssignedEntity() {
        return assignedEntity;
    }

    public void setAssignedEntity(AssignedEntity assignedEntity) {
        this.assignedEntity = assignedEntity;
    }
}
