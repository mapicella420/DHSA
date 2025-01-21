package com.group01.dhsa.Controller;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchFieldSelector;

    @FXML
    private Button refreshButton;

    private final ObservableList<Document> dicomFiles = FXCollections.observableArrayList();
    private final ToggleGroup toggleGroup = new ToggleGroup(); // Gruppo per consentire una sola selezione alla volta

    private FilteredList<Document> filteredList;

    private static final String MONGO_URI = "mongodb://admin:mongodb@localhost:27017";
    private static final String DATABASE_NAME = "medicalData";
    private static final String COLLECTION_NAME = "dicomFiles";

    private Document selectedFile = null; // Documento selezionato

    @FXML
    public void initialize() {
        selectColumn.setPrefWidth(45);
        selectColumn.setResizable(false);
        dicomIdColumn.setPrefWidth(35);
        dicomIdColumn.setResizable(false);
        fileNameColumn.setPrefWidth(200);

        // Configura il comportamento di adattamento automatico per le altre colonne
        patientIdColumn.setPrefWidth(Control.USE_COMPUTED_SIZE);
        studyIdColumn.setPrefWidth(Control.USE_COMPUTED_SIZE);
        studyDateColumn.setPrefWidth(Control.USE_COMPUTED_SIZE);
        studyTimeColumn.setPrefWidth(Control.USE_COMPUTED_SIZE);

        // Configura il ridimensionamento automatico della tabella
        dicomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configura la colonna di selezione con RadioButton personalizzati
        selectColumn.setCellFactory(column -> new TableCell<>() {
            private final RadioButton radioButton = new RadioButton();

            {
                radioButton.setToggleGroup(toggleGroup);
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

        // Configura i dati delle altre colonne
        dicomIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(dicomFiles.indexOf(data.getValue()) + 1)));
        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "fileName")));
        patientIdColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientId")));
        studyIdColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "studyID")));
        studyDateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(getFieldValue(data.getValue(), "studyDate"))));
        studyTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(formatTime(getFieldValue(data.getValue(), "studyTime"))));

        // Carica i file DICOM
        loadDicomFiles();

        // Configura il filtro
        filteredList = new FilteredList<>(dicomFiles, p -> true);
        dicomTable.setItems(filteredList);

        // Configura la selezione del campo per la ricerca
        searchFieldSelector.setItems(FXCollections.observableArrayList("Patient Name", "Patient ID", "Study ID", "File Name", "Study Date"));
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
                if ("studyDate".equals(selectedField)) {
                    fieldValue = formatDate(fieldValue).toLowerCase();
                }
                return fieldValue.contains(lowerCaseFilter);
            });
        });

        // Disabilita inizialmente il pulsante View DICOM
        viewImageButton.setDisable(true);

        System.out.println("[DEBUG] Initialization completed!");
    }



    private String mapField(String selectedField) {
        switch (selectedField) {
            case "Patient Name":
                return "patientName";
            case "Patient ID":
                return "patientId";
            case "Study ID":
                return "studyID";
            case "File Name":
                return "fileName";
            case "Study Date":
                return "studyDate";
            default:
                return "patientName";
        }
    }

    private String formatTime(String time) {
        try {
            if (time == null || time.length() != 6) {
                return "Invalid Time";
            }
            String hours = time.substring(0, 2);
            String minutes = time.substring(2, 4);
            String seconds = time.substring(4, 6);
            return hours + ":" + minutes + ":" + seconds;
        } catch (Exception e) {
            return "Invalid Time";
        }
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

    private String formatDate(String date) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(date, inputFormatter).format(outputFormatter);
        } catch (Exception e) {
            return "Invalid Date";
        }
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

    public void onRefreshButtonClick() {
        System.out.println("Navigating to Upload CSV screen...");
        Stage currentStage = (Stage) refreshButton.getScene().getWindow(); // Recupera lo Stage dalla scena corrente
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DicomListScreen.fxml", currentStage, "Upload CSV");
    }
}
