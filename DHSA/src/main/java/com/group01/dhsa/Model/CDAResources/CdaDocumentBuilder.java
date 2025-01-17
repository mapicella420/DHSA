package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.CdaSection;

public class CdaDocumentBuilder {
    private StringBuilder cdaDocument;

    public CdaDocumentBuilder() {
        cdaDocument = new StringBuilder("<ClinicalDocument xmlns='urn:hl7-org:v3'>");
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

