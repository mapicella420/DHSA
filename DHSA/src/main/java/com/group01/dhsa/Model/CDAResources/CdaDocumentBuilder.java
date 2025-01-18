package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.CdaSection;

public class CdaDocumentBuilder {
    private StringBuilder cdaDocument;

    public CdaDocumentBuilder() {
        cdaDocument = new StringBuilder("<ClinicalDocument xsi:schemaLocation='urn:hl7-org:v3 CDA.xsd' xmlns='urn:hl7-org:v3' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>");
        cdaDocument.append("<typeId extension='POCD_HD000040' root='2.16.840.1.113883.1.3'/>");
        cdaDocument.append("<templateId root='2.16.840.1.113883.2.9.10.1.5'/>");
    }

    public CdaDocumentBuilder addAuthor(String authorId, String authorName) {
        cdaDocument.append("<author>");
        cdaDocument.append("<assignedAuthor>");
        cdaDocument.append("<id extension='").append(authorId).append("'/>");
        cdaDocument.append("<assignedPerson>");
        cdaDocument.append("<name>").append(authorName).append("</name>");
        cdaDocument.append("</assignedPerson>");
        cdaDocument.append("</assignedAuthor>");
        cdaDocument.append("</author>");
        return this;
    }

    public CdaDocumentBuilder addPatient(CdaSection patientAdapter) {
        cdaDocument.append(patientAdapter.toCdaXml());
        return this;
    }

    public CdaDocumentBuilder addObservation(CdaSection observationAdapter) {
        cdaDocument.append("<component>").append(observationAdapter.toCdaXml()).append("</component>");
        return this;
    }

    public String build() {
        cdaDocument.append("</ClinicalDocument>");
        return cdaDocument.toString();
    }
}

