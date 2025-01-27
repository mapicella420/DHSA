package com.group01.dhsa.Model.CDAResources.AdapterPattern.Body;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.CdaSection;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.List;

public class HospitalCourseAdapter implements CdaSection<Component, Encounter> {


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
                "8648-8",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Decorso ospedaliero"
        ));

        section.setTitle(new Title("Decorso Ospedaliero"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();
        text.setValues(textList);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();

        List<Condition> conditions = FHIRClient.getInstance().getConditionsForPatientAndEncounter(
                patientId,
                encounterId
        );


        Patient patient = FHIRClient.getInstance().getPatientFromId(patientId);
        String stringBuilder = "The patient " +  patient.getNameFirstRep().getNameAsSingleString() +
                " came to our attention for " +
                fhirObject.getTypeFirstRep().getCodingFirstRep().getDisplay() +
                ", during the encounter in date " +
                fhirObject.getActualPeriod().getStartElement().toHumanDisplay() +
                ".";
        Paragraph paragraph = new Paragraph();
        paragraph.setContent(stringBuilder);

        textList.add(paragraph);

        if (conditions != null && !conditions.isEmpty()) {
            Paragraph conditionParagraph = new Paragraph();
            StringBuilder conditionsBuilder = new StringBuilder();
            conditionsBuilder.append("The condition was identified as ");
            for (Condition condition : conditions) {
                conditionsBuilder.append(condition.getCode().getCoding().getFirst().getDisplay());
                conditionsBuilder.append(", diagnosed in Encounter ID:");
                conditionsBuilder.append(condition.getIdPart());
                conditionsBuilder.append(". ");
            }
            conditionParagraph.setContent(conditionsBuilder.toString());

            textList.add(conditionParagraph);
        }

        List<Observation> observations = FHIRClient.getInstance().getObservationsForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (observations != null && !observations.isEmpty()) {
            List<StructuredList> structuredLists = new ArrayList<>();
            StructuredList structuredList = new StructuredList();
            structuredLists.add(structuredList);

            Paragraph observationParagraph = new Paragraph(
                    "The following observations were recorded during the encounter:");

            textList.add(observationParagraph);

            List<ListItem> items = new ArrayList<>();
            structuredList.setItems(items);
            for (Observation observation : observations) {
                String obsDetail = observation.getCode().getText();
                String obsValue = formatObservationValue(observation);
                StringBuilder listItemContent = new StringBuilder(obsDetail);
                if (obsValue != null) {
                    listItemContent.append(" (").append(obsValue).append(")");
                }
                items.add(new ListItem(listItemContent.toString()));
            }

            textList.add(structuredList);

        }

        List<CarePlan> carePlans = FHIRClient.getInstance().getCarePlansForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (carePlans != null && !carePlans.isEmpty()) {
            Paragraph carePlanParagraph = new Paragraph(
                    "A care plan was established for the patient, which included:"
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


        List<Procedure> procedures = FHIRClient.getInstance().getProceduresForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (procedures != null && !procedures.isEmpty()) {
            Paragraph procedureParagraph = new Paragraph(
                    "During the hospital stay, the patient underwent the following procedures:");

            textList.add(procedureParagraph);
            List<ListItem> itemsProcedure = new ArrayList<>();
            StructuredList procedureStructuredList = new StructuredList();
            procedureStructuredList.setItems(itemsProcedure);

            for (Procedure procedure : procedures) {
                String procedureDisplay = procedure.getCode().getCodingFirstRep().getDisplay();
                StringBuilder procedureItemContent = new StringBuilder(procedureDisplay);
                if (procedure.getOccurrenceDateTimeType() != null) {
                    String onsetDate = procedure.getOccurrenceDateTimeType().toHumanDisplay();
                    procedureItemContent.append(" (performed at ").append(onsetDate).append("); ");
                } else {
                    procedureItemContent.append("; ");
                }
                itemsProcedure.add(new ListItem(procedureItemContent.toString()));
            }
            textList.add(procedureStructuredList);
        }

        List<Immunization> immunizationList = FHIRClient.getInstance().getImmunizationsForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (immunizationList != null && !immunizationList.isEmpty()) {
            Paragraph immunizationParagraph = new Paragraph(
                    "As part of the patient's care, the following immunizations were performed:");

            textList.add(immunizationParagraph);

            List<ListItem> itemsImmunization = new ArrayList<>();
            StructuredList immunizationStructuredList = new StructuredList();
            immunizationStructuredList.setItems(itemsImmunization);

            for (Immunization immunization: immunizationList) {
                String immunizationDisplay = immunization.getVaccineCode().getCodingFirstRep().getDisplay();
                StringBuilder immunizationItemContent = new StringBuilder(immunizationDisplay);
                if (immunization.getOccurrenceDateTimeType() != null) {
                    String date = immunization.getOccurrenceDateTimeType().toHumanDisplay();
                    immunizationItemContent.append(" (performed at ").append(date).append("); ");
                } else {
                    immunizationItemContent.append(". ");
                }
                itemsImmunization.add(new ListItem(immunizationItemContent.toString()));
            }
            textList.add(immunizationStructuredList);
        }

        List<AllergyIntolerance> allergies = FHIRClient.getInstance().getAllergiesForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (allergies != null && !allergies.isEmpty()) {
            Paragraph allergyParagraph = new Paragraph("The following allergies were identified:");
            textList.add(allergyParagraph);

            List<ListItem> allergyItems = new ArrayList<>();
            StructuredList allergyStructuredList = new StructuredList();
            allergyStructuredList.setItems(allergyItems);

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

                allergyItems.add(new ListItem(allergyDetailBuilder));
            }

            textList.add(allergyStructuredList);
        }

        List<ImagingStudy> imagingStudies = FHIRClient.getInstance().getImagingStudiesForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (imagingStudies != null && !imagingStudies.isEmpty()) {
            StructuredList imagingStudyStructuredList = new StructuredList();
            String introImagingStudies = "As part of the diagnostic process" +
                    ", the following imaging studies were performed:";
            textList.add(new Paragraph(introImagingStudies));


            List<ListItem> imagingItems = new ArrayList<>();
            imagingStudyStructuredList.setItems(imagingItems);

            for (ImagingStudy imagingStudy : imagingStudies) {
                StringBuilder imagingItemContent = new StringBuilder();

                imagingItemContent.append("Study ID: ").append(imagingStudy.getIdPart()).append(". ");

                imagingItemContent.append("Status: ").append(imagingStudy.getStatus()).append(". ");

                if (imagingStudy.getStarted() != null) {
                    imagingItemContent.append("Started on: ")
                            .append(imagingStudy.getStartedElement().toHumanDisplay())
                            .append(". ");
                }

                List<ImagingStudy.ImagingStudySeriesComponent> series = imagingStudy.getSeries();
                if (series != null && !series.isEmpty()) {
                    imagingItemContent.append("The study includes the following series: ");
                    for (ImagingStudy.ImagingStudySeriesComponent seriesComponent : series) {
                        String modality = seriesComponent.getModality().getCodingFirstRep().getDisplay();
                        String bodySite = seriesComponent.getBodySite().getConcept().getCodingFirstRep().getDisplay();

                        imagingItemContent.append(modality.toLowerCase())
                                .append(" scan of the ")
                                .append(bodySite.toLowerCase())
                                .append(". ");


                        List<ImagingStudy.ImagingStudySeriesInstanceComponent> instances = seriesComponent.getInstance();
                        if (instances != null && !instances.isEmpty()) {
                            imagingItemContent.append("Images included: ");
                            for (ImagingStudy.ImagingStudySeriesInstanceComponent instance : instances) {
                                String sopClass = instance.getSopClass().getDisplay();
                                imagingItemContent.append(sopClass.toLowerCase()).append("; ");
                            }
                        }
                    }
                }

                imagingItems.add(new ListItem(imagingItemContent.toString()));
            }
            textList.add(imagingStudyStructuredList);

        }

        List<MedicationRequest> medicationRequestList = FHIRClient.getInstance()
                .getMedicationRequestForPatientAndEncounter(patientId, encounterId);

        if (medicationRequestList != null && !medicationRequestList.isEmpty()) {
            Paragraph medicationParagraph = new Paragraph(
                    "The following medications were prescribed:");
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

        Practitioner practitioner = FHIRClient.getInstance().getPractitionerFromId(fhirObject
                .getParticipantFirstRep().getActor().getReference().split("/")[1]);
        Organization organization = FHIRClient.getInstance().getOrganizationFromId(fhirObject
                .getServiceProvider().getReference().split("/")[1]);

        if (practitioner != null) {
            Paragraph practitionerParagraph = new Paragraph(
                    "The primary practitioner overseeing the patient's care was Dr. "
                            + practitioner.getNameFirstRep().getGivenAsSingleString() + " "
                            + practitioner.getNameFirstRep().getFamily() + "."
            );
            textList.add(practitionerParagraph);
        }

        if (organization != null) {
            Paragraph organizationParagraph = new Paragraph(
                    "The encounter took place at " + organization.getName() + "."
            );
            textList.add(organizationParagraph);
        }


        Paragraph conclusionParagraph = new Paragraph(
                "At the conclusion of the encounter, the patient was in stable condition and was advised to follow up with outpatient care. "
                        + "Specific recommendations included adherence to prescribed medications and scheduled follow-up visits."
        );
        textList.add(conclusionParagraph);


        return component;
    }

    private String formatObservationValue(Observation obs) {
        if (obs.hasValueQuantity()) {
            return obs.getValueQuantity().getValue().toPlainString() + " " +
                    obs.getValueQuantity().getUnit();
        } else if (obs.hasValueCodeableConcept()) {
            return obs.getValueCodeableConcept().getText();
        } else if (obs.hasValueStringType()) {
            return obs.getValueStringType().getValue();
        } else if (obs.hasValueBooleanType()) {
            return Boolean.toString(obs.getValueBooleanType().getValue());
        } else if (obs.hasValueIntegerType()) {
            return Integer.toString(obs.getValueIntegerType().getValue());
        } else if (obs.hasValueRange()) {
            Range range = obs.getValueRange();
            return range.getLow().getValue().toPlainString() + " " + range.getLow().getUnit() +
                    " - " +
                    range.getHigh().getValue().toPlainString() + " " + range.getHigh().getUnit();
        } else if (obs.hasValueRatio()) {
            Ratio ratio = obs.getValueRatio();
            return ratio.getNumerator().getValue().toPlainString() + " " + ratio.getNumerator().getUnit() +
                    " / " +
                    ratio.getDenominator().getValue().toPlainString() + " " + ratio.getDenominator().getUnit();
        } else if (obs.hasValueSampledData()) {
            return "Sampled Data: " + obs.getValueSampledData().getData();
        } else if (obs.hasValueTimeType()) {
            return obs.getValueTimeType().getValue();
        } else if (obs.hasValueDateTimeType()) {
            return obs.getValueDateTimeType().getValue().toString();
        } else if (obs.hasValuePeriod()) {
            Period period = obs.getValuePeriod();
            return "From " + period.getStartElement().toHumanDisplay() +
                    " to " + period.getEndElement().toHumanDisplay();
        } else if (obs.hasValueAttachment()) {
            return "Attachment: " + obs.getValueAttachment().getTitle();
        } else if (obs.hasValueReference()) {
            return "Reference: " + obs.getValueReference().getDisplay();
        } else {
            return null;
        }
    }
}

