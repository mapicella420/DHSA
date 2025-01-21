package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class RepresentedCustodianOrganization {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "addr")
    private Addr addr;

    public RepresentedCustodianOrganization() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Addr getAddr() {
        return addr;
    }

    public void setAddr(Addr addr) {
        this.addr = addr;
    }
}
