package com.group01.dhsa.Model.CDAResources.AdapterPattern.Body;


import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.AdapterPattern.CdaSection;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Device;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.ImagingStudy;

import java.util.ArrayList;
import java.util.List;

public class HospitalDischargeStudiesAdapter implements CdaSection<Component, Encounter> {

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
                "11493-4",
                "2.16.840.1.113883.6.1",
                "LOINC",
                "Hospital discharge studies summary"
        ));

        section.setTitle(new Title("Riscontri ed accertamenti significativi"));

        Text text = new Text();
        section.setText(text);
        List<Object> textList = new ArrayList<>();
        text.setValues(textList);

        String patientId = fhirObject.getSubject().getReference().split("/")[1];
        String encounterId = fhirObject.getIdPart();

        List<ImagingStudy> imagingStudies = FHIRClient.getInstance().getImagingStudiesForPatientAndEncounter(
                patientId,
                encounterId
        );

        if (imagingStudies != null && !imagingStudies.isEmpty()) {
            StructuredList imagingStudyStructuredList = new StructuredList();
            String introImagingStudies = "Summary of key investigations during Hospitalization:";
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

        List<Device> devices = FHIRClient.getInstance().getDeviceByPatientAndEncounter(
                patientId, encounterId
        );

        if (devices != null && !devices.isEmpty()) {
            StructuredList deviceStructuredList = new StructuredList();
            String introDevices = "The findings led to the need for the following devices:";
            textList.add(new Paragraph(introDevices));

            List<ListItem> deviceItems = new ArrayList<>();
            deviceStructuredList.setItems(deviceItems);

            for (Device device : devices) {
                StringBuilder deviceContent = new StringBuilder();

                deviceContent.append("Device ID: ").append(device.getIdPart()).append(". ");
                deviceContent.append("Status: ").append(device.getStatus()).append(". ");

                if (device.hasUdiCarrier() && !device.getUdiCarrier().isEmpty()) {
                    String udi = device.getUdiCarrierFirstRep().getCarrierHRF();
                    deviceContent.append("UDI Carrier: ").append(udi).append(". ");
                }

                if (device.hasDefinition() && device.getDefinition().hasConcept() &&
                        device.getDefinition().getConcept().hasText()) {
                    String deviceDescription = device.getDefinition().getConcept().getText();
                    deviceContent.append("Description: ").append(deviceDescription).append(". ");
                }

                deviceItems.add(new ListItem(deviceContent.toString()));
            }

            textList.add(deviceStructuredList);
        }

        if (textList.isEmpty()) {
            return null;
        }

        return component;
    }
}
