package com.group01.dhsa.Model;

import com.group01.dhsa.ObserverPattern.EventListener;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The `CdaUploader` class listens for "cda_upload" events, reads CDA files,
 * and uploads their content to a MongoDB collection.
 */
public class CdaUploader implements EventListener {
    // MongoDB connection configuration
    private static final String MONGO_URI = "mongodb://admin:mongodb@localhost:27017"; // MongoDB URI
    private static final String DATABASE_NAME = "medicalData"; // Database name
    private static final String COLLECTION_NAME = "cdaDocuments"; // Collection name for CDA documents

    /**
     * Handles events triggered by the EventManager.
     * If the event type is "cda_upload", it processes the provided file.
     *
     * @param eventType The type of the event (e.g., "cda_upload").
     * @param file      The file associated with the event.
     */
    @Override
    public void handleEvent(String eventType, File file) {
        if ("cda_upload".equals(eventType)) {
            uploadCdaToMongo(file); // Upload the CDA file to MongoDB
        }
    }

    /**
     * Uploads the content of a CDA file to the MongoDB database.
     *
     * @param file The CDA file to be uploaded.
     */
    private void uploadCdaToMongo(File file) {
        try {
            // Read the content of the CDA file into a string
            String xmlContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

            // Create a MongoDB document with the XML content
            Document xmlDocument = new Document().append("xmlContent", xmlContent);

            // Connect to MongoDB, access the specified database and collection
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

                // Insert the document into the collection
                collection.insertOne(xmlDocument);
                System.out.println("CDA document successfully saved to MongoDB.");
            }
        } catch (IOException e) {
            // Handle file reading errors
            System.err.println("Error uploading CDA to MongoDB: " + e.getMessage());
        }
    }
}
