package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.*;
import org.hl7.fhir.r5.model.*;

import java.io.File;
import java.io.IOException;


public class CdaDocumentBuilder {

    private ClinicalDocument clinicalDocument;
    private ObjectFactory objectFactory;

    public CdaDocumentBuilder(Integer idNumber) {
        this.objectFactory = new ObjectFactory();
        this.clinicalDocument = objectFactory.createClinicalDocument(idNumber);
    }

    public CdaDocumentBuilder addPatientSection(Patient fhirPatient) {
        PatientAdapter patientAdapter = new PatientAdapter();
        RecordTarget recordTarget = patientAdapter.toCdaObject(fhirPatient);

        clinicalDocument.setRecordTarget(recordTarget);
        return this;
    }

    public CdaDocumentBuilder addAuthorSection(Practitioner fhirProvider) {
        AuthorAdapter authorAdapter = new AuthorAdapter();
        Author author = authorAdapter.toCdaObject(fhirProvider);

        clinicalDocument.setAuthor(author);
        return this;
    }

    public CdaDocumentBuilder addCustodianSection() {
        Custodian custodian = new Custodian();
        AssignedCustodian assignedCustodian = new AssignedCustodian();
        RepresentedCustodianOrganization representedCustodian = new RepresentedCustodianOrganization();

        assignedCustodian.setRepresentedCustodianOrganization(representedCustodian);
        custodian.setAssignedCustodian(assignedCustodian);

        //Codice Struttura + Codice Ente = Codice HSP11. es. ASL Avellino 150201
        representedCustodian.setId(new Id("150201", "2.16.840.1.113883.2.9.4.1.2", "Ministero della Salute"));
        representedCustodian.setName("ASL Avellino");
        Addr addr = new Addr("HP", "VIA DEGLI IMBIMBO 10/12", "100", "AV", "Avellino");
        representedCustodian.setAddr(addr);

        clinicalDocument.setCustodian(custodian);
        return this;
    }
//
//    public CdaDocumentBuilder addObservationSection(Observation fhirObservation) {
//        // Usa l'adapter per convertire l'osservazione FHIR in CDA
//        ObservationAdapter observationAdapter = new ObservationAdapter();
//        ObservationCDA observationCDA = observationAdapter.toCdaObject(fhirObservation);
//
//        // Aggiungi la sezione osservazione al ClinicalDocument
//        clinicalDocument.setObservationSection(observationCDA);
//        return this;
//    }

    // Metodo che crea il documento CDA e lo serializza in XML
    public File  build() throws JAXBException, IOException {
        // Crea il contesto JAXB per il marshalling del documento CDA
        JAXBContext context = JAXBContext.newInstance(ClinicalDocument.class);

        // Crea un marshaller per serializzare in XML
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Crea un file temporaneo
        File tempFile = File.createTempFile("clinicalDocument", ".xml");

        // Serializza il ClinicalDocument nel file temporaneo
        marshaller.marshal(clinicalDocument, tempFile);

        return tempFile;
    }
}

