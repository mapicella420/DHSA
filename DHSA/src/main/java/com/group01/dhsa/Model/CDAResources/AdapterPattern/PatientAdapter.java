package com.group01.dhsa.Model.CDAResources.AdapterPattern;

import com.group01.dhsa.Model.CDAResources.CodiceFiscaleCalculator;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.Id;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.PatientRole;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.RecordTarget;
import com.group01.dhsa.Model.CDAResources.SectionModels.PatientCDA;
import org.hl7.fhir.r5.model.Patient;

public class PatientAdapter implements CdaSection<PatientCDA, Patient> {

    @Override
    public PatientCDA toCdaObject(Patient fhirObject) {

        // Creare un oggetto PatientCDA per il modello CDA
        RecordTarget target = new RecordTarget();

        PatientRole role = new PatientRole();

        target.setPatientRole(role);

        PatientCDA patientCDA = new PatientCDA();

        String nome = fhirObject.getName().get(0).getGiven().toString();
        String cognome = fhirObject.getName().get(0).getFamily();
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

        return patientCDA;
    }
}