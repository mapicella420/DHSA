package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.Model.CDAResources.CodiceFiscaleCalculator;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.Practitioner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class LegalAuthenticatorAdapter implements CdaSection <LegalAuthenticator, Encounter> {

    @Override
    public LegalAuthenticator toCdaObject(Encounter fhirObject) {
        FHIRClient client = FHIRClient.getInstance();
        Practitioner practitioner = client
                .getPractitionerFromId(fhirObject.getParticipant().getFirst()
                        .getActor().getReference().split("/")[1]);

        Organization organization = client.getOrganizationFromId(fhirObject.getServiceProvider()
                .getReference().split("/")[1]);

        LegalAuthenticator legalAuthenticator = new LegalAuthenticator();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");
        legalAuthenticator.setTime(now.atOffset(ZoneOffset.ofHours(1)).format(formatter));

        legalAuthenticator.setSignatureCode(new SignatureCode("S"));

        AssignedEntity assignedEntity = new AssignedEntity();

        // Reference year (2025)
        int currentYear = 2025;
        // Calculate the minimum and maximum birth years (75 years old in 2025, 27 years old in 2025)
        int minBirthYear = currentYear - 75;  // 75 years old in 2025
        int maxBirthYear = currentYear - 27;  // 27 years old in 2025

        // Generate a random year between minBirthYear and maxBirthYear
        int randomYear = ThreadLocalRandom.current().nextInt(minBirthYear, maxBirthYear + 1);
        // Calculate the start date (January 1st of the random year) and the end date (December 31st of the random year)
        LocalDate startDate = LocalDate.of(randomYear, 1, 1);
        LocalDate endDate = LocalDate.of(randomYear, 12, 31);
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        // Generate a random number of days to add to the start date
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween);
        // Generate the random birthdate by adding the random number of days to the start date
        LocalDate randomBirthDate = startDate.plusDays(randomDays);
        int giorno = randomBirthDate.getDayOfMonth();
        String mese = String.valueOf(randomBirthDate.getMonthValue());
        int anno = randomBirthDate.getYear();

        String nome = practitioner.getName().getFirst().getGiven().getFirst().asStringValue();
        String cognome = practitioner.getName().getFirst().getFamily();
        String sesso = practitioner.getGender().toString();
        String luogo = "STATI UNITI";
        boolean isEstero = true;

        CodiceFiscaleCalculator cf = new CodiceFiscaleCalculator(nome, cognome, giorno, mese,
                anno, sesso, luogo, isEstero);

        assignedEntity.setId(new Id(cf.calcolaCodiceFiscale(),"2.16.840.1.113883.2.9.4.3.2" , "Ministero Economia e Finanze"));

        Addr addr = new Addr();
        addr.setUse("HP");
        addr.setCountry("536");//Codice ISTAT US
        addr.setState(practitioner.getAddress().getFirst().getState());
        addr.setCity(practitioner.getAddress().getFirst().getCity());
        addr.setStreetAddressLine(practitioner.getAddress().getFirst().getLine().getFirst().toString());

        assignedEntity.setAddr(addr);

        AssignedPerson assignedPerson = new AssignedPerson();
        assignedPerson.setName(new Name(nome, cognome));

        assignedEntity.setAssignedPerson(assignedPerson);

        RepresentedOrganization representedOrganization = new RepresentedOrganization();
        assignedEntity.setRepresentedOrganization(representedOrganization);

        representedOrganization.setName(organization.getName());
        if (!organization.getContactFirstRep().getTelecom().isEmpty()) {
            representedOrganization.setTelecom(new Telecom("HP","tel:"+organization
                    .getContactFirstRep().getTelecom().getFirst().getValue().replace("-","")));
        }

        Addr addrOrg = new Addr();
        addr.setUse("HP");
        addr.setCountry("536");//Codice ISTAT US
        addr.setState(organization.getContactFirstRep().getAddress().getState());
        addr.setCity(organization.getContactFirstRep().getAddress().getCity());
        addr.setStreetAddressLine(organization.getContactFirstRep().getAddress()
                .getLine().getFirst().toString());
        representedOrganization.setAddr(addrOrg);

        legalAuthenticator.setAssignedEntity(assignedEntity);

        return legalAuthenticator;
    }
}
