package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.Model.CDAResources.CodiceFiscaleCalculator;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.*;
import org.hl7.fhir.r5.model.Patient;

public class PatientAdapter implements CdaSection<RecordTarget, Patient> {

    @Override
    public RecordTarget toCdaObject(Patient fhirObject) {

        // Creare un oggetto PatientCDA per il modello CDA
        RecordTarget target = new RecordTarget();

        PatientRole role = new PatientRole();

        target.setPatientRole(role);

        PatientCDA patientCDA = new PatientCDA();

        String nome = fhirObject.getName().getFirst().getGiven().getFirst().asStringValue();
        String cognome = fhirObject.getName().getFirst().getFamily();
        int giorno = fhirObject.getBirthDateElement().getDay();
        String mese = fhirObject.getBirthDateElement().getMonth().toString();
        int anno = fhirObject.getBirthDateElement().getYear();
        String sesso = fhirObject.getGender().toString();
        String luogo = "STATI UNITI";
        boolean isEstero = true;

        CodiceFiscaleCalculator cf = new CodiceFiscaleCalculator(nome, cognome, giorno, mese,
                anno, sesso, luogo, isEstero);

        role.setId(new Id(cf.calcolaCodiceFiscale(),"2.16.840.1.113883.2.9.4.3.2" , "Ministero Economia e Finanze"));

        role.setPatient(patientCDA);

        Addr addr = new Addr();
        addr.setUse("HP");
        addr.setCountry("536");//Codice ISTAT US
        addr.setState(fhirObject.getAddress().getFirst().getState());
        addr.setCity(fhirObject.getAddress().getFirst().getCity());
        addr.setStreetAddressLine(fhirObject.getAddress().getFirst().getLine().getFirst().toString());

        role.setAddr(addr);

        Name name = new Name();
        name.setFamily(cognome);
        name.setGiven(nome);

        patientCDA.setName(name);

        AdministrativeGenderCode gender = new AdministrativeGenderCode();

        gender.setCode(fhirObject.getGender().toString().toLowerCase());
        if(fhirObject.getGender().toString().equalsIgnoreCase("male")){
            gender.setDisplayName("Maschio");
        }else if(fhirObject.getGender().toString().equalsIgnoreCase("female")){
            gender.setDisplayName("Femmina");
        }else if(fhirObject.getGender().toString().equalsIgnoreCase("other")){
            gender.setDisplayName("Altro");
        }else{
            gender.setDisplayName("Sconosciuto");
        }

        gender.setCodeSystem("2.16.840.1.113883.4.642.4.2");
        gender.setCodeSystemName("HL7 AdministrativeGender");

        patientCDA.setAdministrativeGenderCode(gender);

        BirthTime birthTime = new BirthTime();
        birthTime.setValue(fhirObject.getBirthDateElement().asStringValue());

        patientCDA.setBirthTime(birthTime);


//
//        // Nome del paziente
//        if (fhirObject.hasName()) {
//            fhirObject.getName().forEach(name -> {
//                // Impostare nome e cognome
//                patientCDA.setFirstName(name.getGivenAsSingleString());
//                patientCDA.setLastName(name.getFamily());
//            });
//        }
//
//        // Sesso del paziente
//        if (fhirObject.hasGender()) {
//            patientCDA.setGender(fhirObject.getGender().toCode());
//        }
//
//        // Data di nascita del paziente
//        if (fhirObject.hasBirthDate()) {
//            patientCDA.setBirthDate(fhirObject.getBirthDate().toString());
//        }
//
//        // Codice identificativo regionale (se disponibile)
//        if (fhirObject.hasIdentifier()) {
//            fhirObject.getIdentifier().forEach(id -> {
//                // Per esempio, si può cercare il codice identificativo regionale
//                if ("[OID_ROOT_ANAGRAFE_REGIONALE]".equals(id.getSystem())) {
//                    patientCDA.setCodiceIdentificativoRegionale(id.getValue());
//                }
//            });
//        }

        return target;
    }
}