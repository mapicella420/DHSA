package com.group01.dhsa.Model.FhirResources.Level3.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Controller.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizationExporter implements FhirResourceExporter {

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
        List<Map<String, String>> organizationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Organization resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Organization.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Organization organization = (Organization) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> organizationData = new HashMap<>();
                organizationData.put("Organization", organization.getIdElement().getIdPart());
                organizationData.put("Name", organization.getName());

                // Extract address
                Address address = organization.getContactFirstRep() != null ? organization.getContactFirstRep().getAddress() : null;
                if (address != null) {
                    organizationData.put("Address", address.getLine().isEmpty() ? "N/A" :
                            String.join(", ", address.getLine().stream().map(StringType::getValue).toList()));
                    organizationData.put("City", address.getCity() != null ? address.getCity() : "N/A");
                    organizationData.put("State", address.getState() != null ? address.getState() : "N/A");
                    organizationData.put("Zip", address.getPostalCode() != null ? address.getPostalCode() : "N/A");
                } else {
                    organizationData.put("Address", "N/A");
                    organizationData.put("City", "N/A");
                    organizationData.put("State", "N/A");
                    organizationData.put("Zip", "N/A");
                }


                // Extract phone
                String phone = organization.getContactFirstRep() != null && !organization.getContactFirstRep().getTelecom().isEmpty()
                        ? organization.getContactFirstRep().getTelecomFirstRep().getValue()
                        : "N/A";
                organizationData.put("Phone", phone);

                // Extract geolocation extensions
                organizationData.put("Latitude", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat") != null
                        && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                        ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat").getValue()).getValue())
                        : "N/A");

                organizationData.put("Longitude", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon") != null
                        && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                        ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon").getValue()).getValue())
                        : "N/A");

// Extract revenue and utilization extensions
                organizationData.put("Revenue", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue") != null
                        && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                        ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue").getValue()).getValue())
                        : "N/A");

                organizationData.put("Utilization", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization") != null
                        && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                        ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization").getValue()).getValue())
                        : "N/A");

// Add organization data to the list
                organizationsList.add(organizationData);

            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return organizationsList;
    }


    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> organizationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Organization resources
            Bundle bundle = client.search()
                    .forResource(Organization.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Organization organization = (Organization) entry.getResource();

                    // Check if the search term matches any relevant field
                    boolean matches = false;

                    // Match Id
                    if (!matches && organization.getIdElement().getIdPart().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Name
                    if (!matches && organization.hasName() && organization.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Address fields
                    if (!matches && organization.getContactFirstRep() != null) {
                        Address address = organization.getContactFirstRep().getAddress();
                        if (address != null) {
                            if (!matches && address.getCity() != null && address.getCity().toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                            if (!matches && address.getState() != null && address.getState().toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                            if (!matches && address.getPostalCode() != null && address.getPostalCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                            if (!matches && !address.getLine().isEmpty() &&
                                    address.getLine().stream().anyMatch(line -> line.getValue().toLowerCase().contains(searchTerm.toLowerCase()))) {
                                matches = true;
                            }
                        }
                    }

                    // Match Phone
                    if (!matches && organization.getContactFirstRep() != null && !organization.getContactFirstRep().getTelecom().isEmpty()) {
                        String phone = organization.getContactFirstRep().getTelecomFirstRep().getValue();
                        if (phone != null && phone.toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                    }

                    // Match Geolocation fields
                    if (!matches && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat") != null) {
                        var extension = organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat").getValue();
                        if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                            String latitude = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                            if (latitude.toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                        }
                    }

                    if (!matches && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon") != null) {
                        var extension = organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon").getValue();
                        if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                            String longitude = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                            if (longitude.toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                        }
                    }

                    // Match Revenue and Utilization
                    if (!matches && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue") != null) {
                        var extension = organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue").getValue();
                        if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                            String revenue = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                            if (revenue.toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                        }
                    }

                    if (!matches && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization") != null) {
                        var extension = organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization").getValue();
                        if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                            String utilization = String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
                            if (utilization.toLowerCase().contains(searchTerm.toLowerCase())) {
                                matches = true;
                            }
                        }
                    }

                    // Add matching organization to the result list
                    if (matches) {
                        Map<String, String> organizationData = new HashMap<>();
                        organizationData.put("Organization", organization.getIdElement() != null ? organization.getIdElement().getIdPart() : "N/A");
                        organizationData.put("Name", organization.hasName() ? organization.getName() : "N/A");

                        // Extract address
                        Address address = organization.getContactFirstRep() != null ? organization.getContactFirstRep().getAddress() : null;
                        if (address != null) {
                            organizationData.put("Address", address.getLine().isEmpty() ? "N/A" :
                                    String.join(", ", address.getLine().stream().map(StringType::getValue).toList()));
                            organizationData.put("City", address.getCity() != null ? address.getCity() : "N/A");
                            organizationData.put("State", address.getState() != null ? address.getState() : "N/A");
                            organizationData.put("Zip", address.getPostalCode() != null ? address.getPostalCode() : "N/A");
                        } else {
                            organizationData.put("Address", "N/A");
                            organizationData.put("City", "N/A");
                            organizationData.put("State", "N/A");
                            organizationData.put("Zip", "N/A");
                        }

                        // Extract phone
                        String phone = organization.getContactFirstRep() != null && !organization.getContactFirstRep().getTelecom().isEmpty()
                                ? organization.getContactFirstRep().getTelecomFirstRep().getValue()
                                : "N/A";
                        organizationData.put("Phone", phone);

                        // Extract geolocation extensions
                        organizationData.put("Latitude", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat") != null
                                && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                                ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lat").getValue()).getValue())
                                : "N/A");

                        organizationData.put("Longitude", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon") != null
                                && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                                ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/geolocation-lon").getValue()).getValue())
                                : "N/A");

                        // Extract revenue and utilization extensions
                        organizationData.put("Revenue", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue") != null
                                && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                                ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-revenue").getValue()).getValue())
                                : "N/A");

                        organizationData.put("Utilization", organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization") != null
                                && organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization").getValue() instanceof org.hl7.fhir.r5.model.Quantity
                                ? String.valueOf(((org.hl7.fhir.r5.model.Quantity) organization.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/organization-utilization").getValue()).getValue())
                                : "N/A");

                        organizationsList.add(organizationData);
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Organization resources: " + e.getMessage());
            e.printStackTrace();
        }

        return organizationsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> organizationsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the search query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(Organization.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map search fields to FHIR API query parameters
                switch (searchField) {
                    case "organization":
                        query = query.where(new StringClientParam("_id").matches().value(searchValue));
                        break;
                    case "name":
                        query = query.where(new StringClientParam("name").matches().value(searchValue));
                        break;
                    case "identifier":
                        query = query.where(new StringClientParam("identifier").matches().value(searchValue));
                        break;
                    case "address":
                        query = query.where(new StringClientParam("address").matches().value(searchValue));
                        break;
                    case "address-city":
                        query = query.where(new StringClientParam("address-city").matches().value(searchValue));
                        break;
                    case "address-state":
                        query = query.where(new StringClientParam("address-state").matches().value(searchValue));
                        break;
                    case "address-postalcode":
                        query = query.where(new StringClientParam("address-postalcode").matches().value(searchValue));
                        break;
                    default:
                        System.err.println("[ERROR] Unsupported search field: " + searchField);
                }
            }

            // Execute the query and fetch the bundle
            Bundle bundle = query.returnBundle(Bundle.class).execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Organization organization = (Organization) entry.getResource();
                    Map<String, String> organizationData = convertResourceToMap(organization);
                    organizationsList.add(organizationData);
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Organization resources: " + e.getMessage());
            e.printStackTrace();
        }

        return organizationsList;
    }

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Organization)) {
            throw new IllegalArgumentException("Resource is not of type Organization");
        }

        Organization organization = (Organization) resource;
        Map<String, String> organizationData = new HashMap<>();

        // ID
        organizationData.put("Organization", organization.getIdElement().getIdPart());

        // Meta
        if (organization.hasMeta()) {
            organizationData.put("VersionId", organization.getMeta().hasVersionId() ? organization.getMeta().getVersionId() : "N/A");
            organizationData.put("LastUpdated", organization.getMeta().hasLastUpdated() ? organization.getMeta().getLastUpdated().toString() : "N/A");
            organizationData.put("Source", organization.getMeta().hasSource() ? organization.getMeta().getSource() : "N/A");
        } else {
            organizationData.put("VersionId", "N/A");
            organizationData.put("LastUpdated", "N/A");
            organizationData.put("Source", "N/A");
        }

        // Extensions
        organizationData.put("Latitude", getExtensionValue(organization, "http://hl7.org/fhir/StructureDefinition/geolocation-lat"));
        organizationData.put("Longitude", getExtensionValue(organization, "http://hl7.org/fhir/StructureDefinition/geolocation-lon"));
        organizationData.put("Revenue", getExtensionValue(organization, "http://hl7.org/fhir/StructureDefinition/organization-revenue"));
        organizationData.put("Utilization", getExtensionValue(organization, "http://hl7.org/fhir/StructureDefinition/organization-utilization"));

        // Identifier
        organizationData.put("Identifier", organization.hasIdentifier() && !organization.getIdentifier().isEmpty()
                ? organization.getIdentifierFirstRep().getValue() : "N/A");

        // Name
        organizationData.put("Name", organization.hasName() ? organization.getName() : "N/A");

        // Contact (Telecom and Address)
        if (organization.hasContact() && !organization.getContact().isEmpty()) {
            ContactPoint telecom = organization.getContactFirstRep().hasTelecom() ? organization.getContactFirstRep().getTelecomFirstRep() : null;
            Address address = organization.getContactFirstRep().hasAddress() ? organization.getContactFirstRep().getAddress() : null;

            // Telecom
            organizationData.put("Phone", telecom != null && telecom.hasValue() ? telecom.getValue() : "N/A");

            // Address
            if (address != null) {
                organizationData.put("Address", address.hasLine() ? String.join(", ", address.getLine().stream().map(StringType::getValue).toList()) : "N/A");
                organizationData.put("City", address.hasCity() ? address.getCity() : "N/A");
                organizationData.put("State", address.hasState() ? address.getState() : "N/A");
                organizationData.put("PostalCode", address.hasPostalCode() ? address.getPostalCode() : "N/A");
            } else {
                organizationData.put("Address", "N/A");
                organizationData.put("City", "N/A");
                organizationData.put("State", "N/A");
                organizationData.put("PostalCode", "N/A");
            }
        } else {
            organizationData.put("Phone", "N/A");
            organizationData.put("Address", "N/A");
            organizationData.put("City", "N/A");
            organizationData.put("State", "N/A");
            organizationData.put("PostalCode", "N/A");
        }

        return organizationData;
    }

    /**
     * Utility method to extract the value of an extension as a String.
     */
    private String getExtensionValue(Organization organization, String url) {
        if (organization.hasExtension(url)) {
            var extension = organization.getExtensionByUrl(url).getValue();
            if (extension instanceof org.hl7.fhir.r5.model.Quantity) {
                return String.valueOf(((org.hl7.fhir.r5.model.Quantity) extension).getValue());
            }
        }
        return "N/A";
    }


}
