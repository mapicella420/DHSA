package com.group01.dhsa.Model.CDAResources.SectionModels;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ObservationCDA {

    @XmlElement
    private String code;

    @XmlElement
    private String value;

    public ObservationCDA(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public ObservationCDA() {
    } // Costruttore vuoto per JAXB

    // Getter e setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}