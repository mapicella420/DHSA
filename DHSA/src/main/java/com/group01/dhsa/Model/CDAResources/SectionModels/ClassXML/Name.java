package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "given", "family"})
public class Name {

    @XmlElement(name = "family")
    private String family;

    @XmlElement(name = "given")
    private String given;

    public Name() {
    }

    public Name(String given, String family) {}

    // Getter e Setter
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGiven() {
        return given;
    }

    public void setGiven(String given) {
        this.given = given;
    }

}
