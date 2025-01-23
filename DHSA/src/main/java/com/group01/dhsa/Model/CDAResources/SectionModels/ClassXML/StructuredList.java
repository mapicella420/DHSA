package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuredList {

    @XmlAttribute(name = "type")
    private String type; // "ordered" o "unordered"

    @XmlElement(name = "item")
    private List<ListItem> items;

    public StructuredList() {
    }

    public StructuredList(String type, List<ListItem> items) {
        this.type = type;
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ListItem> getItems() {
        return items;
    }

    public void setItems(List<ListItem> items) {
        this.items = items;
    }
}
