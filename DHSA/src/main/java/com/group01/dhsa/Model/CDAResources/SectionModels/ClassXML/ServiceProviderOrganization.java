package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceProviderOrganization {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "telecom")
    private Telecom telecom;

    @XmlElement(name = "asOrganizationPartOf")
    private AsOrganizationPartOf asOrganizationPartOf;

    public ServiceProviderOrganization() {
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

    public Telecom getTelecom() {
        return telecom;
    }

    public void setTelecom(Telecom telecom) {
        this.telecom = telecom;
    }

    public AsOrganizationPartOf getAsOrganizationPartOf() {
        return asOrganizationPartOf;
    }

    public void setAsOrganizationPartOf(AsOrganizationPartOf asOrganizationPartOf) {
        this.asOrganizationPartOf = asOrganizationPartOf;
    }
}
