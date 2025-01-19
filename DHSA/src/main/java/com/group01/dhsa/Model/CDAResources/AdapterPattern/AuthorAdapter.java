package com.group01.dhsa.Model.CDAResources.AdapterPattern;


import com.group01.dhsa.Model.CDAResources.CodiceFiscaleCalculator;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import org.hl7.fhir.r5.model.Practitioner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class AuthorAdapter implements CdaSection<Author, Practitioner>{

    @Override
    public Author toCdaObject(Practitioner fhirObject) {
        Author author = new Author();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        author.setTime(now.atOffset(ZoneOffset.ofHours(1)).format(formatter));

        AssignedAuthor assignedAuthor = new AssignedAuthor();

        // Define the date range (1st Jan 1955 to 31st Dec 1955)
        LocalDate startDate = LocalDate.of(1955, 1, 1);
        LocalDate endDate = LocalDate.of(1955, 12, 31);
        // Calculate the number of days between start and end date
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        // Generate a random number of days to add to the start date
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween);
        // Generate the random date by adding the random number of days to the start date
        LocalDate randomBirthDate = startDate.plusDays(randomDays);
        int giorno = randomBirthDate.getDayOfMonth();
        String mese = String.valueOf(randomBirthDate.getMonthValue());
        int anno = randomBirthDate.getYear();

        String nome = fhirObject.getName().getFirst().getGiven().getFirst().asStringValue();
        String cognome = fhirObject.getName().getFirst().getFamily();
        String sesso = fhirObject.getGender().toString();
        String luogo = "STATI UNITI";
        boolean isEstero = true;

        CodiceFiscaleCalculator cf = new CodiceFiscaleCalculator(nome, cognome, giorno, mese,
                anno, sesso, luogo, isEstero);

        assignedAuthor.setId(new Id(cf.calcolaCodiceFiscale(),"2.16.840.1.113883.2.9.4.3.2" , "Ministero Economia e Finanze"));

        AssignedPerson assignedPerson = new AssignedPerson();

        Name name = new Name();
        name.setFamily(cognome);
        name.setGiven(nome);
        assignedPerson.setName(name);

        assignedAuthor.setAssignedPerson(assignedPerson);
        author.setAssignedAuthor(assignedAuthor);

        return author;
    }
}
