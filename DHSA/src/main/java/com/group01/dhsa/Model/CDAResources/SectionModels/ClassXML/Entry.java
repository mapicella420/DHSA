package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;


import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Entry {

    @XmlElement(name = "entryRelationship")
    private EntryRelationship entryRelationship;

    @XmlElement(name = "act")
    private Act act;

    //Optional
    @XmlElement(name = "observation")
    private ObservationCDA observation;

    //Optional
    @XmlElement(name = "procedure")
    private ProcedureCDA procedure;

    public Entry() {
    }

    public Entry(ObservationCDA observation) {
        this.observation = observation;
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

    public ObservationCDA getObservation() {
        return observation;
    }

    public void setObservation(ObservationCDA observation) {
        this.observation = observation;
    }

    public ProcedureCDA getProcedure() {
        return procedure;
    }

    public void setProcedure(ProcedureCDA procedureCDA) {
        this.procedure = procedureCDA;
    }
}
