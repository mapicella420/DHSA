package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class BirthTime {

    @XmlAttribute(name = "value")
    private String value;

    // Getter e Setter
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

