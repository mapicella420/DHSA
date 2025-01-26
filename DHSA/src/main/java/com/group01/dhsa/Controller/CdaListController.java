package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.Model.LoggedUser;
import com.group01.dhsa.ObserverPattern.EventObservable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CdaListController implements DataReceiver{
    public Button dischargePatientButton;
    @FXML
    private TableView<Document> cdaTable;

    @FXML
    private TableColumn<Document, String> creationDateColumn;

    @FXML
    private TableColumn<Document, String> patientNameColumn;

    @FXML
    private TableColumn<Document, String> patientCFColumn;

    @FXML
    private TableColumn<Document, String> patientBirthColumn;

    @FXML
    private TableColumn<Document, String> authorNameColumn;


    @FXML
    private Button backButton;

    @FXML
    private Button viewDetailsButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchFieldSelector;

    @FXML
    private Button refreshButton;
    private String patientName;


    private final ObservableList<Document> cdaDocuments = FXCollections.observableArrayList();
    private final ToggleGroup toggleGroup = new ToggleGroup(); // Gruppo per consentire una sola selezione alla volta

    private FilteredList<Document> filteredList;

    private static String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "cdaDocuments";

    private Document selectedDocument = null; // Documento selezionato

    private final EventObservable eventManager;

    public CdaListController() {
        this.eventManager = EventManager.getInstance().getEventObservable();
    }

    public static void setMongoUri() {
        if (LoggedUser.getOrganization() != null) {
            if (LoggedUser.getOrganization().equalsIgnoreCase("Other Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27018";
            } else if (LoggedUser.getOrganization().equalsIgnoreCase("My Hospital")){
                MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
            }
        }
    }

    @FXML
    public void initialize() {
        setMongoUri();

        // Configura le colonne con il tipo corretto (Document)
        creationDateColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "creationDate")));
        patientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientName")));
        patientBirthColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientBirthTime")));
        authorNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "authorName")));
        patientCFColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientCF")));

        // Listener per abilitare i pulsanti alla selezione
        cdaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedDocument = newSelection;
                viewDetailsButton.setDisable(false); // Abilita il pulsante "View Details"
                backButton.setDisable(false);        // Abilita il pulsante "Back"
            } else {
                selectedDocument = null;
                viewDetailsButton.setDisable(true); // Disabilita il pulsante
                backButton.setDisable(true);        // Disabilita il pulsante
            }
        });

        // Carica i documenti CDA
        loadCdaDocuments();

        // Configura il filtro
        filteredList = new FilteredList<>(cdaDocuments, p -> true);
        cdaTable.setItems(filteredList);

        // Configura la selezione del campo per la ricerca
        searchFieldSelector.setItems(FXCollections.observableArrayList("Creation Date", "Patient Name", "CF", "Author Name"));
        searchFieldSelector.setValue("Patient Name");

        // Aggiungi un listener per il campo di ricerca
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filteredList.setPredicate(document -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String selectedField = mapField(searchFieldSelector.getValue());
                String lowerCaseFilter = newValue.toLowerCase();
                String fieldValue = getFieldValue(document, selectedField);
                return fieldValue.toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    @Override
    public void receiveData(Map<String, Object> data) {
        setMongoUri();
        this.patientName = (String) data.get("patientName");
        loadCdaData();
    }

    private void loadCdaData() {
        cdaDocuments.clear(); // Pulisce la lista esistente

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Recupera solo i documenti CDA relativi al paziente selezionato
            List<Document> documents = collection.find(new Document("patientName", this.patientName)).into(new java.util.ArrayList<>());

            documents.forEach(doc -> {
                String xmlContent = doc.getString("xmlContent");
                if (xmlContent != null) {
                    // Analizza il contenuto XML del CDA
                    Document parsedDocument = parseCdaXml(xmlContent);
                    parsedDocument.append("_id", doc.getObjectId("_id")); // Include `_id` nel documento parsato
                    cdaDocuments.add(parsedDocument); // Aggiunge il documento alla lista
                }
            });

            System.out.println("[DEBUG] Loaded " + cdaDocuments.size() + " CDA documents for patient: " + patientName);
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading CDA documents for patient '" + patientName + "': " + e.getMessage());
        }
    }



    private String mapField(String selectedField) {
        switch (selectedField) {
            case "CF":
                return "patientCF";
            case "Patient Name":
                return "patientName";
            case "Creation Date":
                return "creationDate";
            case "Author Name":
                return "authorName";
            default:
                return "patientName"; // Default a "Patient Name" se nessun altro campo corrisponde
        }
    }


    private Document parseCdaXml(String xmlContent) {
        Document extractedData = new Document();
        try {
            // Configura il parser DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document xmlDoc = builder.parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));
            xmlDoc.getDocumentElement().normalize();

            // Estrarre campi generali
            Element clinicalDocument = xmlDoc.getDocumentElement();
            Document append = extractedData.append("creationDate", formatDate(getXmlNodeAttribute(clinicalDocument, "effectiveTime", "value")));

            // Estrarre nome e data di nascita del paziente
            Element patientRoleElement = (Element) xmlDoc.getElementsByTagName("patientRole").item(0);
            if (patientRoleElement != null) {
                // Codice fiscale (CF)
                extractedData.append("patientCF", getXmlNodeAttribute(patientRoleElement, "id", "extension"));

                Element patientElement = (Element) patientRoleElement.getElementsByTagName("patient").item(0);
                if (patientElement != null) {
                    String patientGiven = getTextContentByTagName(patientElement, "given");
                    String patientFamily = getTextContentByTagName(patientElement, "family");
                    extractedData.append("patientName", patientGiven + " " + patientFamily);
                    extractedData.append("patientBirthTime", formatDate(getXmlNodeAttribute(patientElement, "birthTime", "value")));
                }
            }

            // Estrarre nome dell'autore
            Element authorElement = (Element) xmlDoc.getElementsByTagName("assignedAuthor").item(0);
            if (authorElement != null) {
                String authorGiven = getTextContentByTagName(authorElement, "given");
                String authorFamily = getTextContentByTagName(authorElement, "family");
                extractedData.append("authorName", authorGiven + " " + authorFamily);
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Error parsing CDA XML: " + e.getMessage());
        }
        return extractedData;
    }


    private String getXmlNodeAttribute(Element parent, String tagName, String attributeName) {
        try {
            NodeList nodeList = parent.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                if (node != null && node.getAttributes() != null) {
                    Node attributeNode = node.getAttributes().getNamedItem(attributeName);
                    return attributeNode != null ? attributeNode.getNodeValue() : "N/A";
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error getting XML node attribute: " + e.getMessage());
        }
        return "N/A";
    }

    private String getTextContentByTagName(Element parent, String tagName) {
        try {
            NodeList nodeList = parent.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error getting text content: " + e.getMessage());
        }
        return "N/A";
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
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            List<Document> documents = collection.find().into(new java.util.ArrayList<>());
            documents.forEach(doc -> {
                System.out.println("[DEBUG] Loaded document: " + doc.toJson()); // Verifica i dati caricati
                String xmlContent = doc.getString("xmlContent");
                if (xmlContent != null) {
                    Document parsedDocument = parseCdaXml(xmlContent);
                    parsedDocument.append("_id", doc.getObjectId("_id")); // Include `_id` nel documento parsato
                    cdaDocuments.add(parsedDocument);
                }
            });
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading CDA documents: " + e.getMessage());
        }
    }


    private String getFieldValue(Document document, String fieldName) {
        return document.containsKey(fieldName) && document.get(fieldName) != null
                ? document.get(fieldName).toString()
                : "N/A";
    }

    @FXML
    private void onBackButtonClick() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "Main Menu");
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




    @FXML
    private void onRefreshButtonClick() {
        setMongoUri();
        cdaDocuments.clear();
        loadCdaDocuments();
        filteredList = new FilteredList<>(cdaDocuments, p -> true);
        cdaTable.setItems(filteredList);
        System.out.println("[DEBUG] Refreshed CDA documents.");
    }

    public void onExportButtonClick(ActionEvent actionEvent) {
    }

    @FXML
    private void onDischargePatientClick() {

        Stage currentStage = (Stage) dischargePatientButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DischargePanelScreen.fxml",currentStage,"Discharge Patient");

        //System.out.println("Discharge Patient button clicked!");
        // Logica per dimettere il paziente
        if (eventManager == null) {
            System.err.println("EventManager is not set!");
            return;
        }
        eventManager.notify("patient_discharge", null);
    }
}
