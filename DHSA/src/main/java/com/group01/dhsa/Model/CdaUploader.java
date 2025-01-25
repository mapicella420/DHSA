package com.group01.dhsa.Model;

import com.group01.dhsa.ObserverPattern.EventListener;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;
import org.bson.Document; // Importa correttamente il tipo Document di BSON
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.file.*;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * The `CdaUploader` class listens for "cda_upload" events, reads CDA files,
 * and uploads their content to a MongoDB collection.
 */
public class CdaUploader implements EventListener {
    // MongoDB connection configuration

    private static final String DATABASE_NAME = "medicalData"; // Database name
    private static final String COLLECTION_NAME = "cdaDocuments"; // Collection name for CDA documents

    public static String mongodbURI(){

        if (LoggedUser.getOrganization().equals("My Hospital")) {
            return "mongodb://admin:mongodb@localhost:27017";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            return "mongodb://admin:mongodb@localhost:27018";
        }
        return "";
    }
    private static final String MONGO_URI = mongodbURI(); // MongoDB URI
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

            // Analizza l'XML per estrarre informazioni
            Map<String, String> patientData = new HashMap<>();
            extractPatientInfo(xmlContent, patientData);

            if (!patientData.containsKey("patientGiven") || !patientData.containsKey("patientFamily")) {
                System.err.println("[ERROR] Patient name not found in CDA document: " + file.getName());
                return;
            }

            String patientName = patientData.get("patientGiven") + " " + patientData.get("patientFamily");

            // Crea il documento MongoDB
            Document xmlDocument = new Document() // Utilizza org.bson.Document
                    .append("_id", new ObjectId())
                    .append("xmlContent", xmlContent)
                    .append("patientName", patientName)
                    .append("patientData", patientData);

            // Connessione a MongoDB e salvataggio del documento
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

                collection.insertOne(xmlDocument); // Inserisci il documento nella collezione
                System.out.println("[DEBUG] CDA document saved: " + xmlDocument);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error uploading CDA: " + e.getMessage());
        }
    }

    private void extractPatientInfo(String xmlContent, Map<String, String> data) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xmlContent))); // Corretto

            Element patientRoleElement = (Element) doc.getElementsByTagName("patientRole").item(0);
            Element patientElement = (Element) patientRoleElement.getElementsByTagName("patient").item(0);
            Element addressElement = (Element) patientRoleElement.getElementsByTagName("addr").item(0);

            data.put("patientId", getAttributeValue(patientRoleElement, "id", "extension"));
            data.put("patientGiven", getTextContentByTagName(patientElement, "given"));
            data.put("patientFamily", getTextContentByTagName(patientElement, "family"));
            data.put("patientGender", getAttributeValue(patientElement, "administrativeGenderCode", "displayName"));
            data.put("patientBirthTime", getAttributeValue(patientElement, "birthTime", "value"));
            data.put("patientAddress", getTextContentByTagName(addressElement, "streetAddressLine"));
            data.put("patientCountry", getTextContentByTagName(addressElement, "country"));
            data.put("patientState", getTextContentByTagName(addressElement, "state"));
            data.put("patientCity", getTextContentByTagName(addressElement, "city"));

            System.out.println("[DEBUG] Extracted patient data: " + data);
        } catch (Exception e) {
            System.err.println("[ERROR] Error extracting patient info: " + e.getMessage());
        }
    }

    private String getTextContentByTagName(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    private String getAttributeValue(Element element, String tagName, String attributeName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element tagElement = (Element) nodes.item(0);
            return tagElement.getAttribute(attributeName);
        }
        return null;
    }
}
