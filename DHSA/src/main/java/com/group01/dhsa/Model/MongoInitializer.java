package com.group01.dhsa.Model;

import com.mongodb.client.*;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;

/**
 * The `MongoInitializer` class is responsible for initializing MongoDB databases
 * by creating collections and ensuring predefined users exist.
 */
public class MongoInitializer {

    /**
     * Initializes the MongoDB databases, creates necessary collections,
     * and sets up predefined users if they do not already exist.
     */
    public static void initializeDatabase() {
        // First MongoDB server connection (port 27017)
        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("data_app");

            // Check if the 'users' collection exists, create it if not
            if (!database.listCollectionNames().into(new java.util.ArrayList<>()).contains("users")) {
                database.createCollection("users");
                System.out.println("Collection 'users' created.");
            }

            // Access the 'users' collection
            MongoCollection<Document> usersCollection = database.getCollection("users");

            // Check if the 'doctor' user exists
            Document doctor = usersCollection.find(new Document("username", "doctor")).first();
            if (doctor != null) {
                System.out.println("Doctor user already exists.");
            } else {
                // Hash the default password using BCrypt
                String adminPasswordHash = BCrypt.hashpw("doc123", BCrypt.gensalt());
                String doctorFhirId = "5db62284-9e52-3c8e-bde0-53d81bd39963"; // FHIR ID for the doctor

                // Create a new document for the 'doctor' user
                Document adminUser = new Document("username", "doctor")
                        .append("passwordHash", adminPasswordHash)
                        .append("role", "doctor")
                        .append("fhirID", doctorFhirId)
                        .append("organization", "My Hospital")
                        .append("createdAt", new Date());

                // Insert the user into the collection
                usersCollection.insertOne(adminUser);
                System.out.println("Doctor user created.");
            }
        }

        // Second MongoDB server connection (port 27018)
        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27018")) {
            MongoDatabase database = mongoClient.getDatabase("data_app");

            // Check if the 'users' collection exists, create it if not
            if (!database.listCollectionNames().into(new java.util.ArrayList<>()).contains("users")) {
                database.createCollection("users");
                System.out.println("Collection 'users' created.");
            }

            // Access the 'users' collection
            MongoCollection<Document> usersCollection = database.getCollection("users");

            // Check if the 'doctor-other' user exists
            Document doctor = usersCollection.find(new Document("username", "doctor-other")).first();
            if (doctor != null) {
                System.out.println("Doctor-other user already exists.");
            } else {
                // Hash the default password using BCrypt
                String adminPasswordHash = BCrypt.hashpw("doc321", BCrypt.gensalt());
                String otherDocFhirId = "7a1b6e24-50b6-37e5-bfdd-3a4d1f87b978"; // FHIR ID for doctor-other

                // Create a new document for the 'doctor-other' user
                Document adminUser = new Document("username", "doctor-other")
                        .append("passwordHash", adminPasswordHash)
                        .append("role", "doctor")
                        .append("fhirID", otherDocFhirId)
                        .append("organization", "Other Hospital")
                        .append("createdAt", new Date());

                // Insert the user into the collection
                usersCollection.insertOne(adminUser);
                System.out.println("Doctor-other user created.");
            }
        }
    }
}
