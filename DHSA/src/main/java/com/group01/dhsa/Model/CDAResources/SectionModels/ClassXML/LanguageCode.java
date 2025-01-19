package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class LanguageCode {

    @XmlAttribute(name = "code")
    private String code;

    public LanguageCode() {}

    public LanguageCode(String code) {
        this.code = code;
    }

    // Getters e Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

