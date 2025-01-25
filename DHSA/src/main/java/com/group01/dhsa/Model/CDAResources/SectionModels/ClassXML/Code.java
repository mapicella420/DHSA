package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Code {

    @XmlAttribute(name = "nullFlavor")
    private String nullFlavor;

    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "codeSystem")
    private String codeSystem;

    @XmlAttribute(name = "codeSystemName")
    private String codeSystemName;

    //Optional
    @XmlAttribute(name = "displayName")
    private String displayName;

    public Code() {
    }

    public Code(String code, String codeSystem, String codeSystemName, String displayName) {
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
        this.displayName = displayName;
    }

    // Getters e Setters

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNullFlavor() {
        return nullFlavor;
    }

    public void setNullFlavor(String nullFlavor) {
        this.nullFlavor = nullFlavor;
    }
}

