package com.group01.dhsa.Model.FhirResources.Level4.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import com.group01.dhsa.Model.LoggedUser;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    .execute();

            // Iterate over Practitioner resources
            for (Bundle.BundleEntryComponent entry : practitionerBundle.getEntry()) {
                Practitioner practitioner = (Practitioner) entry.getResource();

                // Map to store provider data
                Map<String, String> providerData = new HashMap<>();

                // Extract Practitioner ID
                providerData.put("Id", practitioner.getIdElement().getIdPart());

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
                providerData.put("Id", practitioner.getIdElement().getIdPart());
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

    @Override
    public List<Map<String, String>> searchResources(String searchField, String searchValue) {
        setFhirServerUrl();
        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Initialize the FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Adatta il nome del campo per l'API FHIR
            if ("Id".equalsIgnoreCase(searchField)) {
                searchField = "_id";
            } else if ("Name".equalsIgnoreCase(searchField)) {
                searchField = "name";
            } else if ("Identifier".equalsIgnoreCase(searchField)) {
                searchField = "identifier";
            } else if ("Gender".equalsIgnoreCase(searchField)) {
                searchField = "gender";
            } else if ("City".equalsIgnoreCase(searchField)) {
                searchField = "address-city";
            } else if ("State".equalsIgnoreCase(searchField)) {
                searchField = "address-state";
            } else if ("Zip".equalsIgnoreCase(searchField)) {
                searchField = "address-postalcode";
            } else {
                throw new IllegalArgumentException("Invalid search field: " + searchField);
            }

            // Perform the search query
            Bundle bundle = client.search()
                    .forResource(Practitioner.class)
                    .where(new StringClientParam(searchField).matches().value(searchValue))
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Practitioner practitioner = (Practitioner) entry.getResource();
                    Map<String, String> providerData = new HashMap<>();

                    // Extract Practitioner data
                    providerData.put("Id", practitioner.getIdElement().getIdPart());
                    providerData.put("Identifier", practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty() ?
                            practitioner.getIdentifierFirstRep().getValue() : "N/A");
                    providerData.put("Name", practitioner.hasName() && !practitioner.getName().isEmpty() ?
                            practitioner.getNameFirstRep().getNameAsSingleString() : "N/A");
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

                    // Add to results
                    results.add(providerData);
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error searching Practitioner resources: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

}
