package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class LegalAuthenticator {

    @XmlElement(name = "time")
    private String time;

    @XmlElement(name = "signatureCode")
    private SignatureCode signatureCode;

    @XmlElement(name = "assignedEntity")
    private AssignedEntity assignedEntity;

    public LegalAuthenticator() {
    }

    public LegalAuthenticator(String time, SignatureCode signatureCode, AssignedEntity assignedEntity) {
        this.time = time;
        this.signatureCode = signatureCode;
        this.assignedEntity = assignedEntity;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public SignatureCode getSignatureCode() {
        return signatureCode;
    }

    public void setSignatureCode(SignatureCode signatureCode) {
        this.signatureCode = signatureCode;
    }

    public AssignedEntity getAssignedEntity() {
        return assignedEntity;
    }

    public void setAssignedEntity(AssignedEntity assignedEntity) {
        this.assignedEntity = assignedEntity;
    }
}
