package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Location {

    @XmlElement(name = "healthCareFacility")
    private HealthCareFacility healthCareFacility;

    public Location() {
    }

    public HealthCareFacility getHealthCareFacility() {
        return healthCareFacility;
    }

    public void setHealthCareFacility(HealthCareFacility healthCareFacility) {
        this.healthCareFacility = healthCareFacility;
    }
}
