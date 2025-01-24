package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcedureCDA {

    @XmlElement(name = "code")
    private Code code;

    @XmlElement(name = "statusCode")
    private StatusCode statusCode;

    @XmlElement(name = "effectiveTime")
    private EffectiveTime effectiveTime;

    public ProcedureCDA() {
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public EffectiveTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(EffectiveTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
}
