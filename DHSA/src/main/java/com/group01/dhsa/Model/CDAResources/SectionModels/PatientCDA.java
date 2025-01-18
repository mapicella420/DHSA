package com.group01.dhsa.Model.CDAResources.SectionModels;

import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "patient")
public class PatientCDA {

    @XmlElement(name = "name")
    private Name name;

    @XmlElement(name = "administrativeGenderCode")
    private AdministrativeGenderCode administrativeGenderCode;

    @XmlElement(name = "birthTime")
    private BirthTime birthTime;

    // Getter e Setter
    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public AdministrativeGenderCode getAdministrativeGenderCode() {
        return administrativeGenderCode;
    }

    public void setAdministrativeGenderCode(AdministrativeGenderCode administrativeGenderCode) {
        this.administrativeGenderCode = administrativeGenderCode;
    }

    public BirthTime getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(BirthTime birthTime) {
        this.birthTime = birthTime;
    }
}

