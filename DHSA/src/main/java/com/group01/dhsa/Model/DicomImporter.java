package com.group01.dhsa.Model;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventListener;
import org.bson.Document;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.hl7.fhir.r5.model.ImagingStudy;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.File;

/**
 * The `DicomImporter` class handles the import of DICOM files.
 * It implements the `EventListener` interface to observe "dicom_upload" events and process the files accordingly.
 */
public class DicomImporter implements EventListener {

    // MongoDB connection details

    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "dicomFiles";

    /**
     * Handles events received by the `DicomImporter`.
     * If the event type is "dicom_upload", it triggers the DICOM file import process.
     *
     */

    public static String mongodbURI(){
        if (LoggedUser.getOrganization().equals("My Hospital")){
            return "mongodb://admin:mongodb@localhost:27017";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            return "mongodb://admin:mongodb@localhost:27018";
        }
        return "";
    }
    private static final String MONGO_URI = mongodbURI(); // MongoDB URI

    @Override
    public void handleEvent(String eventType, File file) {
        if ("dicom_upload".equals(eventType)) {
            importDicom(file); // Start the DICOM import process
        }
    }

    /**
     * Imports a DICOM file by extracting its metadata, saving it to MongoDB, and notifying observers.
     *
     * @param file The DICOM file to be imported.
     */
    public void importDicom(File file) {
        try (DicomInputStream dicomInputStream = new DicomInputStream(file)) {
            // Parse the DICOM file to extract metadata
            DicomObject dicomObject = dicomInputStream.readDicomObject();
            System.out.println("Importing DICOM file: " + file.getName());

            // Extract metadata from the DICOM object
            Document dicomMetadata = extractDicomMetadata(dicomObject, file);

            // Map DICOM data to a FHIR ImagingStudy resource (partial example)
            ImagingStudy imagingStudy = new ImagingStudy();
            imagingStudy.setId(dicomMetadata.getString("studyInstanceUID"));

            // Save metadata to MongoDB
            saveToMongoDB(dicomMetadata);

            // Notify observers of successful import
            EventManager.getInstance().getEventObservable().notify("dicom_imported", file);
            System.out.println("Successfully imported DICOM file: " + file.getName());
        } catch (Exception e) {
            // Handle exceptions during the DICOM import process
            System.err.println("Error importing DICOM file: " + e.getMessage());
        }
    }

    /**
     * Extracts metadata from a DICOM object and returns it as a MongoDB document.
     *
     * @param dicomObject The parsed DICOM object containing the file's metadata.
     * @param file        The original DICOM file.
     * @return A MongoDB `Document` containing the extracted metadata.
     */
    private Document extractDicomMetadata(DicomObject dicomObject, File file) {
        Document metadata = new Document();
        metadata.append("fileName", file.getName())
                .append("filePath", file.getAbsolutePath())
                .append("patientName", dicomObject.getString(Tag.PatientName)) // Patient's Name
                .append("patientId", dicomObject.getString(Tag.PatientID))
                .append("modality", dicomObject.getString(Tag.Modality)) // Imaging modality (e.g., CT, MRI)
                .append("studyInstanceUID", dicomObject.getString(Tag.StudyInstanceUID))
                .append("studyID", dicomObject.getString(Tag.StudyID))
                .append("studyDate", dicomObject.getString(Tag.StudyDate))
                .append("studyTime", dicomObject.getString(Tag.StudyTime)) // Study time
                .append("seriesInstanceUID", dicomObject.getString(Tag.SeriesInstanceUID)) // Series UID
                .append("sliceThickness", dicomObject.getString(Tag.SliceThickness)) // Slice thickness
                .append("pixelSpacing", dicomObject.getString(Tag.PixelSpacing)) // Pixel spacing
                .append("patientBirthDate", dicomObject.getString(Tag.PatientBirthDate)) // Patient's birth date
                .append("patientSex", dicomObject.getString(Tag.PatientSex)) // Patient's sex
                .append("accessionNumber", dicomObject.getString(Tag.AccessionNumber)); // Accession number
        return metadata;
    }

    /**
     * Saves the extracted DICOM metadata to a MongoDB collection.
     *
     * @param dicomMetadata The metadata document to be saved.
     */
    private void saveToMongoDB(Document dicomMetadata) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            // Connect to the MongoDB database and collection
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Insert the metadata document into the collection
            collection.insertOne(dicomMetadata);

            // Log the saved document for verification
            System.out.println("Saved to MongoDB: " + dicomMetadata.toJson());
        } catch (Exception e) {
            // Handle errors that occur during database interaction
            System.err.println("Error saving to MongoDB: " + e.getMessage());
        }
    }
}
