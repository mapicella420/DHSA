package com.group01.dhsa.Model;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.FHIRClient;
import com.group01.dhsa.Model.CDAResources.CdaDocumentBuilder;
import com.group01.dhsa.ObserverPattern.EventObservable;
import jakarta.xml.bind.JAXBException;
import org.hl7.fhir.r5.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class is responsible for creating a CDA document based on FHIR data.
 * It fetches patient, practitioner, and encounter data using the FHIR client
 * and builds the CDA document through the {@link CdaDocumentBuilder}.
 */
public class CdaDocumentCreator {
    private final EventObservable eventObservable;

    /**
     * Default constructor initializing the event observable from the EventManager.
     */
    public CdaDocumentCreator() {
        this.eventObservable = EventManager.getInstance().getEventObservable();
    }

    /**
     * Constructor allowing the injection of a custom {@link EventObservable}.
     *
     * @param eventObservable The event observable to be used.
     */
    public CdaDocumentCreator(EventObservable eventObservable) {
        this.eventObservable = eventObservable;
    }

    /**
     * Creates a CDA document for the specified patient and encounter,
     * then notifies listeners of the success or failure of the operation.
     *
     * @param patientId   The ID of the patient for whom the CDA is being created.
     * @param encounterId The ID of the encounter to include in the CDA.
     */
    public void createAndNotify(String patientId, String encounterId) {
        try {
            // Creates the CDA document
            File cdaFile = createCdaDocument(patientId, encounterId);
            // Notify listeners about the successful generation
            eventObservable.notify("cda_generated", cdaFile);
        } catch (Exception e) {
            e.printStackTrace();
            // Notify listeners about the failure
            eventObservable.notify("cda_generation_failed", null);
        }
    }

    /**
     * Creates the CDA document using data fetched from the FHIR server.
     *
     * @param patientId   The ID of the patient.
     * @param encounterId The ID of the encounter.
     * @return A temporary file representing the CDA document.
     * @throws JAXBException If there is an error during CDA generation.
     */
    private File createCdaDocument(String patientId, String encounterId) throws JAXBException {
        FHIRClient client = FHIRClient.getInstance(); // Get the singleton FHIR client instance
        Integer idNumber = 1; // The ID number for the CDA document

        // Fetch patient information from FHIR
        Patient fhirPatient = client.getPatientById(patientId);

        // Initialize the CDA document builder
        CdaDocumentBuilder documentBuilder = new CdaDocumentBuilder(idNumber);

        // Add patient details to the CDA document
        documentBuilder.addPatientSection(fhirPatient);

        // Retrieve the logged-in user (practitioner) details
        LoggedUser loggedUser = LoggedUser.getInstance();
        Practitioner practitioner = client.getPractitionerById(loggedUser.getFhirId());

        // Add author information to the CDA document
        documentBuilder.addAuthorSection(practitioner);

        // Add custodian information to the CDA document
        documentBuilder.addCustodianSection();

        // Fetch encounter details from FHIR
        Encounter encounter = client.getEncounterById(encounterId);

        // Add legal authenticator information to the CDA document
        documentBuilder.addLegalAuthenticatorSection(encounter);

        // Add componentOf (context) section to the CDA document
        documentBuilder.addComponentOfSection(encounter);

        // Add additional details (e.g., observations) to the CDA document
        documentBuilder.addComponentSection(encounter);

        // Fetch observations related to the patient
        List<Observation> fhirObservations = client.getObservationsForPatient(patientId);

        // Build the CDA document and return the resulting file
        try {
            File tempFile = documentBuilder.build();
            printFileContent(tempFile); // Print the content of the CDA document for debugging
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to build CDA document: " + e.getMessage());
        }
    }

    /**
     * Reads and prints the content of the CDA document file.
     * This method is primarily used for debugging purposes.
     *
     * @param file The CDA document file to be printed.
     * @throws IOException If an error occurs while reading the file.
     */
    private void printFileContent(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        System.out.println("CDA Document Content:\n" + content);
    }
}
