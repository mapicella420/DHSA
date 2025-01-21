package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@XmlAccessorType(XmlAccessType.FIELD)
public class EffectiveTime {

    @XmlAttribute(name = "value")
    private String value;

    @XmlElement(name = "low")
    private Low low;

    @XmlElement(name = "high")
    private High high;

    public EffectiveTime() {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");

        this.value = now.atOffset(ZoneOffset.ofHours(1)).format(formatter);
    }

    public EffectiveTime(Low low, High high) {
        this.low = low;
        this.high = high;
    }

    // Getters e Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Low getLow() {
        return low;
    }

    public void setLow(Low low) {
        this.low = low;
    }

    public High getHigh() {
        return high;
    }

    public void setHigh(High high) {
        this.high = high;
    }
}