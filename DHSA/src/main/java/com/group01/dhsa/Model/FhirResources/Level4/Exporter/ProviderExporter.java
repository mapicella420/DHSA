package com.group01.dhsa.Model.FhirResources.Level4.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProviderExporter implements FhirResourceExporter {

    private static String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    private static void setFhirServerUrl() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")) {
                FHIR_SERVER_URL = "http://localhost:8081/fhir";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")) {
                FHIR_SERVER_URL = "http://localhost:8080/fhir";
            }
        }
    }

    @Override
    public List<Map<String, String>> exportResources() {
        setFhirServerUrl();
        List<Map<String, String>> providersList = new ArrayList<>();

        try {
            // Initialize the FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Practitioner resources
            Bundle practitionerBundle = client.search()
                    .forResource(Practitioner.class)
                    .returnBundle(Bundle.class)
                    .count(1000)
                    .execute();

            // Iterate over Practitioner resources
            for (Bundle.BundleEntryComponent entry : practitionerBundle.getEntry()) {
                Practitioner practitioner = (Practitioner) entry.getResource();

                // Map to store provider data
                Map<String, String> providerData = new HashMap<>();

                // Extract Practitioner ID
                providerData.put("Practitioner", practitioner.getIdElement().getIdPart());

                // Extract Identifier
                providerData.put("Identifier", practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty() ?
                        practitioner.getIdentifierFirstRep().getValue() : "N/A");

                // Extract Name
                if (practitioner.hasName() && !practitioner.getName().isEmpty()) {
                    HumanName name = practitioner.getNameFirstRep();
                    providerData.put("Name", String.join(" ", name.getGivenAsSingleString(), name.getFamily()));
                } else {
                    providerData.put("Name", "N/A");
                }

                // Extract Gender
                providerData.put("Gender", practitioner.hasGender() ? practitioner.getGender().toCode() : "N/A");

                // Extract Address
                if (practitioner.hasAddress() && !practitioner.getAddress().isEmpty()) {
                    Address address = practitioner.getAddressFirstRep();
                    providerData.put("Address", address.getLine().isEmpty() ? "N/A" :
                            String.join(", ", address.getLine().stream().map(StringType::getValue).toList()));
                    providerData.put("City", address.hasCity() ? address.getCity() : "N/A");
                    providerData.put("State", address.hasState() ? address.getState() : "N/A");
                    providerData.put("Zip", address.hasPostalCode() ? address.getPostalCode() : "N/A");
                } else {
                    providerData.put("Address", "N/A");
                    providerData.put("City", "N/A");
                    providerData.put("State", "N/A");
                    providerData.put("Zip", "N/A");
                }

                // Extract Phone
                if (practitioner.hasTelecom() && !practitioner.getTelecom().isEmpty()) {
                    providerData.put("Phone", practitioner.getTelecomFirstRep().getValue());
                } else {
                    providerData.put("Phone", "N/A");
                }

                // Extract PractitionerRole for the Practitioner
                Bundle roleBundle = client.search()
                        .forResource(PractitionerRole.class)
                        .where(PractitionerRole.PRACTITIONER.hasId(practitioner.getIdElement().getIdPart()))
                        .returnBundle(Bundle.class)
                        .execute();

                for (Bundle.BundleEntryComponent roleEntry : roleBundle.getEntry()) {
                    PractitionerRole role = (PractitionerRole) roleEntry.getResource();

                    // Extract Organization
                    Reference organizationRef = role.getOrganization();
                    providerData.put("Organization", organizationRef != null ? organizationRef.getReference() : "N/A");

                    // Extract Specialty
                    if (!role.getSpecialty().isEmpty()) {
                        providerData.put("Specialty", role.getSpecialtyFirstRep().getCodingFirstRep().getDisplay());
                    } else {
                        providerData.put("Specialty", "N/A");
                    }

                    // Extract Utilization
                    Extension utilizationExtension = role.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/utilization");
                    providerData.put("Utilization", utilizationExtension != null ? utilizationExtension.getValue().toString() : "N/A");
                }

                // Add provider data to the list
                providersList.add(providerData);
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return providersList;
    }


    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Initialize the FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Practitioner resources
            Bundle bundle = client.search()
                    .forResource(Practitioner.class)
                    .where(new StringClientParam("name").matches().value(searchTerm))
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all results
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Practitioner practitioner = (Practitioner) entry.getResource();
                Map<String, String> providerData = new HashMap<>();

                // Extract Practitioner data
                providerData.put("Practitioner", practitioner.getIdElement().getIdPart());
                providerData.put("Identifier", practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty() ?
                        practitioner.getIdentifierFirstRep().getValue() : "N/A");
                providerData.put("Name", practitioner.hasName() && !practitioner.getName().isEmpty() ?
                        practitioner.getNameFirstRep().getNameAsSingleString() : "N/A");
                providerData.put("Gender", practitioner.hasGender() ? practitioner.getGender().toCode() : "N/A");

                // Add to results
                results.add(providerData);
            }
        } catch (Exception e) {
            System.err.println("Error searching Practitioner resources: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Cerca risorse in base a più campi e valori.
     *
     * @param searchFields Un array di campi per la ricerca.
     * @param searchValues Un array di valori corrispondenti.
     * @return Una lista di mappe contenenti i dati delle risorse corrispondenti.
     */
    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Initialize the FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the search query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(Practitioner.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map search fields to FHIR API query parameters
                switch (searchField) {
                    case "practitioner":
                        query = query.where(new StringClientParam("_id").matches().value(searchValue));
                        break;
                    case "identifier":
                        query = query.where(new StringClientParam("identifier").matches().value(searchValue));
                        break;
                    case "name":
                        query = query.where(new StringClientParam("name").matches().value(searchValue));
                        break;
                    case "gender":
                        query = query.where(new StringClientParam("gender").matches().value(searchValue));
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
                    if (entry.getResource() instanceof Practitioner practitioner) {
                        results.add(extractPractitionerData(practitioner));
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Practitioner resources: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Extracts data from a Practitioner resource and maps it to a key-value structure.
     */
    private Map<String, String> extractPractitionerData(Practitioner practitioner) {
        Map<String, String> providerData = new HashMap<>();

        // Extract Practitioner data
        providerData.put("Practitioner", practitioner.getIdElement().getIdPart());

        // Meta
        if (practitioner.hasMeta()) {
            providerData.put("VersionId", practitioner.getMeta().hasVersionId() ? practitioner.getMeta().getVersionId() : "N/A");
            providerData.put("LastUpdated", practitioner.getMeta().hasLastUpdated() ? practitioner.getMeta().getLastUpdated().toString() : "N/A");
            providerData.put("Source", practitioner.getMeta().hasSource() ? practitioner.getMeta().getSource() : "N/A");
        } else {
            providerData.put("VersionId", "N/A");
            providerData.put("LastUpdated", "N/A");
            providerData.put("Source", "N/A");
        }

        // Identifier
        if (practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty()) {
            String allIdentifiers = practitioner.getIdentifier().stream()
                    .map(Identifier::getValue)
                    .collect(Collectors.joining(", "));
            providerData.put("Identifier", allIdentifiers);
        } else {
            providerData.put("Identifier", "N/A");
        }

        // Name
        if (practitioner.hasName() && !practitioner.getName().isEmpty()) {
            HumanName name = practitioner.getNameFirstRep();
            providerData.put("Name", String.join(" ", name.getGivenAsSingleString(), name.getFamily()));
        } else {
            providerData.put("Name", "N/A");
        }

        // Gender
        providerData.put("Gender", practitioner.hasGender() ? practitioner.getGender().toCode() : "N/A");

        // Address
        if (practitioner.hasAddress() && !practitioner.getAddress().isEmpty()) {
            Address address = practitioner.getAddressFirstRep();
            providerData.put("Address", address.hasLine() ? String.join(", ", address.getLine().stream().map(StringType::getValue).toList()) : "N/A");
            providerData.put("City", address.hasCity() ? address.getCity() : "N/A");
            providerData.put("State", address.hasState() ? address.getState() : "N/A");
            providerData.put("PostalCode", address.hasPostalCode() ? address.getPostalCode() : "N/A");
        } else {
            providerData.put("Address", "N/A");
            providerData.put("City", "N/A");
            providerData.put("State", "N/A");
            providerData.put("PostalCode", "N/A");
        }

        return providerData;
    }

    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Practitioner)) {
            throw new IllegalArgumentException("Resource is not of type Practitioner");
        }

        Practitioner practitioner = (Practitioner) resource;
        Map<String, String> providerData = new HashMap<>();

        // ID
        providerData.put("Practitioner", practitioner.getIdElement().getIdPart());

        // Meta
        if (practitioner.hasMeta()) {
            providerData.put("VersionId", practitioner.getMeta().hasVersionId() ? practitioner.getMeta().getVersionId() : "N/A");
            providerData.put("LastUpdated", practitioner.getMeta().hasLastUpdated() ? practitioner.getMeta().getLastUpdated().toString() : "N/A");
            providerData.put("Source", practitioner.getMeta().hasSource() ? practitioner.getMeta().getSource() : "N/A");
        } else {
            providerData.put("VersionId", "N/A");
            providerData.put("LastUpdated", "N/A");
            providerData.put("Source", "N/A");
        }

        // Identifier
        providerData.put("Identifier", practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty() ?
                practitioner.getIdentifierFirstRep().getValue() : "N/A");

        // Name
        if (practitioner.hasName() && !practitioner.getName().isEmpty()) {
            HumanName name = practitioner.getNameFirstRep();
            providerData.put("Name", String.join(" ", name.getGivenAsSingleString(), name.getFamily()));
        } else {
            providerData.put("Name", "N/A");
        }

        // Gender
        providerData.put("Gender", practitioner.hasGender() ? practitioner.getGender().toCode() : "N/A");

        // Address
        if (practitioner.hasAddress() && !practitioner.getAddress().isEmpty()) {
            Address address = practitioner.getAddressFirstRep();
            providerData.put("Address", address.hasLine() ? String.join(", ", address.getLine().stream().map(StringType::getValue).toList()) : "N/A");
            providerData.put("City", address.hasCity() ? address.getCity() : "N/A");
            providerData.put("State", address.hasState() ? address.getState() : "N/A");
            providerData.put("Zip", address.hasPostalCode() ? address.getPostalCode() : "N/A");
        } else {
            providerData.put("Address", "N/A");
            providerData.put("City", "N/A");
            providerData.put("State", "N/A");
            providerData.put("Zip", "N/A");
        }

        // Return the map
        return providerData;
    }


}
