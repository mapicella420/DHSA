package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Table {

    @XmlElement(name = "thead")
    private TableRow header;

    @XmlElement(name = "tbody")
    private List<TableRow> rows;

    public Table() {
    }

    public Table(TableRow header, List<TableRow> rows) {
        this.header = header;
        this.rows = rows;
    }

    public TableRow getHeader() {
        return header;
    }

    public void setHeader(TableRow header) {
        this.header = header;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
}
