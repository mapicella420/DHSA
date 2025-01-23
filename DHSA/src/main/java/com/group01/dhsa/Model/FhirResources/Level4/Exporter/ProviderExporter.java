package com.group01.dhsa.Model.FhirResources.Level4.Exporter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import com.group01.dhsa.Model.FhirResources.FhirResourceExporter;
import org.hl7.fhir.r5.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderExporter implements FhirResourceExporter {

    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public List<Map<String, String>> exportResources() {
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
                providerData.put("Id", practitioner.getIdentifierFirstRep() != null ? practitioner.getIdentifierFirstRep().getValue() : "N/A");

                // Extract Name
                HumanName name = practitioner.getNameFirstRep();
                if (name != null) {
                    providerData.put("Name", name.getGivenAsSingleString() + " " + name.getFamily());
                } else {
                    providerData.put("Name", "N/A");
                }

                // Extract Gender
                providerData.put("Gender", practitioner.hasGender() ? practitioner.getGender().toCode() : "N/A");

                // Extract Address
                Address address = practitioner.getAddressFirstRep();
                if (address != null) {
                    providerData.put("Address", address.getLine().isEmpty() ? "N/A" :
                            String.join(", ", address.getLine().stream().map(StringType::getValue).toList()));
                    providerData.put("City", address.getCity() != null ? address.getCity() : "N/A");
                    providerData.put("State", address.getState() != null ? address.getState() : "N/A");
                    providerData.put("Zip", address.getPostalCode() != null ? address.getPostalCode() : "N/A");
                } else {
                    providerData.put("Address", "N/A");
                    providerData.put("City", "N/A");
                    providerData.put("State", "N/A");
                    providerData.put("Zip", "N/A");
                }


                // Extract Phone
                ContactPoint phone = practitioner.getTelecomFirstRep();
                providerData.put("Phone", phone != null ? phone.getValue() : "N/A");

                // Retrieve PractitionerRole for the Practitioner
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
        try {
            // Inizializza il client FHIR
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Esegui la ricerca basata sul nome del provider
            Bundle bundle = client.search()
                    .forResource("Practitioner")
                    .where(new StringClientParam("name").matches().value(searchTerm))
                    .returnBundle(Bundle.class)
                    .execute();

            // Converti i risultati in una lista di mappe
            List<Map<String, String>> results = new ArrayList<>();
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Practitioner practitioner = (Practitioner) entry.getResource();
                Map<String, String> resourceData = new HashMap<>();

                // Aggiungi i dettagli del provider
                resourceData.put("Id", practitioner.getIdElement().getIdPart());
                resourceData.put("Name", practitioner.getNameFirstRep().getNameAsSingleString());
                resourceData.put("Gender", practitioner.hasGender() ? practitioner.getGender().toCode() : "N/A");

                // Specialità (se disponibile)
                if (practitioner.hasQualification() && !practitioner.getQualification().isEmpty()) {
                    resourceData.put("Specialty", practitioner.getQualificationFirstRep().getCode().getText());
                } else {
                    resourceData.put("Specialty", "N/A");
                }

                // Aggiungi i dettagli del provider alla lista dei risultati
                results.add(resourceData);
            }

            return results;
        } catch (Exception e) {
            System.err.println("Error searching Provider resources: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
