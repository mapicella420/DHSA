package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Id {

    @XmlAttribute(name = "extension")
    private String extension;

    @XmlAttribute(name = "root")
    private String root;

    @XmlAttribute(name = "assigningAuthorityName")
    private String assigningAuthorityName;

    public Id() {}

    public Id(String extension, String root, String assigningAuthorityName) {
        this.extension = extension;
        this.root = root;
        this.assigningAuthorityName = assigningAuthorityName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getAssigningAuthorityName() {
        return assigningAuthorityName;
    }

    public void setAssigningAuthorityName(String assigningAuthorityName) {
        this.assigningAuthorityName = assigningAuthorityName;
    }
}
