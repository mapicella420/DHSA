package com.group01.dhsa.Model.FhirResources.Level4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.group01.dhsa.Model.FhirResources.FhirResourceImporter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.ExtendedContactDetail;
import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.Quantity;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class OrganizationImporter implements FhirResourceImporter {
    private static final String FHIR_SERVER_URL = "http://localhost:8080/fhir";

    @Override
    public void importCsvToFhir(String csvFilePath) {
        try {
            // Inizializza il client FHIR
            FhirContext fhirContext = FhirContext.forR5();
            IGenericClient client = fhirContext.newRestfulGenericClient(FHIR_SERVER_URL);

            // Leggi il file CSV
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .withFirstRecordAsHeader()
                    .parse(in);

            // Itera sui record del CSV
            for (CSVRecord record : records) {
                String organizationId = record.get("Id");

                // Controlla se l'Organization esiste già
                if (organizationExistsByIdentifier(client, organizationId)) {
                    System.out.println("Organization with ID " + organizationId + " already exists. Skipping.");
                    continue;
                }

                Organization organization = new Organization();

                // ID
                if (record.isMapped("Id") && !record.get("Id").isEmpty()) {
                    organization.addIdentifier().setValue(organizationId);

                }

                // Nome dell'organizzazione
                if (record.isMapped("NAME") && !record.get("NAME").isEmpty()) {
                    organization.setName(record.get("NAME"));
                }

                // Contatti (includono indirizzo e telefono)
                if (record.isMapped("ADDRESS") || record.isMapped("CITY") || record.isMapped("STATE") || record.isMapped("ZIP") || record.isMapped("PHONE")) {
                    ExtendedContactDetail contactDetail = new ExtendedContactDetail();
                    Address address = new Address();

                    // Popola l'indirizzo
                    if (record.isMapped("ADDRESS") && !record.get("ADDRESS").isEmpty()) {
                        address.addLine(record.get("ADDRESS"));
                    }
                    if (record.isMapped("CITY") && !record.get("CITY").isEmpty()) {
                        address.setCity(record.get("CITY"));
                    }
                    if (record.isMapped("STATE") && !record.get("STATE").isEmpty()) {
                        address.setState(record.get("STATE"));
                    }
                    if (record.isMapped("ZIP") && !record.get("ZIP").isEmpty()) {
                        address.setPostalCode(record.get("ZIP"));
                    }

                    // Aggiunge l'indirizzo al contatto
                    contactDetail.setAddress(address);

                    // Popola il telefono
                    if (record.isMapped("PHONE") && !record.get("PHONE").isEmpty()) {
                        contactDetail.addTelecom(new ContactPoint()
                                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                                .setValue(record.get("PHONE")));
                    }

                    // Aggiunge il contatto all'organizzazione
                    List<ExtendedContactDetail> contactDetails = new ArrayList<>();
                    contactDetails.add(contactDetail);
                    organization.setContact(contactDetails);
                }

                // Coordinate geografiche
                if (record.isMapped("LAT") && record.isMapped("LON") &&
                        !record.get("LAT").isEmpty() && !record.get("LON").isEmpty()) {
                    organization.addExtension("http://hl7.org/fhir/StructureDefinition/geolocation-lat",
                            new Quantity().setValue(Double.parseDouble(record.get("LAT"))).setCode("latitude"));
                    organization.addExtension("http://hl7.org/fhir/StructureDefinition/geolocation-lon",
                            new Quantity().setValue(Double.parseDouble(record.get("LON"))).setCode("longitude"));
                }

                // Fatturato (REVENUE) come estensione
                if (record.isMapped("REVENUE") && !record.get("REVENUE").isEmpty()) {
                    organization.addExtension("http://hl7.org/fhir/StructureDefinition/organization-revenue",
                            new Quantity().setValue(Double.parseDouble(record.get("REVENUE"))));
                }

                // Utilizzo (UTILIZATION) come estensione
                if (record.isMapped("UTILIZATION") && !record.get("UTILIZATION").isEmpty()) {
                    organization.addExtension("http://hl7.org/fhir/StructureDefinition/organization-utilization",
                            new Quantity().setValue(Double.parseDouble(record.get("UTILIZATION"))));
                }

                // Invia l'organizzazione al server FHIR
                client.create().resource(organization).execute();

                // Log di conferma
                System.out.println("Organizzazione con ID " + organizationId + " caricata con successo.");
            }
        } catch (Exception e) {
            // Gestione errori
            System.err.println("Errore durante l'importazione del CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Controlla se un'Organization con il dato identifier esiste già sul server FHIR.
     *
     * @param client Il client FHIR utilizzato per connettersi al server.
     * @param organizationIdentifier L'identifier dell'Organization da cercare.
     * @return true se un'Organization con l'identifier fornito esiste, altrimenti false.
     */
    private boolean organizationExistsByIdentifier(IGenericClient client, String organizationIdentifier) {
        try {
            var bundle = client.search()
                    .forResource("Organization")
                    .where(Organization.IDENTIFIER.exactly().identifier(organizationIdentifier))
                    .returnBundle(org.hl7.fhir.r5.model.Bundle.class)
                    .execute();

            return !bundle.getEntry().isEmpty();
        } catch (Exception e) {
            System.err.println("Errore durante il controllo dell'Organization con identifier: " + e.getMessage());
            return false;
        }
    }
}
