package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.AllergyIntolerance;
import org.hl7.fhir.r5.model.Encounter;

import java.util.ArrayList;
import java.util.List;

public class AllergyAdapter implements CdaSection<Component, Encounter> {
    @Override
    public Component toCdaObject(Encounter fhirObject) {
        Component component = new Component();
        StructuredBody structuredBody = new StructuredBody();
        component.setStructuredBody(structuredBody);
        ComponentInner componentInner = new ComponentInner();
        structuredBody.setComponentInner(componentInner);

        Section section = new Section();
        componentInner.setSection(section);

        section.setCode(new Code(
                "48765-2",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "ALLERGIE E/O REAZIONI AVVERSE"
        ));

        section.setTitle(new Title("Allergie e/o Reazioni Avverse"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();
        text.setValues(textList);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];

        List<AllergyIntolerance> allergies = FHIRClient.getInstance().getAllergiesForPatient(
                patientId
        );

        if (allergies != null && !allergies.isEmpty()) {

            for (AllergyIntolerance allergy : allergies) {
                String allergyDisplay = allergy.getCode().getCodingFirstRep() != null
                        ? allergy.getCode().getCodingFirstRep().getDisplay()
                        : "Unknown allergy";

                String clinicalStatus = allergy.getClinicalStatus() != null &&
                        !allergy.getClinicalStatus().getCoding().isEmpty()
                        ? allergy.getClinicalStatus().getCodingFirstRep().getCode()
                        : "Unknown clinical status";

                String verificationStatus = allergy.getVerificationStatus() != null &&
                        !allergy.getVerificationStatus().getCoding().isEmpty()
                        ? allergy.getVerificationStatus().getCodingFirstRep().getCode()
                        : "Unknown verification status";

                String recordedDate = allergy.getRecordedDate() != null
                        ? allergy.getRecordedDateElement().toHumanDisplay()
                        : "No recorded date available";


                String allergyDetailBuilder = allergyDisplay +
                        " (Clinical status: " + clinicalStatus + ", Verification status: " +
                        verificationStatus + ", Recorded on: " + recordedDate +
                        ").";

                textList.add(new Paragraph(allergyDetailBuilder));
            }

            //Entries are optional
            List<Entry> entryList = new ArrayList<>();

            for (AllergyIntolerance allergy : allergies) {
                Entry entry = new Entry();
                Act act = new Act("ACT", "ENV");
                Code code = new Code();
                code.setNullFlavor("NA");
                act.setCode(code);
                act.setStatusCode(new StatusCode(allergy.getClinicalStatus().getCodingFirstRep().getCode()));

                Low low = new Low();
                if (allergy.getRecordedDateElement() != null) {
                    low.setValue(allergy.getRecordedDateElement().toHumanDisplay());
                }else {
                    low.setNullFlavor("UNK");
                }

                High high = null;
                if (allergy.getClinicalStatus().getCodingFirstRep() != null) {
                    if (allergy.getClinicalStatus().getCodingFirstRep().getCode()
                            .equalsIgnoreCase("inactive") ||
                            allergy.getClinicalStatus().getCodingFirstRep().getCode()
                                    .equalsIgnoreCase("resolved")) {
                            high = new High("");
                    }
                }
                act.setEffectiveTime(new EffectiveTime(low, high));

                EntryRelationship entryRelationship = new EntryRelationship();
                entryRelationship.setTypeCode("SUBJ");
                ObservationCDA observationCDA = new ObservationCDA();
                entryRelationship.setObservation(observationCDA);
                act.setEntryRelationship(entryRelationship);
                observationCDA.setCode(new Code(
                        "52473-6",
                        "2.16.840.1.113883.6.1",
                        "LOINC",
                        "Allergia o causa della reazione"
                ));

                observationCDA.setStatusCode(new StatusCode("completed"));

                observationCDA.setEffectiveTime(new EffectiveTime(low, high));

                Value value = new Value();
                value.setCode(allergy.getCode().getCodingFirstRep().getCode());
                value.setCodeSystem("2.16.840.1.113883.4.642.3.137");
                value.setCodeSystemName("AllergyIntolerance Substance/Product, Condition and Negation Codes");
                value.setDisplayName(allergy.getCode().getCodingFirstRep().getDisplay());

                observationCDA.setValue(value);

                entry.setAct(act);

                entryList.add(entry);
            }
            section.setEntry(entryList);
        }

        if (textList.isEmpty()) {
            return null;
        }

        return component;
    }
}
