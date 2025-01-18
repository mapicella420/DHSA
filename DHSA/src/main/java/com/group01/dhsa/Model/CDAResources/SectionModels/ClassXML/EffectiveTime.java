package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@XmlAccessorType(XmlAccessType.FIELD)
public class EffectiveTime {

    @XmlAttribute(name = "value")
    private String value;

    public EffectiveTime() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");

        this.value = now.atOffset(ZoneOffset.ofHours(1)).format(formatter);
    }

    // Getters e Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}