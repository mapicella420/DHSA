package com.group01.dhsa.Model.CDAResources.SectionModels;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ClinicalDocument")
public class ClinicalDocument {

    @XmlElement
    private String templateId;

    @XmlElement
    private String title;

    @XmlElement
    private PatientCDA patientSection;  // Singolo oggetto, non lista

    @XmlElement
    private ObservationCDA observationSection;

    // Costruttore, getter e setter

    public ClinicalDocument() {}

    public ClinicalDocument(String templateId, String title) {
        this.templateId = templateId;
        this.title = title;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PatientCDA getPatientSection() {
        return patientSection;
    }

    public void setPatientSection(PatientCDA patientSection) {
        this.patientSection = patientSection;
    }

    public ObservationCDA getObservationSection() {
        return observationSection;
    }

    public void setObservationSection(ObservationCDA observationSection) {
        this.observationSection = observationSection;
    }
}