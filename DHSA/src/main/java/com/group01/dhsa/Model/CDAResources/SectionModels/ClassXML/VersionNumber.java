package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class VersionNumber {

    @XmlAttribute(name = "value")
    private int value;

    public VersionNumber() {}

    public VersionNumber(int value) {
        this.value = value;
    }

    // Getters e Setters
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
