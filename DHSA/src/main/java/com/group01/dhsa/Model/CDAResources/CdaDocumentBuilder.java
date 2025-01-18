package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.*;
import jakarta.xml.bind.*;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Patient;


public class CdaDocumentBuilder {

    private ClinicalDocument clinicalDocument;
    private ObjectFactory objectFactory;

    public CdaDocumentBuilder() {
        this.objectFactory = new ObjectFactory();
        this.clinicalDocument = objectFactory.createClinicalDocument();
    }

    public CdaDocumentBuilder addPatientSection(Patient fhirPatient) {
        // Usa l'adapter per convertire il paziente FHIR in CDA
        PatientAdapter patientAdapter = new PatientAdapter();
        PatientCDA patientCDA = patientAdapter.toCdaObject(fhirPatient);

        // Aggiungi la sezione paziente al ClinicalDocument
        clinicalDocument.setPatientSection(patientCDA);
        return this;
    }

    public CdaDocumentBuilder addObservationSection(Observation fhirObservation) {
        // Usa l'adapter per convertire l'osservazione FHIR in CDA
        ObservationAdapter observationAdapter = new ObservationAdapter();
        ObservationCDA observationCDA = observationAdapter.toCdaObject(fhirObservation);

        // Aggiungi la sezione osservazione al ClinicalDocument
        clinicalDocument.setObservationSection(observationCDA);
        return this;
    }

    // Metodo che crea il documento CDA e lo serializza in XML
    public void build() throws JAXBException {
        // Crea il contesto JAXB per il marshalling del documento CDA
        JAXBContext context = JAXBContext.newInstance(ClinicalDocument.class);

        // Crea un marshaller per serializzare in XML
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Serializza l'intero ClinicalDocument in XML
        marshaller.marshal(clinicalDocument, System.out);
    }
}

