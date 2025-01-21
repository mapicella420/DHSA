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

public class DicomImporter implements EventListener {

    private static final String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "dicomFiles";

    @Override
    public void handleEvent(String eventType, File file) {
        if ("dicom_upload".equals(eventType)) {
            importDicom(file);
        }
    }

    public void importDicom(File file) {
        try (DicomInputStream dicomInputStream = new DicomInputStream(file)) {
            // Parse the DICOM file
            DicomObject dicomObject = dicomInputStream.readDicomObject();
            System.out.println("Importing DICOM file: " + file.getName());

            // Extract metadata from the DICOM object
            Document dicomMetadata = extractDicomMetadata(dicomObject, file);

            // Load data onto the FHIR server
            ImagingStudy imagingStudy = new ImagingStudy();
            imagingStudy.setId(dicomMetadata.getString("studyInstanceUID")); // You can add more FHIR details

            // Save data to MongoDB
            saveToMongoDB(dicomMetadata);

            // Notify observers
            EventManager.getInstance().getEventObservable().notify("dicom_imported", file);
            System.out.println("Successfully imported DICOM file: " + file.getName());

        } catch (Exception e) {
            System.err.println("Error importing DICOM file: " + e.getMessage());
        }
    }

    private Document extractDicomMetadata(DicomObject dicomObject, File file) {
        Document metadata = new Document();
        metadata.append("fileName", file.getName())
                .append("filePath", file.getAbsolutePath())
                .append("patientId", dicomObject.getString(Tag.PatientID))
                .append("studyInstanceUID", dicomObject.getString(Tag.StudyInstanceUID))
                .append("patientName", dicomObject.getString(Tag.PatientName))
                .append("studyID", dicomObject.getString(Tag.StudyID))
                .append("modality", dicomObject.getString(Tag.Modality))
                .append("studyDate", dicomObject.getString(Tag.StudyDate))
                .append("studyTime", dicomObject.getString(Tag.StudyTime))
                .append("seriesInstanceUID", dicomObject.getString(Tag.SeriesInstanceUID))
                .append("sliceThickness", dicomObject.getString(Tag.SliceThickness))
                .append("pixelSpacing", dicomObject.getString(Tag.PixelSpacing))
                .append("patientBirthDate", dicomObject.getString(Tag.PatientBirthDate))
                .append("patientSex", dicomObject.getString(Tag.PatientSex))
                .append("accessionNumber", dicomObject.getString(Tag.AccessionNumber));
        return metadata;
    }

    private void saveToMongoDB(Document dicomMetadata) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            collection.insertOne(dicomMetadata);

            // Log the saved document
            System.out.println("Saved to MongoDB: " + dicomMetadata.toJson());
        } catch (Exception e) {
            System.err.println("Error saving to MongoDB: " + e.getMessage());
        }
    }
}
