package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {

    @XmlAttribute(name = "xsi:type")
    private String type;

    @XmlAttribute(name = "code")
    private String code;

    @XmlAttribute(name = "codeSystem")
    private String codeSystem;

    @XmlAttribute(name = "codeSystemName")
    private String codeSystemName;

    @XmlAttribute(name = "displayName")
    private String displayName;

    @XmlElement(name = "translation")
    private Translation translation;

    public Value() {
        this.type = "CD";
    }

    public Value(String code, String codeSystem, String codeSystemName, String displayName) {
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
        this.displayName = displayName;
    }

    public Value(String type, String code, String codeSystem, String codeSystemName, String displayName) {
        this.type = type;
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
        this.displayName = displayName;
    }

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

    public Translation getTranslation() {
        return translation;
    }

    public void setTranslation(Translation translation) {
        this.translation = translation;
    }
}
