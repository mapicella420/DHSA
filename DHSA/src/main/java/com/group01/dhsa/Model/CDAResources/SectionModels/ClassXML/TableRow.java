package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tr", propOrder = {"cells"})
public class TableRow {

    @XmlElement(name = "td")
    private List<TableCell> cells;

    public TableRow() {}

    public TableRow(List<TableCell> cells) {
        this.cells = cells;
    }

    public List<TableCell> getCells() {
        return cells;
    }

    public void setCells(List<TableCell> cells) {
        this.cells = cells;
    }
}

