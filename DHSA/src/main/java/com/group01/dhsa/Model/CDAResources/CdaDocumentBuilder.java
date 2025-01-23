package com.group01.dhsa.Model.CDAResources;

import com.group01.dhsa.Model.CDAResources.AdapterPattern.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.*;
import com.group01.dhsa.Model.CDAResources.SectionModels.ClassXML.*;
import jakarta.xml.bind.*;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Practitioner;

import java.io.File;
import java.io.IOException;

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
    public void addComponentSection(Encounter encounter) {
        AdmissionAdapter admissionAdapter = new AdmissionAdapter();
        Component component = admissionAdapter.toCdaObject(encounter);

        clinicalDocument.setComponent(component);
    }

    // Example method for adding an observation section (commented out in original code)
/*
    public CdaDocumentBuilder addObservationSection(Observation fhirObservation) {
        ObservationAdapter observationAdapter = new ObservationAdapter();
        ObservationCDA observationCDA = observationAdapter.toCdaObject(fhirObservation);

        clinicalDocument.setObservationSection(observationCDA);
        return this;
    }
*/

    /**
     * Builds the CDA document by serializing it to an XML file.
     *
     * @return The generated XML file
     * @throws JAXBException If an error occurs during JAXB marshalling
     * @throws IOException   If an error occurs while creating the temporary file
     */
    public File build() throws JAXBException, IOException {
        // Create a JAXB context for serializing the ClinicalDocument
        JAXBContext context = JAXBContext.newInstance(ClinicalDocument.class);

        // Configure the marshaller for pretty-printing the XML
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Create a temporary file to store the XML
        File tempFile = File.createTempFile("clinicalDocument", ".xml");

        // Serialize the ClinicalDocument to the temporary file
        marshaller.marshal(clinicalDocument, tempFile);

        return tempFile;
    }
}
