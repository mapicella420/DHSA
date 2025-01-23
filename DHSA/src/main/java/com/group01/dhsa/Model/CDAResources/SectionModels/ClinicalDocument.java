package com.group01.dhsa.Model.CDAResources.SectionModels;

import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "ClinicalDocument")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClinicalDocument {

    // Attributo namespace
    @XmlAttribute(name = "xsi:schemaLocation")
    private String schemaLocation = "urn:hl7-org:v3 CDA.xsd";

    @XmlAttribute(name = "xmlns")
    private String xmlns = "urn:hl7-org:v3";

    @XmlAttribute(name = "xmlns:xsi")
    private String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";


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

    @XmlElement(name = "recordTarget")
    private RecordTarget recordTarget;

    @XmlElement(name = "author")
    private Author author;

    @XmlElement(name = "custodian")
    private Custodian custodian;

    @XmlElement(name = "legalAuthenticator")
    private LegalAuthenticator legalAuthenticator;

    @XmlElement(name = "componentOf")
    private ComponentOf componentOf;

    @XmlElement(name = "component")
    private List<Component> component;


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

    public RecordTarget getRecordTarget() {
        return recordTarget;
    }

    public void setRecordTarget(RecordTarget recordTarget) {
        this.recordTarget = recordTarget;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Custodian getCustodian() {
        return custodian;
    }

    public void setCustodian(Custodian custodian) {
        this.custodian = custodian;
    }

    public LegalAuthenticator getLegalAuthenticator() {
        return legalAuthenticator;
    }

    public void setLegalAuthenticator(LegalAuthenticator legalAuthenticator) {
        this.legalAuthenticator = legalAuthenticator;
    }

    public ComponentOf getComponentOf() {
        return componentOf;
    }

    public void setComponentOf(ComponentOf componentOf) {
        this.componentOf = componentOf;
    }

    public List<Component> getComponent() {
        if (component == null) {
            component = new ArrayList<Component>();
        }
        return component;
    }

    public void setComponent(List<Component> component) {
        this.component = component;
    }
}