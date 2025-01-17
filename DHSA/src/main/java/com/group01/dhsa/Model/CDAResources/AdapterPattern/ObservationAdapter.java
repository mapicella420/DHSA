package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import org.hl7.fhir.r5.model.Observation;

public class ObservationAdapter implements CdaSection {
    private Observation observation;

    public ObservationAdapter(Observation observation) {
        this.observation = observation;
    }

    @Override
    public String toCdaXml() {
        return "<component>" +
                "<observation>" +
                "<code code='" + observation.getCode().getCodingFirstRep().getCode() + "'/>" +
                "<value value='" + observation.getValue().toString() + "'/>" +
                "</observation>" +
                "</component>";
    }
}
