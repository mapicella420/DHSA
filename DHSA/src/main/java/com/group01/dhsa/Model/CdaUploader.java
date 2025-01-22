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

public class CdaUploader implements EventListener {
    private static final String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "cdaDocuments";

    @Override
    public void handleEvent(String eventType, File file) {
        if ("cda_upload".equals(eventType)) {
            uploadCdaToMongo(file);
        }
    }

    private void uploadCdaToMongo(File file) {
        try {
            String xmlContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            Document xmlDocument = new Document().append("xmlContent", xmlContent);

            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
                collection.insertOne(xmlDocument);
                System.out.println("CDA document successfully saved to MongoDB.");
            }
        } catch (IOException e) {
            System.err.println("Error uploading CDA to MongoDB: " + e.getMessage());
        }
    }
}
