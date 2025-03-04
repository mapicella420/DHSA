package com.group01.dhsa.Model.FhirResources.Level3.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.LoggedUser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceExporter implements FhirResourceExporter {

    private static String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    private static void setFhirServerUrl() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                FHIR_SERVER_URL = "http://localhost:8081/fhir";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                FHIR_SERVER_URL = "http://localhost:8080/fhir";
            }
        }
    }

    @Override
    public List<Map<String, String>> exportResources() {
        setFhirServerUrl();
        List<Map<String, String>> devicesList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Device resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Device.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Device device = (Device) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> deviceData = new HashMap<>();
                deviceData.put("Code", device.getIdentifierFirstRep() != null ? device.getIdentifierFirstRep().getValue() : "N/A");
                deviceData.put("Description", device.getDefinition() != null && device.getDefinition().getConcept() != null
                        ? device.getDefinition().getConcept().getText() : "N/A");

                // Extract UDI (Unique Device Identifier)
                deviceData.put("UDI", device.hasUdiCarrier() ? device.getUdiCarrierFirstRep().getCarrierHRF() : "N/A");

                // Extract Patient reference
                deviceData.put("Patient", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient") != null
                        ? device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient").getValue().toString() : "N/A");

                // Extract Encounter reference
                deviceData.put("Encounter", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter") != null
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
        setFhirServerUrl();
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

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> devicesList = new ArrayList<>();

        try {
            // Initialize the FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform a general search for Device resources
            Bundle bundle = client.search()
                    .forResource(Device.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Device device = (Device) entry.getResource();

                    boolean matches = true;

                    // Apply filtering logic for each search field
                    for (int i = 0; i < searchFields.length; i++) {
                        String searchField = searchFields[i].toLowerCase();
                        String searchValue = searchValues[i];

                        switch (searchField) {
                            case "id":
                                matches = matches && device.getIdElement().getIdPart().equalsIgnoreCase(searchValue);
                                break;
                            case "code":
                                matches = matches && device.hasIdentifier() &&
                                        device.getIdentifierFirstRep().getValue().equalsIgnoreCase(searchValue);
                                break;
                            case "description":
                                matches = matches && device.hasDefinition() &&
                                        device.getDefinition().getConcept().getText().equalsIgnoreCase(searchValue);
                                break;
                            case "udi":
                                matches = matches && device.hasUdiCarrier() &&
                                        device.getUdiCarrierFirstRep().getCarrierHRF().equalsIgnoreCase(searchValue);
                                break;
                            case "patient":
                                matches = matches && hasExtensionReference(device, "http://hl7.org/fhir/StructureDefinition/device-patient", searchValue);
                                break;
                            case "encounter":
                                matches = matches && hasExtensionReference(device, "http://hl7.org/fhir/StructureDefinition/device-encounter", searchValue);
                                break;
                            case "status":
                                matches = matches && device.hasStatus() &&
                                        device.getStatus().toCode().equalsIgnoreCase(searchValue);
                                break;
                            default:
                                System.err.println("[ERROR] Unsupported search field: " + searchField);
                                matches = false;
                        }

                        if (!matches) break; // Stop further checks if one condition is false
                    }

                    // Add matching device to the results
                    if (matches) {
                        devicesList.add(convertResourceToMap(device));
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

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Device)) {
            throw new IllegalArgumentException("Resource is not of type Device");
        }

        Device device = (Device) resource;
        Map<String, String> deviceData = new HashMap<>();

        // Device Identifier (CODE)
        deviceData.put("CODE", device.hasIdentifier() && !device.getIdentifier().isEmpty()
                ? device.getIdentifierFirstRep().getValue()
                : "N/A");

        // Description
        deviceData.put("DESCRIPTION", device.hasDefinition() && device.getDefinition().hasConcept() && device.getDefinition().getConcept().hasText()
                ? device.getDefinition().getConcept().getText()
                : "N/A");

        // UDI (Unique Device Identifier)
        deviceData.put("UDI", device.hasUdiCarrier() && !device.getUdiCarrier().isEmpty()
                ? device.getUdiCarrierFirstRep().getCarrierHRF()
                : "N/A");

        // Patient Reference
        if (device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient") != null) {
            deviceData.put("Patient", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-patient").getValue().toString());
        } else {
            deviceData.put("Patient", "N/A");
        }

        // Encounter Reference
        if (device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter") != null) {
            deviceData.put("Encounter", device.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/device-encounter").getValue().toString());
        } else {
            deviceData.put("Encounter", "N/A");
        }

        // Status
        deviceData.put("STATUS", device.hasStatus() ? device.getStatus().toCode() : "N/A");

        // Manufacturer
        deviceData.put("MANUFACTURER", device.hasManufacturer() ? device.getManufacturer() : "N/A");

        // Model Number
        deviceData.put("MODEL_NUMBER", device.hasModelNumber() ? device.getModelNumber() : "N/A");

        // Serial Number
        deviceData.put("SERIAL_NUMBER", device.hasSerialNumber() ? device.getSerialNumber() : "N/A");

        // Version
        deviceData.put("VERSION", device.hasVersion() && !device.getVersion().isEmpty()
                ? device.getVersionFirstRep().getValue()
                : "N/A");

        // Lot Number
        deviceData.put("LOT_NUMBER", device.hasLotNumber() ? device.getLotNumber() : "N/A");

        // Expiration Date
        deviceData.put("EXPIRATION_DATE", device.hasExpirationDate() ? device.getExpirationDateElement().toHumanDisplay() : "N/A");

        // Manufacture Date
        deviceData.put("MANUFACTURE_DATE", device.hasManufactureDate() ? device.getManufactureDateElement().toHumanDisplay() : "N/A");

        // Owner
        deviceData.put("OWNER", device.hasOwner() ? device.getOwner().getReference() : "N/A");

        // Location
        deviceData.put("LOCATION", device.hasLocation() ? device.getLocation().getReference() : "N/A");

        // Note
        deviceData.put("NOTE", device.hasNote() && !device.getNote().isEmpty()
                ? device.getNoteFirstRep().getText()
                : "N/A");

        return deviceData;
    }

    private boolean hasExtensionReference(Device device, String extensionUrl, String referenceValue) {
        return device.getExtension().stream()
                .filter(extension -> extension.getUrl().equals(extensionUrl))
                .anyMatch(extension -> extension.hasValue() &&
                        extension.getValue() instanceof org.hl7.fhir.r5.model.Reference &&
                        ((org.hl7.fhir.r5.model.Reference) extension.getValue()).getReference().contains(referenceValue));
    }


}
