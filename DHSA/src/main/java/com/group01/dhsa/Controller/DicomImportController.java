package com.group01.dhsa.Controller;

import com.group01.dhsa.EventManager;
import com.group01.dhsa.ObserverPattern.EventListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DicomImportController implements EventListener {

    @FXML
    private Button importFolderButton;

    @FXML
    private Button importFileButton;

    @FXML
    private Button backButton;

    @FXML
    private Button importButton;

    @FXML
    private Label uploadingFileLabel;

    @FXML
    private Label uploadProgressLabel;

    private final ObservableList<File> dicomFiles = FXCollections.observableArrayList();

    public DicomImportController() {
        EventManager.getInstance().getEventObservable().subscribe("dicom_imported", this);
    }

    @FXML
    public void initialize() {
        uploadingFileLabel.setText("Waiting for import...");
        uploadProgressLabel.setText("0/0 files uploaded");
    }

    @FXML
    private void onImportFolderClick() {

        uploadingFileLabel.setVisible(true);
        uploadProgressLabel.setVisible(true);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select DICOM Folder");
        File selectedDirectory = directoryChooser.showDialog(importFolderButton.getScene().getWindow());

        if (selectedDirectory != null && selectedDirectory.isDirectory()) {
            File[] files = selectedDirectory.listFiles((dir, name) -> name.endsWith(".dcm"));
            if (files != null) {
                dicomFiles.addAll(List.of(files));
                updateProgressLabel();
            }
            // Update the button text with the folder name
            importFolderButton.setText(selectedDirectory.getName());
        }
    }

    @FXML
    private void onImportFileClick() {

        uploadingFileLabel.setVisible(true);
        uploadProgressLabel.setVisible(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DICOM File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DICOM Files", "*.dcm"));
        File selectedFile = fileChooser.showOpenDialog(importFileButton.getScene().getWindow());

        if (selectedFile != null) {
            dicomFiles.add(selectedFile);
            updateProgressLabel();
            // Update the button text with the file name
            importFileButton.setText(selectedFile.getName());
        }
    }

    @FXML
    private void onImportButtonClick() {
        importButton.setDisable(true);
        uploadingFileLabel.setVisible(true);
        uploadProgressLabel.setVisible(true);
        try {
            List<File> filesToImport = new ArrayList<>(dicomFiles);
            int totalFiles = filesToImport.size();
            for (int i = 0; i < totalFiles; i++) {
                File file = filesToImport.get(i);
                uploadingFileLabel.setText("Uploading: " + file.getName());
                uploadProgressLabel.setText((i + 1) + "/" + totalFiles + " files uploaded");

                // Simulate file import (replace with actual logic)
                EventManager.getInstance().getEventObservable().notify("dicom_upload", file);
            }

            uploadingFileLabel.setText("All files uploaded successfully!");
        } catch (Exception e) {
            System.err.println("Error during import: " + e.getMessage());
            uploadingFileLabel.setText("Error during upload.");
        } finally {
            importButton.setDisable(false);
        }
    }

    @FXML
    private void onBackButtonClick() {
        System.out.println("Returning to the previous screen...");
        Stage stage = (Stage) backButton.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "Doctor Dashboard");
    }

    @Override
    public void handleEvent(String eventType, File file) {
        if ("dicom_imported".equals(eventType)) {
            dicomFiles.add(file);
        }
    }

    private void updateProgressLabel() {
        uploadProgressLabel.setText("0/" + dicomFiles.size() + " files uploaded");
    }

    public void onRefreshButtonClick() {
        System.out.println("Navigating to Upload CSV screen...");
        Stage currentStage = (Stage) uploadProgressLabel.getScene().getWindow(); // Recupera lo Stage dalla scena corrente
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DicomImportScreen.fxml", currentStage, "Upload CSV");
    }
}
