package com.group01.dhsa.Model.FhirResources.Level4.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceExporter implements FhirResourceExporter {

    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public List<Map<String, String>> exportResources() {
        List<Map<String, String>> devicesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Device resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Device.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Device device = (Device) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> deviceData = new HashMap<>();
                deviceData.put("CODE", device.getIdentifierFirstRep() != null ? device.getIdentifierFirstRep().getValue() : "N/A");
                deviceData.put("DESCRIPTION", device.getDefinition() != null && device.getDefinition().getConcept() != null
                        ? device.getDefinition().getConcept().getText() : "N/A");

                // Extract UDI (Unique Device Identifier)
                deviceData.put("UDI", device.hasUdiCarrier() ? device.getUdiCarrierFirstRep().getCarrierHRF() : "N/A");

                // Extract Patient reference
                deviceData.put("PATIENT", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient") != null
                        ? device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient").getValue().toString() : "N/A");

                // Extract Encounter reference
                deviceData.put("ENCOUNTER", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter") != null
                        ? device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter").getValue().toString() : "N/A");

                // Extract Status
                deviceData.put("STATUS", device.hasStatus() ? device.getStatus().toCode() : "N/A");

                // Add device data to the list
                devicesList.add(deviceData);
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return devicesList;
    }

    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        List<Map<String, String>> devicesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Device resources
            Bundle bundle = client.search()
                    .forResource(Device.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Device device = (Device) entry.getResource();

                    // Check if the search term matches any relevant field
                    boolean matches = false;

                    // Match Device Identifier (CODE)
                    if (!matches && device.getIdentifierFirstRep() != null &&
                            device.getIdentifierFirstRep().getValue().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Description
                    if (!matches && device.getDefinition() != null && device.getDefinition().getConcept() != null &&
                            device.getDefinition().getConcept().getText() != null &&
                            device.getDefinition().getConcept().getText().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match UDI (Unique Device Identifier)
                    if (!matches && device.hasUdiCarrier() && device.getUdiCarrierFirstRep().getCarrierHRF() != null &&
                            device.getUdiCarrierFirstRep().getCarrierHRF().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Patient reference
                    if (!matches && device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient") != null) {
                        String patientReference = device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient").getValue().toString();
                        if (patientReference.toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                    }

                    // Match Encounter reference
                    if (!matches && device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter") != null) {
                        String encounterReference = device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter").getValue().toString();
                        if (encounterReference.toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                    }

                    // Match Status
                    if (!matches && device.hasStatus() &&
                            device.getStatus().toCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Add matching device to the result list
                    if (matches) {
                        Map<String, String> deviceData = new HashMap<>();

                        // Extract Device Identifier (CODE)
                        deviceData.put("CODE", device.getIdentifierFirstRep() != null ? device.getIdentifierFirstRep().getValue() : "N/A");

                        // Extract Description
                        deviceData.put("DESCRIPTION", device.getDefinition() != null && device.getDefinition().getConcept() != null
                                ? device.getDefinition().getConcept().getText() : "N/A");

                        // Extract UDI (Unique Device Identifier)
                        deviceData.put("UDI", device.hasUdiCarrier() ? device.getUdiCarrierFirstRep().getCarrierHRF() : "N/A");

                        // Extract Patient reference
                        deviceData.put("PATIENT", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient") != null
                                ? device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient").getValue().toString() : "N/A");

                        // Extract Encounter reference
                        deviceData.put("ENCOUNTER", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter") != null
                                ? device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter").getValue().toString() : "N/A");

                        // Extract Status
                        deviceData.put("STATUS", device.hasStatus() ? device.getStatus().toCode() : "N/A");

                        // Add device data to the list
                        devicesList.add(deviceData);
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Device resources: " + e.getMessage());
            e.printStackTrace();
        }

        return devicesList;
    }





}
