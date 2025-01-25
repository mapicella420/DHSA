package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DicomListController implements DataReceiver {

    @FXML
    private TableView<Document> dicomTable;


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

        // Assegna un listener alla tabella per abilitare il pulsante View DICOM
        dicomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedFile = newSelection;
            viewImageButton.setDisable(selectedFile == null); // Abilita solo se è selezionata una riga
        });

        System.out.println("[DEBUG] Initialization completed!");
    }

    private String patientName;

    @Override
    public void receiveData(Map<String, Object> data) {
        this.patientName = (String) data.get("patientName");
        loadDicomData();
    }

    private void loadDicomData() {
        try {
            // Filter DICOM documents for the selected patient
            List<Document> filteredResources = dicomFiles.stream()
                    .filter(doc -> patientName.equalsIgnoreCase(getFieldValue(doc, "patientName")))
                    .collect(Collectors.toList());

            if (filteredResources.isEmpty()) {
                System.out.println("[DEBUG] No DICOM resources found for patient: " + patientName);
                return;
            }

            // Clear the table to ensure no duplicate columns are added
            dicomTable.getColumns().clear();

            // Explicitly define the columns
            TableColumn<Document, String> dicomIdColumn = new TableColumn<>("ID");
            dicomIdColumn.setCellValueFactory(data -> new SimpleStringProperty(
                    String.valueOf(dicomFiles.indexOf(data.getValue()) + 1)));

            TableColumn<Document, String> fileNameColumn = new TableColumn<>("File Name");
            fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "fileName")));

            TableColumn<Document, String> patientIdColumn = new TableColumn<>("Patient ID");
            patientIdColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "patientId")));

            TableColumn<Document, String> studyIdColumn = new TableColumn<>("Study ID");
            studyIdColumn.setCellValueFactory(data -> new SimpleStringProperty(getFieldValue(data.getValue(), "studyID")));

            TableColumn<Document, String> studyDateColumn = new TableColumn<>("Study Date");
            studyDateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(getFieldValue(data.getValue(), "studyDate"))));

            TableColumn<Document, String> studyTimeColumn = new TableColumn<>("Study Time");
            studyTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(formatTime(getFieldValue(data.getValue(), "studyTime"))));

            // Add the explicitly defined columns to the table
            dicomTable.getColumns().addAll(
                    dicomIdColumn, fileNameColumn, patientIdColumn, studyIdColumn, studyDateColumn, studyTimeColumn);

            // Add filtered data to the table
            dicomTable.setItems(FXCollections.observableArrayList(filteredResources));
            System.out.println("[DEBUG] Loaded DICOM data for patient: " + patientName);

        } catch (Exception e) {
            System.err.println("[ERROR] Error loading DICOM data: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            Object value = document.get(fieldName);
            if (value instanceof ObjectId) {
                return value.toString(); // Converte ObjectId in stringa
            } else if (value instanceof String) {
                return (String) value; // Ritorna direttamente la stringa
            } else if (value != null) {
                return value.toString(); // Converte qualsiasi altro tipo in stringa
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error getting field value for '" + fieldName + "': " + e.getMessage());
        }
        return "N/A"; // Valore predefinito in caso di errore o campo nullo
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
