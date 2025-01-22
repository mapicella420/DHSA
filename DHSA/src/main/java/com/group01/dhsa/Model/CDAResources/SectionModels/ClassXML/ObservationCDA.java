package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ObservationCDA {

    @XmlAttribute(name = "classCode")
    private String classCode;

    @XmlAttribute(name = "moodCode")
    private String moodCode;

    @XmlElement(name = "code")
    private Code code;

    @XmlElement(name = "value")
    private Value value;

    public ObservationCDA() {
    }

    public ObservationCDA(String classCode, String moodCode) {
        this.classCode = classCode;
        this.moodCode = moodCode;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
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