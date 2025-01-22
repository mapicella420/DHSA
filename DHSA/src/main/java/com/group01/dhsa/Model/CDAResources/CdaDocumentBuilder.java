package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Practitioner;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;


public class CdaDocumentBuilder {

    private ClinicalDocument clinicalDocument;
    private ObjectFactory objectFactory;

    public CdaDocumentBuilder(Integer idNumber) {
        this.objectFactory = new ObjectFactory();
        this.clinicalDocument = objectFactory.createClinicalDocument(idNumber);
    }

    public void addPatientSection(Patient fhirPatient) {
        PatientAdapter patientAdapter = new PatientAdapter();
        RecordTarget recordTarget = patientAdapter.toCdaObject(fhirPatient);

        clinicalDocument.setRecordTarget(recordTarget);
    }

    public void addAuthorSection(Practitioner fhirProvider) {
        AuthorAdapter authorAdapter = new AuthorAdapter();
        Author author = authorAdapter.toCdaObject(fhirProvider);

        clinicalDocument.setAuthor(author);
    }

    public void addCustodianSection() {
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
    }

    public void addLegalAuthenticatorSection(Encounter encounter) {
        LegalAuthenticatorAdapter legalAuthenticatorAdapter = new LegalAuthenticatorAdapter();

        LegalAuthenticator legalAuthenticator = legalAuthenticatorAdapter.toCdaObject(encounter);

        clinicalDocument.setLegalAuthenticator(legalAuthenticator);
    }

    public void addComponentOfSection(Encounter encounter) {
        ComponentOfAdapter componentOfAdapter = new ComponentOfAdapter();

        ComponentOf componentOf = componentOfAdapter.toCdaObject(encounter);

        clinicalDocument.setComponentOf(componentOf);
    }

    public void addAdmissionSection(Encounter encounter) {
        AdmissionAdapter admissionAdapter = new AdmissionAdapter();

        Component component = admissionAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    public void addClinicalHistorySection(Encounter encounter) {
        ClinicalHistoryAdapter clinicalHistoryAdapter = new ClinicalHistoryAdapter();

        Component component = clinicalHistoryAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    public void addHospitalCourseSection(Encounter encounter) {
        HospitalCourseAdapter hospitalCourseAdapter = new HospitalCourseAdapter();

        Component component = hospitalCourseAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    public File  build() throws JAXBException, IOException, TransformerException {

        JAXBContext context = JAXBContext.newInstance(ClinicalDocument.class);


        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);


        StringWriter stringWriter = new StringWriter();


        marshaller.marshal(clinicalDocument, stringWriter);


        File tempFile = File.createTempFile("clinicalDocument", ".xml");


        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");


        transformer.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(stringWriter.toString())),
                new StreamResult(tempFile));

        return tempFile;
    }
}

