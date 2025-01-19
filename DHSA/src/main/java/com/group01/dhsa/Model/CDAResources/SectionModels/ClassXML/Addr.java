package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Addr {

    @XmlAttribute(name = "use")
    private String use;

    @XmlElement(name = "country")
    private String country;

    @XmlElement(name = "state")
    private String state;

    @XmlElement(name = "county")
    private String county;

    @XmlElement(name = "city")
    private String city;

    @XmlElement(name = "censusTract")
    private String censusTract;

    @XmlElement(name = "postalCode")
    private String postalCode;

    @XmlElement(name = "streetAddressLine")
    private String streetAddressLine;

    // Getters e Setters
    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCensusTract() {
        return censusTract;
    }

    public void setCensusTract(String censusTract) {
        this.censusTract = censusTract;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreetAddressLine() {
        return streetAddressLine;
    }

    public void setStreetAddressLine(String streetAddressLine) {
        this.streetAddressLine = streetAddressLine;
    }
}
