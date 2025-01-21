package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ComponentOf {

    @XmlElement(name = "encompassingEncounter")
    private EncompassingEncounter encompassingEncounter;

    public ComponentOf() {
    }

    public EncompassingEncounter getEncompassingEncounter() {
        return encompassingEncounter;
    }

    public void setEncompassingEncounter(EncompassingEncounter encompassingEncounter) {
        this.encompassingEncounter = encompassingEncounter;
    }
}
