package com.group01.dhsa.Controller;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DicomListController {

    @FXML
    private TableView<Document> dicomTable;

    @FXML
    private TableColumn<Document, Boolean> selectColumn;

    @FXML
    private TableColumn<Document, String> dicomIdColumn;

    @FXML
    private TableColumn<Document, String> fileNameColumn;

    @FXML
    private TableColumn<Document, String> patientIdColumn;

    @FXML
    private TableColumn<Document, String> studyIdColumn;

    @FXML
    private TableColumn<Document, String> studyDateColumn;

    @FXML
    private TableColumn<Document, String> studyTimeColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button viewImageButton;

    private final ObservableList<Document> dicomFiles = FXCollections.observableArrayList();
    private final ToggleGroup toggleGroup = new ToggleGroup(); // Gruppo per consentire una sola selezione alla volta

    private static final String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "dicomFiles";

    private Document selectedFile = null; // Documento selezionato

    @FXML
    public void initialize() {
        // Configura le colonne della tabella
        dicomIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(dicomFiles.indexOf(data.getValue()) + 1)));
        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "fileName")));
        patientIdColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientId")));
        studyIdColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "studyID")));
        studyDateColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "studyDate")));
        studyTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "studyTime")));

        // Configura la colonna di selezione con un RadioButton
        // Configura la colonna di selezione con un RadioButton personalizzato
        selectColumn.setCellFactory(column -> new TableCell<>() {
            private final RadioButton radioButton = new RadioButton();

            {
                radioButton.setToggleGroup(toggleGroup);
                radioButton.getStyleClass().add("checkbox"); // Aggiungi la classe di stile
                radioButton.setOnAction(event -> handleSelection(getIndex()));
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(radioButton);
                    radioButton.setSelected(selectedFile == dicomFiles.get(getIndex()));
                }
            }
        });


        loadDicomFiles();
        dicomTable.setItems(dicomFiles);

        // Disabilita il pulsante View DICOM inizialmente
        viewImageButton.setDisable(true);

        System.out.println("[DEBUG] Initialization completed!");
    }

    private void loadDicomFiles() {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            List<Document> files = collection.find().into(new java.util.ArrayList<>());
            files.forEach(doc -> System.out.println("[DEBUG] Loaded document: " + doc.toJson()));
            dicomFiles.addAll(files);
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading DICOM files: " + e.getMessage());
        }
    }

    private String getFieldValue(Document document, String fieldName) {
        return document.containsKey(fieldName) && document.get(fieldName) != null
                ? document.get(fieldName).toString()
                : "N/A";
    }

    private void handleSelection(int index) {
        if (index >= 0 && index < dicomFiles.size()) {
            selectedFile = dicomFiles.get(index);
            viewImageButton.setDisable(false); // Abilita il pulsante
            System.out.println("[DEBUG] Selected file: " + selectedFile.toJson());
        } else {
            selectedFile = null;
            viewImageButton.setDisable(true); // Disabilita il pulsante
            System.out.println("[DEBUG] No file selected.");
        }
    }

    @FXML
    private void onBackButtonClick() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "Doctor Panel");
    }

    @FXML
    private void onViewImageClick() {
        if (selectedFile != null) {
            System.out.println("[DEBUG] Selected file: " + selectedFile.toJson());

            Stage stage = (Stage) viewImageButton.getScene().getWindow();
            ChangeScreen screenChanger = new ChangeScreen();

            Object controller = screenChanger.switchScreen(
                    "/com/group01/dhsa/View/DicomViewScreen.fxml", stage, "View DICOM");

            if (controller instanceof DicomViewController) {
                System.out.println("[DEBUG] Imposto il file DICOM nel DicomViewController.");
                ((DicomViewController) controller).setDicomFile(selectedFile);
            } else {
                System.err.println("[ERROR] Il controller non è un'istanza di DicomViewController.");
            }
        } else {
            System.err.println("[ERROR] Nessun file selezionato.");
        }
    }



}
