package com.group01.dhsa.Model.CDAResources.SectionModels;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class PatientCDA {

    @XmlElement
    private String id;

    @XmlElement
    private String city;

    @XmlElement
    private String streetAddressLine;

    public PatientCDA(String id, String city, String streetAddressLine) {
        this.id = id;
        this.city = city;
        this.streetAddressLine = streetAddressLine;
    }

    public PatientCDA() {} // Costruttore senza argomenti per JAXB

    // Getter e setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreetAddressLine() {
        return streetAddressLine;
    }

    public void setStreetAddressLine(String streetAddressLine) {
        this.streetAddressLine = streetAddressLine;
    }
}


