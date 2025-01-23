package com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Text {

    @XmlElements({
            @XmlElement(name = "paragraph", type = Paragraph.class),
            @XmlElement(name = "list", type = StructuredList.class),
            @XmlElement(name = "table", type = Table.class)
    })
    private List<?> values;

    public Text() {
    }

    public List<?> getValues() {
        return values;
    }

    public void setValues(List<?> values) {
        this.values = values;
    }

}
