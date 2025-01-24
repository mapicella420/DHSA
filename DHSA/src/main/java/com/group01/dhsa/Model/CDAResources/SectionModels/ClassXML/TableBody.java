package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tbody", propOrder = {"rows"})
public class TableBody {

    @XmlElement(name = "tr")
    private List<TableRow> rows;

    public TableBody() {}

    public TableBody(List<TableRow> rows) {
        this.rows = rows;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
}
