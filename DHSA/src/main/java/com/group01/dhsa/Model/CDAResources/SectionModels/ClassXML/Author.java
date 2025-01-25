package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "author")
public class Author {

    @XmlElement(name = "time")
    private Time time;

    @XmlElement(name = "assignedAuthor")
    private AssignedAuthor assignedAuthor;

    //Optional
    @XmlElement(name = "representedOrganization")
    private RepresentedOrganization representedOrganization;

    public Author() {
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public AssignedAuthor getAssignedAuthor() {
        return assignedAuthor;
    }

    public void setAssignedAuthor(AssignedAuthor assignedAuthor) {
        this.assignedAuthor = assignedAuthor;
    }

    public RepresentedOrganization getRepresentedOrganization() {
        return representedOrganization;
    }

    public void setRepresentedOrganization(RepresentedOrganization representedOrganization) {
        this.representedOrganization = representedOrganization;
    }
}
