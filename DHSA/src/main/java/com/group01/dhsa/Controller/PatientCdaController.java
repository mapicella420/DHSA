package com.group01.dhsa.Controller;

import com.group01.dhsa.Model.LoggedUser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PatientCdaController  implements DataReceiver{

    @FXML
    private TableView<Document> cdaTable;

    @FXML
    private TableColumn<Document, String> creationDateColumn;

    @FXML
    private TableColumn<Document, String> patientCFColumn;

    @FXML
    private TableColumn<Document, String> patientBirthColumn;

    @FXML
    private TableColumn<Document, String> authorNameColumn;

    @FXML
    private ComboBox<String> searchFieldSelector;

    @FXML
    private TextField searchField;

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button viewDetailsButton;
    private String patientName;

    private final ObservableList<Document> cdaDocuments = FXCollections.observableArrayList();
    private FilteredList<Document> filteredList;

    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "cdaDocuments";

    private Document selectedDocument = null; // Documento selezionato

    public static void setMongoUri() {
        if (LoggedUser.getOrganization().equals("My Hospital")){
            MONGO_URI = "mongodb://admin:mongodb@localhost:27017";

        } else if (LoggedUser.getOrganization().equals("Other Hospital")) {
            MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
        }
    }

    @FXML
    public void initialize() {
        setMongoUri();
        System.out.println("[DEBUG] Initializing PatientCdaController...");

        // Configura le colonne della tabella
        creationDateColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "creationDate")));
        patientCFColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientCF")));
        patientBirthColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientBirthTime")));
        authorNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "authorName")));

        // Configura il listener per la selezione della riga
        cdaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedDocument = newSelection;
                System.out.println("[DEBUG] Selected document: " + selectedDocument.toJson());
                viewDetailsButton.setDisable(false);
            } else {
                selectedDocument = null;
                System.out.println("[DEBUG] No document selected.");
                viewDetailsButton.setDisable(true);
            }
        });

        // Configura i campi di ricerca
        searchFieldSelector.setItems(FXCollections.observableArrayList("Creation Date", "Codice Fiscale", "Author Name"));
        searchFieldSelector.setValue("Codice Fiscale");

        // Carica i documenti CDA
        loadCdaDocuments();

        // Configura il filtro
        filteredList = new FilteredList<>(cdaDocuments, p -> true);
        cdaTable.setItems(filteredList);
        System.out.println("[DEBUG] Table initialized with items.");
    }


    private Document parseCdaXml(String xmlContent) {
        Document extractedData = new Document();
        try {
            System.out.println("[DEBUG] Parsing XML content...");
            // Configura il parser DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // Supporto per gli spazi dei nomi
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document xmlDoc = builder.parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));
            xmlDoc.getDocumentElement().normalize();

            // Estrarre il nome del paziente
            Element patientRoleElement = (Element) xmlDoc.getElementsByTagNameNS("urn:hl7-org:v3", "patientRole").item(0);
            if (patientRoleElement != null) {
                Element patientElement = (Element) patientRoleElement.getElementsByTagNameNS("urn:hl7-org:v3", "patient").item(0);
                if (patientElement != null) {
                    Element givenElement = (Element) patientElement.getElementsByTagNameNS("urn:hl7-org:v3", "given").item(0);
                    Element familyElement = (Element) patientElement.getElementsByTagNameNS("urn:hl7-org:v3", "family").item(0);

                    String givenName = (givenElement != null) ? givenElement.getTextContent().trim() : "N/A";
                    String familyName = (familyElement != null) ? familyElement.getTextContent().trim() : "N/A";
                    String extractedPatientName = givenName + " " + familyName;

                    System.out.println("[DEBUG] Extracted patient name: " + extractedPatientName);

                    // Normalizza il nome del paziente atteso rimuovendo eventuali prefissi come "Mr." o "Mrs."
                    String normalizedPatientName = patientName.replaceAll("^(Mr\\.\\s*|Mrs\\.\\s*)", "").trim();
                    System.out.println("[DEBUG] Normalized patient name: " + normalizedPatientName);

                    // Verifica se il nome del paziente corrisponde
                    if (!extractedPatientName.equalsIgnoreCase(normalizedPatientName)) {
                        System.out.println("[DEBUG] Patient name does not match. Expected: " + normalizedPatientName + ", Found: " + extractedPatientName);
                        return null; // Non corrisponde, ritorna senza estrarre il documento
                    }
                    extractedData.append("patientName", extractedPatientName);
                } else {
                    System.out.println("[DEBUG] No patient element found.");
                    return null; // Nome paziente non trovato, ritorna senza estrarre il documento
                }
            } else {
                System.out.println("[DEBUG] No patientRole element found.");
                return null; // patientRole non trovato, ritorna senza estrarre il documento
            }

            // Estrarre altri dettagli del documento solo se il nome corrisponde
            System.out.println("[DEBUG] Patient name matches. Extracting additional fields...");

            // Estrazione del Codice Fiscale (CF)
            Element idElement = (Element) patientRoleElement.getElementsByTagNameNS("urn:hl7-org:v3", "id").item(0);
            if (idElement != null) {
                String patientCF = idElement.getAttribute("extension");
                System.out.println("[DEBUG] Extracted patient CF: " + patientCF);
                extractedData.append("patientCF", patientCF);
            }

            // Estrazione della data di nascita
            Element birthTimeElement = (Element) xmlDoc.getElementsByTagNameNS("urn:hl7-org:v3", "birthTime").item(0);
            if (birthTimeElement != null) {
                String birthTime = birthTimeElement.getAttribute("value");
                String formattedBirthTime = formatDate(birthTime);
                System.out.println("[DEBUG] Extracted patient birth time: " + formattedBirthTime);
                extractedData.append("patientBirthTime", formattedBirthTime);
            }

            // Estrazione della data di creazione
            Element effectiveTimeElement = (Element) xmlDoc.getElementsByTagNameNS("urn:hl7-org:v3", "effectiveTime").item(0);
            if (effectiveTimeElement != null) {
                String effectiveTime = effectiveTimeElement.getAttribute("value");
                String formattedEffectiveTime = formatDate(effectiveTime);
                System.out.println("[DEBUG] Extracted creation date: " + formattedEffectiveTime);
                extractedData.append("creationDate", formattedEffectiveTime);
            }

            // Nome dell'autore
            Element authorElement = (Element) xmlDoc.getElementsByTagNameNS("urn:hl7-org:v3", "assignedAuthor").item(0);
            if (authorElement != null) {
                Element authorGivenElement = (Element) authorElement.getElementsByTagNameNS("urn:hl7-org:v3", "given").item(0);
                Element authorFamilyElement = (Element) authorElement.getElementsByTagNameNS("urn:hl7-org:v3", "family").item(0);

                String authorGiven = (authorGivenElement != null) ? authorGivenElement.getTextContent().trim() : "N/A";
                String authorFamily = (authorFamilyElement != null) ? authorFamilyElement.getTextContent().trim() : "N/A";

                System.out.println("[DEBUG] Extracted author name: " + authorGiven + " " + authorFamily);
                extractedData.append("authorName", authorGiven + " " + authorFamily);
            } else {
                System.out.println("[DEBUG] No author element found.");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Error parsing CDA XML: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[DEBUG] Extracted document: " + extractedData.toJson());
        return extractedData;
    }

    private String formatDate(String rawDate) {
        try {
            if (rawDate.length() == 8 && rawDate.matches("\\d{8}")) { // Formato `yyyyMMdd`
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date date = inputFormat.parse(rawDate);
                return outputFormat.format(date);
            } else if (rawDate.contains("-")) { // Formato ISO 8601
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(rawDate, inputFormatter).format(outputFormatter);
            } else { // Formato `yyyyMMddHHmmss`
                try {
                    // Prova il primo formato (incluso il fuso orario)
                    SimpleDateFormat inputFormat1 = new SimpleDateFormat("yyyyMMddHHmmssZ");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = inputFormat1.parse(rawDate);
                    return outputFormat.format(date);
                } catch (Exception e1) {
                    try {
                        // Prova il secondo formato (senza fuso orario)
                        SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date date = inputFormat2.parse(rawDate);
                        return outputFormat.format(date);
                    } catch (Exception e2) {
                        // Se entrambi i formati falliscono, restituisci un messaggio di errore
                        return "Formato data non valido";
                    }
                }
            }
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    private void loadCdaDocuments() {
        cdaDocuments.clear(); // Pulisce la lista
        System.out.println("[DEBUG] Clearing existing documents...");

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Normalizza il nome del paziente
            String normalizedPatientName = patientName.replaceAll("^(Mr\\.\\s*|Mrs\\.\\s*)", "").trim();
            System.out.println("[DEBUG] Normalized patient name for matching: " + normalizedPatientName);

            // Filtra i documenti CDA
            List<Document> documents = collection.find().into(new java.util.ArrayList<>());
            System.out.println("[DEBUG] Retrieved " + documents.size() + " documents from MongoDB.");

            for (Document doc : documents) {
                String xmlContent = doc.getString("xmlContent");
                if (xmlContent != null) {
                    Document parsedDocument = parseCdaXml(xmlContent);

                    // Controlla se il documento è valido
                    if (parsedDocument == null) {
                        System.out.println("[DEBUG] Skipping document due to name mismatch or invalid content.");
                        continue; // Salta i documenti non validi
                    }

                    // Verifica il nome del paziente
                    String extractedPatientName = parsedDocument.getString("patientName");
                    System.out.println("[DEBUG] Extracted patient name: " + extractedPatientName);

                    if (isNameMatch(extractedPatientName,normalizedPatientName)) {
                        parsedDocument.append("_id", doc.getObjectId("_id")); // Include `_id` nel documento parsato
                        cdaDocuments.add(parsedDocument);
                        System.out.println("[DEBUG] Added document for patient: " + extractedPatientName);
                    } else {
                        System.out.println("[DEBUG] Skipping document due to name mismatch. Expected: " + normalizedPatientName + ", Found: " + extractedPatientName);
                    }
                } else {
                    System.out.println("[DEBUG] Skipping document without XML content.");
                }
            }

            System.out.println("[DEBUG] Total documents for patient " + normalizedPatientName + ": " + cdaDocuments.size());
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading CDA documents: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[DEBUG] Total documents loaded: " + cdaDocuments.size());
    }


    private boolean isNameMatch(String dbPatientName, String inputPatientName) {

        String[] dbParts = dbPatientName.split(" ");
        String[] inputParts = inputPatientName.split(" ");


        if (dbParts.length <= inputParts.length) {
            for (String dbPart : dbParts) {
                if (!Arrays.asList(inputParts).contains(dbPart)) {
                    return false;
                }
            }
            return true;
        }


        for (String inputPart : inputParts) {
            if (!Arrays.asList(dbParts).contains(inputPart)) {
                return false;
            }
        }
        return true;
    }




    private String getFieldValue(Document document, String fieldName) {
        return document.containsKey(fieldName) && document.get(fieldName) != null
                ? document.get(fieldName).toString()
                : "N/A";
    }

    @FXML
    private void onBackButtonClick() {
        // Torna alla schermata precedente
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/PatientPanelScreen.fxml", currentStage, "Patient Panel");
    }

    @FXML
    private void onRefreshButtonClick() {
        // Torna alla schermata precedente
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/PatientCdaScreen.fxml", currentStage, "Patient Panel");
    }

    @FXML
    private void onViewDetailsClick() {
        setMongoUri();
        if (selectedDocument != null) {
            if (!selectedDocument.containsKey("_id") || selectedDocument.get("_id") == null) {
                showAlert("Error", "Invalid Document", "The selected document does not have a valid ID.");
                return;
            }

            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                // Connettiti al database e alla collezione
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

                // Recupera l'ID del documento selezionato
                ObjectId documentId = selectedDocument.getObjectId("_id");
                System.out.println("[DEBUG] Selected document ID: " + documentId);

                // Recupera il documento completo da MongoDB
                Document fullDocument = collection.find(new Document("_id", documentId)).first();

                if (fullDocument == null || !fullDocument.containsKey("xmlContent")) {
                    throw new IllegalArgumentException("The selected document does not contain valid XML content.");
                }

                // Ottieni il contenuto XML dal documento
                String xmlContent = fullDocument.getString("xmlContent");

                // Crea un file temporaneo per il CDA
                File cdaFile = File.createTempFile("CDA_Preview_", ".xml");
                cdaFile.deleteOnExit(); // Il file viene eliminato alla chiusura dell'applicazione
                try (FileWriter writer = new FileWriter(cdaFile)) {
                    writer.write(xmlContent);
                }

                // Cambia schermata
                Stage currentStage = (Stage) viewDetailsButton.getScene().getWindow();
                ChangeScreen screenChanger = new ChangeScreen();
                Object controller = screenChanger.switchScreen(
                        "/com/group01/dhsa/View/CdaPreviewScreen.fxml",
                        currentStage,
                        "CDA Preview"
                );

                // Passa il file al controller della schermata di anteprima
                if (controller instanceof CdaPreviewController) {
                    ((CdaPreviewController) controller).setCdaFile(cdaFile);
                    System.out.println("[DEBUG] CDA file passed to CdaPreviewController.");
                } else {
                    System.err.println("[ERROR] The controller is not an instance of CdaPreviewController.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("[ERROR] " + e.getMessage());
                showAlert("Error", "Invalid Document", e.getMessage());
            } catch (Exception e) {
                System.err.println("[ERROR] Error while transitioning to CDA Preview screen: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("[ERROR] No document selected.");
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void receiveData(Map<String, Object> data) {
        setMongoUri();
        if (data != null && data.containsKey("patientName")) {
            this.patientName = (String) data.get("patientName");
            loadCdaDocuments();
            System.out.println(patientName);// Puoi aggiungere qui logica per utilizzare `patientName`
        }
    }

}
