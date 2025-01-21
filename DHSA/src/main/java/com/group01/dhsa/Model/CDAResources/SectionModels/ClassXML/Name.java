package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "given", "family","prefix"})
public class Name {

    @XmlElement(name = "family")
    private String family;

    @XmlElement(name = "given")
    private String given;

    @XmlElement(name = "prefix")
    private String prefix;

    public Name() {
    }

    public Name(String given, String family) {
        this.given = given;
        this.family = family;
    }

    public Name(String family, String given, String prefix) {
        this.family = family;
        this.given = given;
        this.prefix = prefix;
    }

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

    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
