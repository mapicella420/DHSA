package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.CodiceFiscaleCalculator;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.Practitioner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class ComponentOfAdapter implements CdaSection <ComponentOf, Encounter>{


    @Override
    public ComponentOf toCdaObject(Encounter fhirObject) {
        ComponentOf componentOf = new ComponentOf();
        EncompassingEncounter encompassingEncounter = new EncompassingEncounter();
        componentOf.setEncompassingEncounter(encompassingEncounter);

        Organization organization = FHIRClient.getInstance()
                .getOrganizationFromId(fhirObject.getServiceProvider()
                        .getReference().split("/")[1]);

        //root scelto come ramo nosologico 5 perchè non lo abbiamo
        encompassingEncounter.setId(new Id(
                fhirObject.getIdentifierFirstRep().getValue(),
                "2.16.840.1.113883.2.9.2.5.4.6",
                organization.getName()
        ));
        // Parse the input string to a ZonedDateTime
        ZonedDateTime zonedDateTime = fhirObject.getActualPeriod()
                .getStart().toInstant().atZone(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");
        String formattedDate = zonedDateTime.format(formatter);

        zonedDateTime = fhirObject.getActualPeriod()
                .getEnd().toInstant().atZone(ZoneId.of("UTC"));
        String formattedEnd = zonedDateTime.format(formatter);

        EffectiveTime effectiveTime = new EffectiveTime(
                new Low(formattedDate),
                new High(formattedEnd));
        encompassingEncounter.setEffectiveTime(effectiveTime);

        ResponsibleParty responsibleParty = new ResponsibleParty();
        encompassingEncounter.setResponsibleParty(responsibleParty);

        AssignedEntity assignedEntity = new AssignedEntity();
        responsibleParty.setAssignedEntity(assignedEntity);

        AssignedPerson assignedPerson = new AssignedPerson();
        assignedEntity.setAssignedPerson(assignedPerson);

        Practitioner practitioner = FHIRClient.getInstance().getPractitionerFromId(
                fhirObject.getParticipant().getFirst().getActor()
                .getReference().split("/")[1]);

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

        assignedPerson.setName(new Name(
                nome, cognome, "Dr."
        ));

        Location location = new Location();
        encompassingEncounter.setLocation(location);

        HealthCareFacility healthCareFacility = new HealthCareFacility();
        location.setHealthCareFacility(healthCareFacility);

        //Codice struttura 02, 26=medicina generale
        healthCareFacility.setId(new Id(
                "150201.02.26",
                "2.16.840.1.113883.2.9.4.1.6",
                "Ministero della Salute"
        ));

        healthCareFacility.setLocation(new LocationHealth("Medicina Generale"));

        ServiceProviderOrganization serviceProviderOrganization = new ServiceProviderOrganization();
        healthCareFacility.setServiceProviderOrganization(serviceProviderOrganization);

        serviceProviderOrganization.setId(new Id(
            "150201",
                "2.16.840.1.113883.2.9.4.1.2",
                "Ministero della Salute"
        ));

        serviceProviderOrganization.setName("ASL Avellino");

        AsOrganizationPartOf asOrganizationPartOf = new AsOrganizationPartOf();
        serviceProviderOrganization.setAsOrganizationPartOf(asOrganizationPartOf);

        //Codice Estero 999999
        asOrganizationPartOf.setId(new Id(
                "999999",
                "2.16.840.1.113883.2.9.4.1.1",
                "Ministero della Salute"
        ));

        return componentOf;
    }
}
