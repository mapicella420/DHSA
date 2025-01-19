package com.group01.dhsa.Model.CDAResources.SectionModels;

import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "ClinicalDocument")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClinicalDocument {

    // Attributo namespace
    @XmlAttribute(name = "xsi:schemaLocation", namespace = "http://www.w3.org/2001/XMLSchema-instance")
    private String schemaLocation = "urn:hl7-org:v3 CDA.xsd";

    @XmlAttribute(name = "xmlns")
    private String xmlns = "urn:hl7-org:v3";

    @XmlAttribute(name = "xmlns:xsi")
    private String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";

    // Elementi XML
    @XmlElement(name = "typeId")
    private TypeId typeId;

    @XmlElement(name = "templateId")
    private TemplateId templateId;

    @XmlElement(name = "id")
    private Id id;

    @XmlElement(name = "realmCode")
    private RealmCode realmCode;

    @XmlElement(name = "code")
    private Code code;

    @XmlElement(name = "title")
    private Title title;

    @XmlElement(name = "effectiveTime")
    private EffectiveTime effectiveTime;

    @XmlElement(name = "confidentialityCode")
    private ConfidentialityCode confidentialityCode;

    @XmlElement(name = "languageCode")
    private LanguageCode languageCode;

    @XmlElement(name = "setId")
    private SetId setId;

    @XmlElement(name = "versionNumber")
    private VersionNumber versionNumber;

    public ClinicalDocument() {
    }

    public ClinicalDocument(Integer idNumber) {
        this.typeId = new TypeId("POCD_HD000040", "2.16.840.1.113883.1.3");
        this.templateId = new TemplateId("2.16.840.1.113883.2.9.10.1.5","1.2");
        this.realmCode = new RealmCode("IT");
        this.id = new Id(idNumber.toString(),"2.16.840.1.113883.2.9.2.150","Regione Campania");
        this.code = new Code("34105-7","2.16.840.1.113883.6.1", "LOINC", "Lettera di dimissione ospedaliera");
        this.title = new Title("LETTERA DI DIMISSIONE OSPEDALIERA");
        this.effectiveTime = new EffectiveTime();
        this.confidentialityCode = new ConfidentialityCode("V", "2.16.840.1.113883.5.25", "HL7 Confidentiality", "Very restricted");
        this.languageCode = new LanguageCode("it-IT");
        this.setId = new SetId(idNumber.toString(), "2.16.840.1.113883.2.9.2.150", "Regione Campania");
        this.versionNumber = new VersionNumber(1);
    }

//    @XmlElement(name = "recordTarget")
//    private RecordTarget recordTarget;

//    @XmlElement(name = "author")
//    private String author;

    //Getter e Setter
    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getXmlnsXsi() {
        return xmlnsXsi;
    }

    public void setXmlnsXsi(String xmlnsXsi) {
        this.xmlnsXsi = xmlnsXsi;
    }

    public TypeId getTypeId() {
        return typeId;
    }

    public void setTypeId(TypeId typeId) {
        this.typeId = typeId;
    }

    public TemplateId getTemplateId() {
        return templateId;
    }

    public void setTemplateId(TemplateId templateId) {
        this.templateId = templateId;
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public RealmCode getRealmCode() {
        return realmCode;
    }

    public void setRealmCode(RealmCode realmCode) {
        this.realmCode = realmCode;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public EffectiveTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(EffectiveTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public ConfidentialityCode getConfidentialityCode() {
        return confidentialityCode;
    }

    public void setConfidentialityCode(ConfidentialityCode confidentialityCode) {
        this.confidentialityCode = confidentialityCode;
    }

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(LanguageCode languageCode) {
        this.languageCode = languageCode;
    }

    public SetId getSetId() {
        return setId;
    }

    public void setSetId(SetId setId) {
        this.setId = setId;
    }

    public VersionNumber getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(VersionNumber versionNumber) {
        this.versionNumber = versionNumber;
    }
}

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "ClinicalDocument")
//public class ClinicalDocument {
//
//    @XmlElement
//    private String templateId;
//
//    @XmlElement
//    private String title;
//
//    @XmlElement
//    private PatientCDA patientSection;  // Singolo oggetto, non lista
//
//    @XmlElement
//    private ObservationCDA observationSection;
//
//    // Costruttore, getter e setter
//
//    public ClinicalDocument() {}
//
//    public ClinicalDocument(String templateId, String title) {
//        this.templateId = templateId;
//        this.title = title;
//    }
//
//    public String getTemplateId() {
//        return templateId;
//    }
//
//    public void setTemplateId(String templateId) {
//        this.templateId = templateId;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public PatientCDA getPatientSection() {
//        return patientSection;
//    }
//
//    public void setPatientSection(PatientCDA patientSection) {
//        this.patientSection = patientSection;
//    }
//
//    public ObservationCDA getObservationSection() {
//        return observationSection;
//    }
//
//    public void setObservationSection(ObservationCDA observationSection) {
//        this.observationSection = observationSection;
//    }
//}