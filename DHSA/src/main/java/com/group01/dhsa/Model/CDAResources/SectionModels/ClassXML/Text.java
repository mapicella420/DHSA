package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Text {

    @XmlElement(name = "p")
    private List<Paragraph> paragraphs;

    @XmlElement(name = "list")
    private List<StructuredList> lists;

    @XmlElement(name = "table")
    private List<Table> tables;

    public Text() {
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public List<StructuredList> getLists() {
        return lists;
    }

    public void setLists(List<StructuredList> lists) {
        this.lists = lists;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
}
