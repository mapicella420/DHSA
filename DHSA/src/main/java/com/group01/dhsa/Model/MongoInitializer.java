package com.group01.dhsa.Model;

import com.mongodb.client.*;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;

public class MongoInitializer {

    public static void initializeDatabase() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("data_app");

            if (!database.listCollectionNames().into(new java.util.ArrayList<>()).contains("users")) {
                database.createCollection("users");
                System.out.println("Collection 'users' created.");
            }

            MongoCollection<Document> usersCollection = database.getCollection("users");
            Document doctor = usersCollection.find(new Document("username", "doctor")).first();

            if (doctor != null) {
                System.out.println("Doctor user already exists.");
            } else {
                String adminPasswordHash = BCrypt.hashpw("doc123", BCrypt.gensalt());

                Document adminUser = new Document("username", "doctor")
                        .append("passwordHash", adminPasswordHash)
                        .append("role", "doctor")
                        .append("createdAt", new Date());
                usersCollection.insertOne(adminUser);
                System.out.println("Doctor user created.");
            }
        }
        try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27018")) {
            MongoDatabase database = mongoClient.getDatabase("data_app");

            if (!database.listCollectionNames().into(new java.util.ArrayList<>()).contains("users")) {
                database.createCollection("users");
                System.out.println("Collection 'users' created.");
            }

            MongoCollection<Document> usersCollection = database.getCollection("users");
            Document doctor = usersCollection.find(new Document("username", "doctor-other")).first();

            if (doctor != null) {
                System.out.println("Doctor user already exists.");
            } else {
                String adminPasswordHash = BCrypt.hashpw("doc321", BCrypt.gensalt());

                Document adminUser = new Document("username", "doctor-other")
                        .append("passwordHash", adminPasswordHash)
                        .append("role", "doctor")
                        .append("createdAt", new Date());
                usersCollection.insertOne(adminUser);
                System.out.println("Doctor-other user created.");
            }
        }
    }
}
