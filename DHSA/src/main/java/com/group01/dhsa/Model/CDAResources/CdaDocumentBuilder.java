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

/**
 * The `CdaDocumentBuilder` class is responsible for constructing and serializing
 * a CDA (Clinical Document Architecture) document from FHIR resources.
 * It uses the adapter pattern to convert FHIR resources into CDA-compliant objects.
 */
public class CdaDocumentBuilder {

    private ClinicalDocument clinicalDocument; // The main CDA document structure
    private ObjectFactory objectFactory;       // Factory for creating CDA elements

    /**
     * Constructor initializes the builder with a unique document ID.
     *
     * @param idNumber A unique identifier for the document
     */
    public CdaDocumentBuilder(Integer idNumber) {
        this.objectFactory = new ObjectFactory();
        this.clinicalDocument = objectFactory.createClinicalDocument(idNumber);
    }

    /**
     * Adds the patient section to the CDA document using a `PatientAdapter`.
     *
     * @param fhirPatient The FHIR `Patient` resource to be included in the CDA
     */
    public void addPatientSection(Patient fhirPatient) {
        PatientAdapter patientAdapter = new PatientAdapter();
        RecordTarget recordTarget = patientAdapter.toCdaObject(fhirPatient);

        clinicalDocument.setRecordTarget(recordTarget);
    }

    /**
     * Adds the author section to the CDA document using an `AuthorAdapter`.
     *
     * @param fhirProvider The FHIR `Practitioner` resource representing the author
     */
    public void addAuthorSection(Practitioner fhirProvider) {
        AuthorAdapter authorAdapter = new AuthorAdapter();
        Author author = authorAdapter.toCdaObject(fhirProvider);

        clinicalDocument.setAuthor(author);
    }

    /**
     * Adds the custodian section to the CDA document.
     * This represents the organization responsible for maintaining the document.
     */
    public void addCustodianSection() {
        Custodian custodian = new Custodian();
        AssignedCustodian assignedCustodian = new AssignedCustodian();
        RepresentedCustodianOrganization representedCustodian = new RepresentedCustodianOrganization();

        // Set organizational details
        assignedCustodian.setRepresentedCustodianOrganization(representedCustodian);
        custodian.setAssignedCustodian(assignedCustodian);

        // Example organization details
        representedCustodian.setId(new Id("150201", "2.16.840.1.113883.2.9.4.1.2", "Ministero della Salute"));
        representedCustodian.setName("ASL Avellino");
        Addr addr = new Addr("HP", "VIA DEGLI IMBIMBO 10/12", "100", "AV", "Avellino");
        representedCustodian.setAddr(addr);

        clinicalDocument.setCustodian(custodian);
    }

    /**
     * Adds the legal authenticator section to the CDA document.
     * This represents the individual legally responsible for the document's content.
     *
     * @param encounter The FHIR `Encounter` resource used to extract details
     */
    public void addLegalAuthenticatorSection(Encounter encounter) {
        LegalAuthenticatorAdapter legalAuthenticatorAdapter = new LegalAuthenticatorAdapter();
        LegalAuthenticator legalAuthenticator = legalAuthenticatorAdapter.toCdaObject(encounter);

        clinicalDocument.setLegalAuthenticator(legalAuthenticator);
    }

    /**
     * Adds the "componentOf" section to the CDA document, which links to the encounter.
     *
     * @param encounter The FHIR `Encounter` resource providing details
     */
    public void addComponentOfSection(Encounter encounter) {
        ComponentOfAdapter componentOfAdapter = new ComponentOfAdapter();
        ComponentOf componentOf = componentOfAdapter.toCdaObject(encounter);

        clinicalDocument.setComponentOf(componentOf);
    }

    /**
     * Adds the component section to the CDA document, which includes admission details.
     *
     * @param encounter The FHIR `Encounter` resource containing admission details
     */
    public void addAdmissionSection(Encounter encounter) {
        AdmissionAdapter admissionAdapter = new AdmissionAdapter();
        Component component = admissionAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    /**
     * Builds the CDA document by serializing it to an XML file.
     *
     * @return The generated XML file
     * @throws JAXBException If an error occurs during JAXB marshalling
     * @throws IOException   If an error occurs while creating the temporary file
     */
    public File  build() throws JAXBException, IOException, TransformerException {

        JAXBContext context = JAXBContext.newInstance(ClinicalDocument.class);

        // Configure the marshaller for pretty-printing the XML

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);


        // Create a temporary file to store the XML
        StringWriter stringWriter = new StringWriter();


        marshaller.marshal(clinicalDocument, stringWriter);


        File tempFile = File.createTempFile("clinicalDocument", ".xml");

        // Serialize the ClinicalDocument to the temporary file
        marshaller.marshal(clinicalDocument, tempFile);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");


        transformer.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(stringWriter.toString())),
                new StreamResult(tempFile));

        return tempFile;
    }

    /**
     * Adds a hospital course section to the CDA document.
     *
     * This section describes the course of the patient's treatment during their hospital stay.
     *
     * @param encounter The FHIR `Encounter` resource that provides details for the hospital course section.
     */
    public void addHospitalCourseSection(Encounter encounter) {
        // Create an adapter to convert the FHIR Encounter resource into a CDA-compliant hospital course section.
        HospitalCourseAdapter hospitalCourseAdapter = new HospitalCourseAdapter();

        // Convert the Encounter resource to a CDA Component object.
        Component component = hospitalCourseAdapter.toCdaObject(encounter);

        // Retrieve the current list of components in the ClinicalDocument.
        List<Component> list = clinicalDocument.getComponent();

        // Add the new hospital course component to the list.
        list.add(component);

        // Update the ClinicalDocument with the modified list of components.
        clinicalDocument.setComponent(list);
    }

    /**
     * Adds a clinical history section to the CDA document.
     *
     * This section outlines the patient's medical history, including prior conditions and treatments.
     *
     * @param encounter The FHIR `Encounter` resource that provides details for the clinical history section.
     */
    public void addClinicalHistorySection(Encounter encounter) {
        // Create an adapter to convert the FHIR Encounter resource into a CDA-compliant clinical history section.
        ClinicalHistoryAdapter clinicalHistoryAdapter = new ClinicalHistoryAdapter();

        // Convert the Encounter resource to a CDA Component object.
        Component component = clinicalHistoryAdapter.toCdaObject(encounter);

        // Retrieve the current list of components in the ClinicalDocument.
        List<Component> list = clinicalDocument.getComponent();

        // Add the new clinical history component to the list.
        list.add(component);

        // Update the ClinicalDocument with the modified list of components.
        clinicalDocument.setComponent(list);
    }

    public void addHospitalDischargeStudiesSection(Encounter encounter) {
        HospitalDischargeStudiesAdapter hospitalDischargeStudiesAdapter = new HospitalDischargeStudiesAdapter();

        Component component = hospitalDischargeStudiesAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    public void addRelevantDiagnosticSection(Encounter encounter) {
        RelevantDiagnosticAdapter relevantDiagnosticAdapter = new RelevantDiagnosticAdapter();

        Component component = relevantDiagnosticAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    public void addHistoryOfProceduresSection(Encounter encounter) {
        HistoryOfProceduresAdapter historyOfProceduresAdapter = new HistoryOfProceduresAdapter();

        Component component = historyOfProceduresAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

    public void addAllergySection(Encounter encounter) {
        AllergyAdapter allergyAdapter = new AllergyAdapter();

        Component component = allergyAdapter.toCdaObject(encounter);

        List<Component> list = clinicalDocument.getComponent();
        list.add(component);
        clinicalDocument.setComponent(list);
    }

}

