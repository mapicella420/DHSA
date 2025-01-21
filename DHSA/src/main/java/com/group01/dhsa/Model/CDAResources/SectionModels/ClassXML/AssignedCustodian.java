package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class AssignedCustodian {

    @XmlElement(name = "representedCustodianOrganization")
    private RepresentedCustodianOrganization representedCustodianOrganization;

    public AssignedCustodian() {
    }

    public RepresentedCustodianOrganization getRepresentedCustodianOrganization() {
        return representedCustodianOrganization;
    }

    public void setRepresentedCustodianOrganization(RepresentedCustodianOrganization representedCustodianOrganization) {
        this.representedCustodianOrganization = representedCustodianOrganization;
    }
}
