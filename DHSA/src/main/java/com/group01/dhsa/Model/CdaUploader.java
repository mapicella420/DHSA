package com.group01.dhsa.Model;

import com.group01.dhsa.ObserverPattern.EventListener;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

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
            // Leggi il contenuto del file CDA
            String xmlContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

            // Crea un nuovo ObjectId per il documento
            ObjectId objectId = new ObjectId();

            // Crea il documento MongoDB con `_id` e `xmlContent`
            Document xmlDocument = new Document()
                    .append("_id", objectId) // Assegna un ObjectId manualmente
                    .append("xmlContent", xmlContent);

            // Connessione a MongoDB e salvataggio del documento
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

                // Inserisce il documento nella collezione
                collection.insertOne(xmlDocument);
                System.out.println("CDA document successfully saved to MongoDB with ID: " + objectId);
            }
        } catch (IOException e) {
            // Gestione degli errori di lettura
            System.err.println("Error uploading CDA to MongoDB: " + e.getMessage());
        }
    }

}
