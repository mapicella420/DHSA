package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;


import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Entry {

    @XmlElement(name = "entryRelationship")
    private EntryRelationship entryRelationship;

    @XmlElement(name = "act")
    private Act act;

    //Optional
    @XmlElement(name = "observation")
    private List<ObservationCDA> observation;

    public Entry() {
    }

    public EntryRelationship getEntryRelationship() {
        return entryRelationship;
    }

    public void setEntryRelationship(EntryRelationship entryRelationship) {
        this.entryRelationship = entryRelationship;
    }

    public Act getAct() {
        return act;
    }

    public void setAct(Act act) {
        this.act = act;
    }

    public List<ObservationCDA> getObservation() {
        return observation;
    }

    public void setObservation(List<ObservationCDA> observation) {
        this.observation = observation;
    }
}
