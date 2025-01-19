package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class TemplateId {

    @XmlAttribute(name = "extension")
    private String extension;

    @XmlAttribute(name = "root")
    private String root;

    public TemplateId() {}

    public TemplateId(String extension, String root) {
        this.extension = extension;
        this.root = root;
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
}
