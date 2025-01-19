package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "author")
public class Author {

    @XmlAttribute(name = "time")
    private String time;

    @XmlElement(name = "assignedAuthor")
    private AssignedAuthor assignedAuthor;

    public Author() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public AssignedAuthor getAssignedAuthor() {
        return assignedAuthor;
    }

    public void setAssignedAuthor(AssignedAuthor assignedAuthor) {
        this.assignedAuthor = assignedAuthor;
    }
}
