package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class StructuredBody {

    @XmlElement(name = "component")
    private ComponentInner component;

    @XmlAttribute(name = "classCode")
    private String classCode;

    @XmlAttribute(name = "moodCode")
    private String moodCode;

    public StructuredBody() {
    }

    public ComponentInner getComponent() {
        return component;
    }

    public void setComponent(ComponentInner component) {
        this.component = component;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getMoodCode() {
        return moodCode;
    }

    public void setMoodCode(String moodCode) {
        this.moodCode = moodCode;
    }
}
