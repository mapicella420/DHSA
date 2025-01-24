package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "table", propOrder = {"thead", "tbody"})
public class Table {

    @XmlElement(name = "thead")
    private TableHead thead;

    @XmlElement(name = "tbody")
    private TableBody tbody;

    public Table() {}

    public Table(TableHead thead, TableBody tbody) {
        this.thead = thead;
        this.tbody = tbody;
    }

    public TableHead getThead() {
        return thead;
    }

    public void setThead(TableHead thead) {
        this.thead = thead;
    }

    public TableBody getTbody() {
        return tbody;
    }

    public void setTbody(TableBody tbody) {
        this.tbody = tbody;
    }
}
