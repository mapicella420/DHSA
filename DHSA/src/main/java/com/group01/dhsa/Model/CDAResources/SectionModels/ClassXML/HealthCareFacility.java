package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class HealthCareFacility {

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "location")
    private LocationHealth location;

    @XmlElement(name = "serviceProviderOrganization")
    private ServiceProviderOrganization serviceProviderOrganization;

    public HealthCareFacility() {
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public LocationHealth getLocation() {
        return location;
    }

    public void setLocation(LocationHealth location) {
        this.location = location;
    }

    public ServiceProviderOrganization getServiceProviderOrganization() {
        return serviceProviderOrganization;
    }

    public void setServiceProviderOrganization(ServiceProviderOrganization serviceProviderOrganization) {
        this.serviceProviderOrganization = serviceProviderOrganization;
    }
}
