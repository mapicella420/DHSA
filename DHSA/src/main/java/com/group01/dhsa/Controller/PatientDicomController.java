package com.group01.dhsa.Controller;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PatientDicomController implements DataReceiver {

    @FXML
    private TableView<Document> dicomTable;

    @FXML
    private TableColumn<Document, String> fileNameColumn;

    @FXML
    private TableColumn<Document, String> patientNameColumn;

    @FXML
    private TableColumn<Document, String> modalityColumn;

    @FXML
    private TableColumn<Document, String> studyDateColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchFieldSelector;

    @FXML
    private Button backButton;


    @FXML
    private Button viewDetailsButton;

    private final ObservableList<Document> dicomFiles = FXCollections.observableArrayList();
    private FilteredList<Document> filteredList;

    private static final String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "dicomFiles";

    private Document selectedDocument = null;
    private String patientName;

    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Initializing PatientDicomController...");

        // Configurazione delle colonne
        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "fileName")));
        patientNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientName")));
        modalityColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "modality")));
        studyDateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(getFieldValue(data.getValue(), "studyDate"))));

        // Listener per la selezione della riga nella tabella
        dicomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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

        // Configurazione del filtro per la ricerca
        searchFieldSelector.setItems(FXCollections.observableArrayList("File Name", "Patient Name", "Modality", "Study Date"));
        searchFieldSelector.setValue("Patient Name");

        // Caricamento dei file DICOM
        loadDicomFiles();

        // Impostazione del filtro sui dati
        filteredList = new FilteredList<>(dicomFiles, p -> true);
        dicomTable.setItems(filteredList);

        // Listener per il campo di ricerca
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String selectedField = mapField(searchFieldSelector.getValue());
            filteredList.setPredicate(file -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String fieldValue = getFieldValue(file, selectedField);
                return fieldValue.toLowerCase().contains(newValue.toLowerCase());
            });
        });
    }


    private String mapField(String selectedField) {
        switch (selectedField) {
            case "File Name":
                return "fileName";
            case "Patient Name":
                return "patientName";
            case "Modality":
                return "modality";
            case "Study Date":
                return "studyDate";
            default:
                return "patientName";
        }
    }

    private void loadDicomFiles() {
        dicomFiles.clear();
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            List<Document> documents = collection.find(new Document("patientName", patientName)).into(new java.util.ArrayList<>());

            for (Document doc : documents) {
                dicomFiles.add(doc);
            }

            System.out.println("[DEBUG] Loaded " + dicomFiles.size() + " DICOM files for patient: " + patientName);
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading DICOM files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getFieldValue(Document document, String fieldName) {
        return document.containsKey(fieldName) && document.get(fieldName) != null
                ? document.get(fieldName).toString()
                : "N/A";
    }

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = inputFormat.parse(rawDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    @FXML
    private void onBackButtonClick() {
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/PatientPanel.fxml", currentStage, "Patient Panel");
    }

    @FXML
    private void onRefreshButtonClick() {
        loadDicomFiles();
    }

    @FXML
    private void onViewDetailsClick() {
        if (selectedDocument != null) {
            try {
                String filePath = getFieldValue(selectedDocument, "filePath");
                System.out.println("[DEBUG] Opening DICOM file: " + filePath);

                // Implementa il cambio schermata e il passaggio dei dati per il file DICOM

            } catch (Exception e) {
                System.err.println("[ERROR] Error opening DICOM file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("[ERROR] No document selected.");
        }
    }

    @Override
    public void receiveData(Map<String, Object> data) {
        if (data != null && data.containsKey("patientName")) {
            this.patientName = (String) data.get("patientName");
            loadDicomFiles();
            System.out.println("[DEBUG] Patient name set: " + patientName);
        }
    }

    @FXML
    private void onViewImageClick() {
        if (selectedDocument != null) {
            System.out.println("[DEBUG] Selected file: " + selectedDocument.toJson());

            Stage stage = (Stage) viewDetailsButton.getScene().getWindow();
            ChangeScreen screenChanger = new ChangeScreen();

            Object controller = screenChanger.switchScreen(
                    "/com/group01/dhsa/View/DicomViewScreen.fxml", stage, "View DICOM");

            if (controller instanceof DicomViewController) {
                System.out.println("[DEBUG] Imposto il file DICOM nel DicomViewController.");
                ((DicomViewController) controller).setDicomFile(selectedDocument);
            } else {
                System.err.println("[ERROR] Il controller non è un'istanza di DicomViewController.");
            }
        } else {
            System.err.println("[ERROR] Nessun file selezionato.");
        }
    }

}
