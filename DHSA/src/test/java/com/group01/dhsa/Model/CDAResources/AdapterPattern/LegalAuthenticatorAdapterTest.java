package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.Header.LegalAuthenticatorAdapter;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Organization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class LegalAuthenticatorAdapterTest {
    LegalAuthenticatorAdapter legalAuthenticatorAdapter;
    FHIRClient fhirClient;
    Encounter encounter;

    @BeforeEach
    void setUp() {
        legalAuthenticatorAdapter = new LegalAuthenticatorAdapter();
        fhirClient = FHIRClient.getInstance();

        // Replace with an actual Encounter ID for testing purposes
        String encounterId = "9331c45b-0beb-42aa-1a13-012a432f7c3c";
        encounter = fhirClient.getEncounterById(encounterId);
    }

    @Test
    void toCdaObject() {
        LegalAuthenticator legalAuthenticator = legalAuthenticatorAdapter.toCdaObject(encounter);
        assertNotNull(legalAuthenticator, "The LegalAuthenticator object should not be null");

        // Verify the assigned entity is not null
        AssignedEntity assignedEntity = legalAuthenticator.getAssignedEntity();
        assertNotNull(assignedEntity, "The AssignedEntity should not be null");

        // Verify the ID is correctly generated
        Id assignedEntityId = assignedEntity.getId();
        assertNotNull(assignedEntityId, "The ID of AssignedEntity should not be null");
        assertEquals("2.16.840.1.113883.2.9.4.3.2", assignedEntityId.getRoot(), "The root ID does not match");



        // Verify the organization data is mapped correctly
        Organization organization = fhirClient.getOrganizationFromId(encounter.getServiceProvider().getReference().split("/")[1]);
        RepresentedOrganization representedOrganization = assignedEntity.getRepresentedOrganization();
        assertNotNull(representedOrganization, "The RepresentedOrganization should not be null");
        assertEquals(organization.getName(), representedOrganization.getName(), "The organization name does not match");

        // Verify the telecom and address for the organization
        Telecom telecom = representedOrganization.getTelecom();
        assertNotNull(telecom, "The Telecom should not be null");
        assertEquals("tel:" + organization.getContactFirstRep().getTelecom().getFirst().getValue().replace("-", ""), telecom.getValue(), "The telecom value does not match");

        Addr addr = assignedEntity.getAddr();

        assertNotNull(addr, "The Address of the RepresentedOrganization should not be null");
        assertEquals(organization.getContactFirstRep().getAddress().getCity(), addr.getCity(), "The organization city does not match");
        assertEquals(organization.getContactFirstRep().getAddress().getState(), addr.getState(), "The organization state does not match");

        // Verify the signature time
        LocalDateTime now = LocalDateTime.now();
        String expectedTime = now.atOffset(ZoneOffset.ofHours(1)).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ"));
        assertEquals(expectedTime, legalAuthenticator.getTime().getValue(), "The signature time does not match");

        System.out.println("LegalAuthenticator CDA successfully generated: " + legalAuthenticator);
    }
}