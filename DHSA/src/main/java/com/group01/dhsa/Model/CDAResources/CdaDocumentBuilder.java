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

