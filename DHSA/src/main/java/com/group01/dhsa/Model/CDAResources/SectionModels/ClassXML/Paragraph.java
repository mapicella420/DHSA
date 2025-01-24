package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "paragraph")
@XmlAccessorType(XmlAccessType.FIELD)
public class Paragraph {

    @XmlValue
    private String content;

    public Paragraph() {
    }

    public Paragraph(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
