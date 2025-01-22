package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class EntryRelationship {

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
}
