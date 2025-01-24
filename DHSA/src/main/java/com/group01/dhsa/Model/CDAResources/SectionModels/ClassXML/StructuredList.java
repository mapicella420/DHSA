package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuredList {

    @XmlElement(name = "item")
    private List<ListItem> items;

    public StructuredList() {
    }

    public StructuredList(List<ListItem> items) {
        this.items = items;
    }

    public List<ListItem> getItems() {
        return items;
    }

    public void setItems(List<ListItem> items) {
        this.items = items;
    }
}
