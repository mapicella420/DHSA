package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class EntryRelationship {

    @XmlAttribute(name = "typeCode")
    private String typeCode;

    @XmlElement(name = "observation")
    private ObservationCDA observation;

    public EntryRelationship() {
    }

    public ObservationCDA getObservation() {
        return observation;
    }

    public void setObservation(ObservationCDA observation) {
        this.observation = observation;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }
}
