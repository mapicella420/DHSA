package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Telecom {

    @XmlAttribute(name = "use")
    private String use;

    @XmlAttribute(name = "value")
    private String value;

    public Telecom() {
    }

    public Telecom(String use, String value) {
        this.use = use;
        this.value = value;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
