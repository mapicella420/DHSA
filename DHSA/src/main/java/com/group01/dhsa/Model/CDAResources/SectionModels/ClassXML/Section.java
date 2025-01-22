package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "code", "title", "entry", "text", "component" })
public class Section {

    @XmlAttribute(name = "classCode")
    private String classCode;

    @XmlAttribute(name = "moodCode")
    private String moodCode;

    @XmlElement(name = "code")
    private Code code;

    @XmlElement(name = "title")
    private Title title;

    @XmlElement(name = "entry")
    private List<Entry> entry;

    @XmlElement(name = "text")
    private Text text;

    @XmlElement(name = "component")
    private Component component;

    public Section() {
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

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public List<Entry> getEntry() {
        return entry;
    }

    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
