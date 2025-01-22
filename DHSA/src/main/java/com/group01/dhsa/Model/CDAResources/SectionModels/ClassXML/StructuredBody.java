package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuredBody {

    @XmlElement(name = "component")
    private ComponentInner componentInner;

    @XmlAttribute(name = "classCode")
    private String classCode;

    @XmlAttribute(name = "moodCode")
    private String moodCode;

    public StructuredBody() {
    }

    public ComponentInner getComponentInner() {
        return componentInner;
    }

    public void setComponentInner(ComponentInner componentInner) {
        this.componentInner = componentInner;
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
