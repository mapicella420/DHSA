package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Act {

    @XmlAttribute(name = "classCode")
    private String classCode;

    @XmlAttribute(name = "moodCode")
    private String moodCode;

    @XmlElement(name = "code")
    private Code code;

    @XmlElement(name = "statusCode")
    private StatusCode statusCode;

    @XmlElement(name = "effectiveTime")
    private EffectiveTime effectiveTime;

    @XmlElement(name = "entryRelationship")
    private EntryRelationship entryRelationship;

    public Act() {
    }

    public Act(String classCode, String moodCode) {
        this.classCode = classCode;
        this.moodCode = moodCode;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public EffectiveTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(EffectiveTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public EntryRelationship getEntryRelationship() {
        return entryRelationship;
    }

    public void setEntryRelationship(EntryRelationship entryRelationship) {
        this.entryRelationship = entryRelationship;
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
