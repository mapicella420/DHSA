package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "thead", propOrder = {"rows"})
public class TableHead {

    @XmlElement(name = "tr")
    private List<TableRow> rows;

    public TableHead() {}

    public TableHead(List<TableRow> rows) {
        this.rows = rows;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
}
