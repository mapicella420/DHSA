package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Component {

    @XmlElement(name = "structuredBody")
    private StructuredBody structuredBody;

    public Component() {
    }

    public StructuredBody getStructuredBody() {
        return structuredBody;
    }

    public void setStructuredBody(StructuredBody structuredBody) {
        this.structuredBody = structuredBody;
    }

}
