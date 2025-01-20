package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class AssignedEntity {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "addr")
    private Addr addr;

    @XmlElement(name = "assignedPerson")
    private AssignedPerson assignedPerson;

    @XmlElement(name = "representedOrganization")
    private RepresentedOrganization representedOrganization;

    public AssignedEntity() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Addr getAddr() {
        return addr;
    }

    public void setAddr(Addr addr) {
        this.addr = addr;
    }

    public AssignedPerson getAssignedPerson() {
        return assignedPerson;
    }

    public void setAssignedPerson(AssignedPerson assignedPerson) {
        this.assignedPerson = assignedPerson;
    }

    public RepresentedOrganization getRepresentedOrganization() {
        return representedOrganization;
    }

    public void setRepresentedOrganization(RepresentedOrganization representedOrganization) {
        this.representedOrganization = representedOrganization;
    }
}
