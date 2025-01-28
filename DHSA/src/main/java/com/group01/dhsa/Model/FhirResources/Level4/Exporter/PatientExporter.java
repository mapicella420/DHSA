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

public class PatientExporter implements FhirResourceExporter {

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
        List<Map<String, String>> patientsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Retrieve all Patient resources from the FHIR server
            Bundle bundle = client.search()
                    .forResource(Patient.class)
                    .returnBundle(Bundle.class)
                    .count(1000)

                    .execute();

            // Iterate over the bundle entries
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Patient patient = (Patient) entry.getResource();

                // Extract relevant fields and store them in a Map
                Map<String, String> patientData = new HashMap<>();
                patientData.put("Identifier", patient.getIdentifierFirstRep() != null ? patient.getIdentifierFirstRep().getValue() : "N/A");
                patientData.put("Patient", patient.getIdPart() != null ? patient.getIdPart() : "N/A");

                // Name
                HumanName name = patient.getNameFirstRep();
                if (name != null) {
                    patientData.put("Name", name.getGivenAsSingleString() + " " + name.getFamily());
                } else {
                    patientData.put("Name", "N/A");
                }

                // Gender
                patientData.put("Gender", patient.hasGender() ? patient.getGender().toCode() : "N/A");

                // Birth Date
                patientData.put("BirthDate", patient.hasBirthDate() ? patient.getBirthDate().toString() : "N/A");

                // Deceased Date
                if (patient.hasDeceasedDateTimeType()) {
                    patientData.put("DeceasedDate", patient.getDeceasedDateTimeType().getValueAsString());
                } else {
                    patientData.put("DeceasedDate", patient.hasDeceasedBooleanType() && patient.getDeceasedBooleanType().booleanValue() ? "Deceased" : "Alive");
                }

                // Address
                Address address = patient.getAddressFirstRep();
                if (address != null) {
                    // Converte la lista di StringType in una lista di String per concatenarla
                    if (!address.getLine().isEmpty()) {
                        patientData.put("Address", String.join(", ",
                                address.getLine().stream().map(StringType::getValue).toList()));
                    } else {
                        patientData.put("Address", "N/A");
                    }

                    // Aggiunge le altre informazioni sull'indirizzo
                    patientData.put("City", address.getCity() != null ? address.getCity() : "N/A");
                    patientData.put("State", address.getState() != null ? address.getState() : "N/A");
                    patientData.put("Zip", address.getPostalCode() != null ? address.getPostalCode() : "N/A");
                } else {
                    // Valori di default se l'indirizzo non è presente
                    patientData.put("Address", "N/A");
                    patientData.put("City", "N/A");
                    patientData.put("State", "N/A");
                    patientData.put("Zip", "N/A");
                }


                // Race
                Extension raceExtension = patient.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/us-core-race");
                patientData.put("Race", raceExtension != null && raceExtension.hasValue() ? raceExtension.getValue().toString() : "N/A");

                // Ethnicity
                Extension ethnicityExtension = patient.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity");
                patientData.put("Ethnicity", ethnicityExtension != null && ethnicityExtension.hasValue() ? ethnicityExtension.getValue().toString() : "N/A");

                // Add patient data to the list
                patientsList.add(patientData);
            }
        } catch (Exception e) {
            System.err.println("Error during resource export: " + e.getMessage());
            e.printStackTrace();
        }

        return patientsList;
    }


    @Override
    public List<Map<String, String>> searchResources(String searchTerm) {
        setFhirServerUrl();
        List<Map<String, String>> patientsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Perform search query for Patient resources
            Bundle bundle = client.search()
                    .forResource(Patient.class)
                    .returnBundle(Bundle.class)
                    .execute();

            // Process all pages of the bundle
            while (bundle != null) {
                for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                    Patient patient = (Patient) entry.getResource();

                    // Check if the search term matches any relevant field
                    boolean matches = false;

                    // Match Id
                    if (!matches && patient.getIdPart().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }
                    if (!matches && patient.getIdElement().getIdPart().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Name
                    if (!matches && patient.hasName() && patient.getNameFirstRep().getGivenAsSingleString() != null &&
                            patient.getNameFirstRep().getGivenAsSingleString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }
                    if (!matches && patient.hasName() && patient.getNameFirstRep().hasFamily() &&
                            patient.getNameFirstRep().getFamily().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }


                    // Match Gender
                    if (!matches && patient.hasGender() && patient.getGender().toCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Birth Date
                    if (!matches && patient.hasBirthDate() && patient.getBirthDate().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Deceased Date
                    if (!matches && patient.hasDeceasedDateTimeType() &&
                            patient.getDeceasedDateTimeType().getValueAsString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }
                    if (!matches && patient.hasDeceasedBooleanType() &&
                            (patient.getDeceasedBooleanType().booleanValue() ? "deceased" : "alive").toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Address fields
                    Address address = patient.getAddressFirstRep();
                    if (address != null) {
                        if (!matches && !address.getLine().isEmpty() &&
                                address.getLine().stream().anyMatch(line -> line.getValue().toLowerCase().contains(searchTerm.toLowerCase()))) {
                            matches = true;
                        }
                        if (!matches && address.getCity() != null && address.getCity().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                        if (!matches && address.getState() != null && address.getState().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                        if (!matches && address.getPostalCode() != null && address.getPostalCode().toLowerCase().contains(searchTerm.toLowerCase())) {
                            matches = true;
                        }
                    }

                    // Match Race
                    Extension raceExtension = patient.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/us-core-race");
                    if (!matches && raceExtension != null && raceExtension.hasValue() &&
                            raceExtension.getValue().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Match Ethnicity
                    Extension ethnicityExtension = patient.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity");
                    if (!matches && ethnicityExtension != null && ethnicityExtension.hasValue() &&
                            ethnicityExtension.getValue().toString().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matches = true;
                    }

                    // Add matching patient to the result list
                    if (matches) {
                        Map<String, String> patientData = new HashMap<>();

                        patientData.put("Identifier", patient.getIdentifierFirstRep() != null ? patient.getIdentifierFirstRep().getValue() : "N/A");
                        patientData.put("Patient", patient.getIdPart() != null ? patient.getIdPart() : "N/A");

                        HumanName name = patient.getNameFirstRep();
                        if (name != null) {
                            patientData.put("Name", name.getGivenAsSingleString() + " " + name.getFamily());
                        } else {
                            patientData.put("Name", "N/A");
                        }

                        patientData.put("Gender", patient.hasGender() ? patient.getGender().toCode() : "N/A");
                        patientData.put("BirthDate", patient.hasBirthDate() ? patient.getBirthDate().toString() : "N/A");

                        if (patient.hasDeceasedDateTimeType()) {
                            patientData.put("DeceasedDate", patient.getDeceasedDateTimeType().getValueAsString());
                        } else {
                            patientData.put("DeceasedDate", patient.hasDeceasedBooleanType() && patient.getDeceasedBooleanType().booleanValue() ? "Deceased" : "Alive");
                        }

                        Address addr = patient.getAddressFirstRep();
                        if (addr != null) {
                            if (!addr.getLine().isEmpty()) {
                                patientData.put("Address", String.join(", ",
                                        addr.getLine().stream().map(StringType::getValue).toList()));
                            } else {
                                patientData.put("Address", "N/A");
                            }

                            patientData.put("City", addr.getCity() != null ? addr.getCity() : "N/A");
                            patientData.put("State", addr.getState() != null ? addr.getState() : "N/A");
                            patientData.put("Zip", addr.getPostalCode() != null ? addr.getPostalCode() : "N/A");
                        } else {
                            patientData.put("Address", "N/A");
                            patientData.put("City", "N/A");
                            patientData.put("State", "N/A");
                            patientData.put("Zip", "N/A");
                        }

                        patientData.put("Race", raceExtension != null && raceExtension.hasValue() ? raceExtension.getValue().toString() : "N/A");
                        patientData.put("Ethnicity", ethnicityExtension != null && ethnicityExtension.hasValue() ? ethnicityExtension.getValue().toString() : "N/A");

                        patientsList.add(patientData);
                    }
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Patient resources: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(patientsList);
        return patientsList;
    }

    @Override
    public List<Map<String, String>> searchResources(String[] searchFields, String[] searchValues) {
        if (searchFields.length != searchValues.length) {
            throw new IllegalArgumentException("The number of fields must match the number of values.");
        }

        setFhirServerUrl();
        List<Map<String, String>> patientsList = new ArrayList<>();

        try {
            // Initialize FHIR client
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Build the search query dynamically
            IQuery<IBaseBundle> query = client.search().forResource(Patient.class);

            for (int i = 0; i < searchFields.length; i++) {
                String searchField = searchFields[i].toLowerCase();
                String searchValue = searchValues[i];

                // Map search fields to FHIR API query parameters
                switch (searchField) {
                    case "patient":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("_id").exactly().code(searchValue));
                        break;
                    case "identifier":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("identifier").exactly().code(searchValue));
                        break;
                    case "name":
                        query = query.where(new ca.uhn.fhir.rest.gclient.StringClientParam("name").matches().value(searchValue));
                        break;
                    case "gender":
                        query = query.where(new ca.uhn.fhir.rest.gclient.TokenClientParam("gender").exactly().code(searchValue));
                        break;
                    case "birthdate":
                        query = query.where(new ca.uhn.fhir.rest.gclient.DateClientParam("birthdate").exactly().day(searchValue));
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
                    Patient patient = (Patient) entry.getResource();
                    patientsList.add(convertResourceToMap(patient));
                }

                // Retrieve the next page of the bundle
                bundle = bundle.getLink(Bundle.LINK_NEXT) != null
                        ? client.loadPage().next(bundle).execute()
                        : null;
            }
        } catch (Exception e) {
            System.err.println("Error searching Patient resources: " + e.getMessage());
            e.printStackTrace();
        }

        return patientsList;
    }


    @Override
    public Map<String, String> convertResourceToMap(Object resource) {
        if (!(resource instanceof Patient)) {
            throw new IllegalArgumentException("Resource is not of type Patient");
        }

        Patient patient = (Patient) resource;
        Map<String, String> patientData = new HashMap<>();

        // Identifier
        patientData.put("Identifier", patient.hasIdentifier() ?
                patient.getIdentifierFirstRep().getValue() : "N/A");

        // ID
        patientData.put("Patient", patient.getIdElement().getIdPart() != null ?
                patient.getIdElement().getIdPart() : "N/A");

        // Name
        if (patient.hasName()) {
            HumanName name = patient.getNameFirstRep();
            patientData.put("Name", name.getGivenAsSingleString() +
                    (name.hasFamily() ? " " + name.getFamily() : ""));
        } else {
            patientData.put("Name", "N/A");
        }

        // Gender
        patientData.put("Gender", patient.hasGender() ?
                patient.getGender().toCode() : "N/A");

        // Birth Date
        patientData.put("BirthDate", patient.hasBirthDate() ?
                patient.getBirthDate().toString() : "N/A");

        // Deceased Date
        if (patient.hasDeceasedDateTimeType()) {
            patientData.put("DeceasedDate",
                    patient.getDeceasedDateTimeType().getValueAsString());
        } else if (patient.hasDeceasedBooleanType()) {
            patientData.put("DeceasedDate", patient.getDeceasedBooleanType().booleanValue() ?
                    "Deceased" : "Alive");
        } else {
            patientData.put("DeceasedDate", "N/A");
        }

        // Address
        if (patient.hasAddress()) {
            Address address = patient.getAddressFirstRep();
            if (!address.getLine().isEmpty()) {
                patientData.put("Address", String.join(", ",
                        address.getLine().stream().map(StringType::getValue).toList()));
            } else {
                patientData.put("Address", "N/A");
            }
            patientData.put("City", address.hasCity() ? address.getCity() : "N/A");
            patientData.put("State", address.hasState() ? address.getState() : "N/A");
            patientData.put("Zip", address.hasPostalCode() ? address.getPostalCode() : "N/A");
        } else {
            patientData.put("Address", "N/A");
            patientData.put("City", "N/A");
            patientData.put("State", "N/A");
            patientData.put("Zip", "N/A");
        }

        // Race
        Extension raceExtension = patient.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/us-core-race");
        patientData.put("Race", raceExtension != null && raceExtension.hasValue() ?
                raceExtension.getValue().toString() : "N/A");

        // Ethnicity
        Extension ethnicityExtension = patient.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/us-core-ethnicity");
        patientData.put("Ethnicity", ethnicityExtension != null && ethnicityExtension.hasValue() ?
                ethnicityExtension.getValue().toString() : "N/A");

        // Telecom
        if (patient.hasTelecom()) {
            patientData.put("Telecom", String.join(", ",
                    patient.getTelecom().stream()
                            .map(ContactPoint::getValue)
                            .toList()));
        } else {
            patientData.put("Telecom", "N/A");
        }

        // Marital Status
        patientData.put("MaritalStatus", patient.hasMaritalStatus() ?
                patient.getMaritalStatus().getText() : "N/A");

        // Multiple Birth
        if (patient.hasMultipleBirthBooleanType()) {
            patientData.put("MultipleBirth", patient.getMultipleBirthBooleanType().booleanValue() ?
                    "Yes" : "No");
        } else if (patient.hasMultipleBirthIntegerType()) {
            patientData.put("MultipleBirth",
                    String.valueOf(patient.getMultipleBirthIntegerType().getValue()));
        } else {
            patientData.put("MultipleBirth", "N/A");
        }

        // Communication
        if (patient.hasCommunication()) {
            patientData.put("Communication", String.join(", ",
                    patient.getCommunication().stream()
                            .map(comm -> comm.getLanguage().getText())
                            .toList()));
        } else {
            patientData.put("Communication", "N/A");
        }

        // General Practitioner
        if (patient.hasGeneralPractitioner()) {
            patientData.put("GeneralPractitioner", String.join(", ",
                    patient.getGeneralPractitioner().stream()
                            .map(ref -> ref.getReference())
                            .toList()));
        } else {
            patientData.put("GeneralPractitioner", "N/A");
        }

        // Managing Organization
        patientData.put("ManagingOrganization", patient.hasManagingOrganization() ?
                patient.getManagingOrganization().getReference() : "N/A");

        return patientData;
    }



}
