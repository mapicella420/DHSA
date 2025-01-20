package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Custodian {

    @XmlElement(name = "assignedCustodian")
    private AssignedCustodian assignedCustodian;

    public Custodian() {
    }

    public AssignedCustodian getAssignedCustodian() {
        return assignedCustodian;
    }

    public void setAssignedCustodian(AssignedCustodian assignedCustodian) {
        this.assignedCustodian = assignedCustodian;
    }
}
