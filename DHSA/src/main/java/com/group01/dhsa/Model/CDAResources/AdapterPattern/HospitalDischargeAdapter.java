package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.CarePlan;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.MedicationRequest;

import java.util.ArrayList;
import java.util.List;

public class HospitalDischargeAdapter implements CdaSection<Component, Encounter> {

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
                "11535-2",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Diagnosi di Dimissione"
        ));

        section.setTitle(new Title("Condizioni del paziente e diagnosi alla dimissione"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();
        text.setValues(textList);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();

        List<CarePlan> carePlans = FHIRClient.getInstance().getCarePlansForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (carePlans != null && !carePlans.isEmpty()) {
            Paragraph carePlanParagraph = new Paragraph(
                    "The patient is advised to follow the prescribed care plans as follows:"
            );
            textList.add(carePlanParagraph);

            List<ListItem> carePlanItems = new ArrayList<>();
            StructuredList carePlanStructuredList = new StructuredList();
            carePlanStructuredList.setItems(carePlanItems);

            for (CarePlan carePlan : carePlans) {
                StringBuilder carePlanItemContent = new StringBuilder();
                String carePlanDisplay = carePlan.getCategoryFirstRep().getCodingFirstRep()
                        .getDisplay();
                carePlanItemContent.append(carePlanDisplay);
                if (carePlan.getAddressesFirstRep().getConcept() != null) {
                    carePlanItemContent.append(", due to ");
                    carePlanItemContent.append(carePlan.getAddressesFirstRep().getConcept()
                            .getCodingFirstRep().getDisplay()
                    );
                }
                if (carePlan.getPeriod().hasStart()) {
                    carePlanItemContent.append(" (started ");
                    carePlanItemContent.append(carePlan.getPeriod().getStartElement().toHumanDisplay());
                    carePlanItemContent.append(")");
                }
                carePlanItemContent.append(";");
                carePlanItems.add(new ListItem(carePlanItemContent.toString()));
            }
            textList.add(carePlanStructuredList);
        }

        List<MedicationRequest> medicationRequestList = FHIRClient.getInstance()
                .getMedicationRequestForPatientAndEncounter(patientId, encounterId);

        if (medicationRequestList != null && !medicationRequestList.isEmpty()) {
            Paragraph medicationParagraph = new Paragraph(
                    "The indicated medications are to be taken according to the reported dosage:");
            textList.add(medicationParagraph);

            List<ListItem> medicationItems = new ArrayList<>();
            StructuredList medicationStructuredList = new StructuredList();
            medicationStructuredList.setItems(medicationItems);

            for (MedicationRequest medicationRequest : medicationRequestList) {
                StringBuilder medicationContent = new StringBuilder();

                String medicationDisplay = medicationRequest.getMedication().getConcept().getCodingFirstRep().getDisplay();
                medicationContent.append(medicationDisplay);

                medicationRequest.getExtension().stream()
                        .filter(extension -> extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/base-cost"))
                        .findFirst()
                        .ifPresent(extension -> {
                            Double baseCost = extension.getValueQuantity().getValue().doubleValue();
                            medicationContent.append(" (Base cost: $").append(String.format("%.2f", baseCost)).append(")");
                        });

                medicationRequest.getExtension().stream()
                        .filter(extension -> extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/total-cost"))
                        .findFirst()
                        .ifPresent(extension -> {
                            Double totalCost = extension.getValueQuantity().getValue().doubleValue();
                            medicationContent.append(" (Total cost: $").append(String.format("%.2f", totalCost)).append(")");
                        });

                String status = medicationRequest.getStatus().toCode();
                medicationContent.append(". Status: ").append(status).append(".");

                medicationItems.add(new ListItem(medicationContent.toString()));
            }

            textList.add(medicationStructuredList);
        }

        textList.add(new Paragraph("Please ensure you complete any prescribed treatments and seek medical advice if your symptoms worsen."));
        textList.add(new Paragraph("You will be provided with further information regarding any necessary follow-up appointments."));


        return component;
    }
}
