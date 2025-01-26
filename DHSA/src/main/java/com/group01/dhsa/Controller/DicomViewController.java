package com.group01.dhsa.Controller;

import com.group01.dhsa.Model.LoggedUser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bson.Document;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

public class DicomViewController {

    @FXML
    private VBox rootVBox;

    @FXML
    private ImageView dicomImageView;

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label frameLabel;

    @FXML
    private ScrollBar frameScrollBar;

    @FXML
    private Label patientIdLabel;

    @FXML
    private Label patientBirthDateLabel;

    @FXML
    private Label patientSexLabel;

    @FXML
    private Label studyInstanceUIDLabel;

    @FXML
    private Label modalityLabel;

    @FXML
    private Label studyDateLabel;

    @FXML
    private Label studyTimeLabel;

    @FXML
    private Label accessionNumberLabel;

    @FXML
    private Button playPauseButton;

    @FXML
    private ImageView playPauseIcon;

    private Document dicomFile;
    private int totalFrames = 1;
    private ImageReader dicomReader;
    private ImageInputStream dicomInputStream;

    private Timeline timeline;
    private boolean isPlaying = false;

    private final Image playIcon = new Image(getClass().getResourceAsStream("/com/group01/dhsa/icons/play.png"));
    private final Image pauseIcon = new Image(getClass().getResourceAsStream("/com/group01/dhsa/icons/pause.png"));



    @FXML
    public void initialize() {
        System.out.println("[DEBUG] DicomViewController initialized.");
        if (dicomFile == null) {
            System.out.println("[DEBUG] No DICOM file set during initialization.");
        } else {
            setupDicomReader(dicomFile);
            loadMetadata(dicomFile);
            setupScrollBar();
            loadFrame(0); // Load the first frame by default
        }

        setupMouseScrollListener();
        updatePlayPauseIcon(); // Imposta l'icona iniziale
    }

    public void setDicomFile(Document dicomFile) {
        this.dicomFile = dicomFile;
        System.out.println("[DEBUG] DICOM file set: " + dicomFile.toJson());

        setupDicomReader(dicomFile);
        loadMetadata(dicomFile);
        setupScrollBar();
        loadFrame(0); // Load the first frame by default
    }

    private void setupDicomReader(Document dicomFile) {
        try {
            String filePath = dicomFile.getString("filePath");
            System.out.println("[DEBUG] Setting up DICOM reader for file: " + filePath);

            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("[ERROR] File path is missing or empty.");
            }

            File file = new File(filePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("[ERROR] File not found: " + filePath);
            }

            dicomInputStream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("[ERROR] No DICOM readers available.");
            }

            dicomReader = readers.next();
            dicomReader.setInput(dicomInputStream);

            totalFrames = dicomReader.getNumImages(true); // Get total number of frames
            System.out.println("[DEBUG] Total frames in DICOM: " + totalFrames);
        } catch (Exception e) {
            System.err.println("[ERROR] Error setting up DICOM reader: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFrame(int frameIndex) {
        try {
            if (dicomReader == null) {
                System.err.println("[ERROR] DICOM reader not initialized.");
                return;
            }

            if (frameIndex < 0 || frameIndex >= totalFrames) {
                System.err.println("[ERROR] Frame index out of bounds: " + frameIndex);
                return;
            }

            System.out.println("[DEBUG] Loading frame: " + (frameIndex + 1) + "/" + totalFrames);

            DicomImageReadParam param = (DicomImageReadParam) dicomReader.getDefaultReadParam();
            BufferedImage bufferedImage = dicomReader.read(frameIndex, param);

            Image image = javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);
            dicomImageView.setImage(image);

            frameLabel.setText("Frame " + (frameIndex + 1) + "/" + totalFrames);

            System.out.println("[DEBUG] Frame " + (frameIndex + 1) + " loaded successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading frame: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMetadata(Document dicomFile) {
        try {
            System.out.println("[DEBUG] Loading metadata for file: " + dicomFile.getString("fileName"));

            patientNameLabel.setText("Patient Name: " + dicomFile.getString("patientName"));
            patientIdLabel.setText("Patient ID: " + dicomFile.getString("patientId"));
            patientBirthDateLabel.setText("Birth Date: " + formatDate(dicomFile.getString("patientBirthDate")));
            patientSexLabel.setText("Sex: " + dicomFile.getString("patientSex"));
            studyInstanceUIDLabel.setText("Study UID: " + dicomFile.getString("studyInstanceUID"));
            modalityLabel.setText("Modality: " + dicomFile.getString("modality"));
            studyDateLabel.setText("Study Date: " + formatDate(dicomFile.getString("studyDate")));
            studyTimeLabel.setText("Study Time: " + formatTime(dicomFile.getString("studyTime")));
            accessionNumberLabel.setText("Accession Number: " + dicomFile.getString("accessionNumber"));

            System.out.println("[DEBUG] Metadata loaded successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Error loading metadata: " + e.getMessage());
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

    private String formatDate(String date) {
        try {
            if (date == null || date.length() != 8) {
                return "Invalid Date";
            }
            String year = date.substring(0, 4);
            String month = date.substring(4, 6);
            String day = date.substring(6, 8);
            return day + "/" + month + "/" + year;
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    private void setupScrollBar() {
        frameScrollBar.setMin(0);
        frameScrollBar.setMax(totalFrames - 1);
        frameScrollBar.setValue(0);

        frameScrollBar.valueProperty().addListener((obs, oldValue, newValue) -> {
            int frameIndex = newValue.intValue();
            System.out.println("[DEBUG] Scrolling to frame: " + (frameIndex + 1));

            if (frameIndex >= totalFrames - 1) {
                frameScrollBar.setValue(0); // Riparti dal primo frame
                loadFrame(0);
            } else {
                loadFrame(frameIndex);
            }
        });

        System.out.println("[DEBUG] ScrollBar setup complete. Total frames: " + totalFrames);
    }

    private void setupMouseScrollListener() {
        rootVBox.setOnScroll(event -> {
            double delta = event.getDeltaY(); // Get the scroll delta
            double currentValue = frameScrollBar.getValue();

            if (delta > 0) {
                frameScrollBar.setValue(Math.max(frameScrollBar.getMin(), currentValue - 1)); // Scroll up
            } else if (delta < 0) {
                frameScrollBar.setValue(Math.min(frameScrollBar.getMax(), currentValue + 1)); // Scroll down
            }

            System.out.println("[DEBUG] Mouse scroll event: DeltaY=" + delta + ", New Scroll Value=" + frameScrollBar.getValue());
        });
    }

    @FXML
    private void onPlayPauseButtonClick() {
        if (isPlaying) {
            stopPlayback();
        } else {
            startPlayback();
        }
        isPlaying = !isPlaying;
        updatePlayPauseIcon();
    }

    private void startPlayback() {
        timeline = new Timeline(new KeyFrame(Duration.millis(150), event -> {
            double currentValue = frameScrollBar.getValue();
            if (currentValue < frameScrollBar.getMax()) {
                frameScrollBar.setValue(currentValue + 1); // Passa al frame successivo
            } else {
                frameScrollBar.setValue(0); // Riparti dal primo frame
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopPlayback() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void updatePlayPauseIcon() {
        if (isPlaying) {
            playPauseIcon.setImage(pauseIcon);
        } else {
            playPauseIcon.setImage(playIcon);
        }
    }

    @FXML
    private void onBackButtonClick() {
        Stage stage = (Stage) dicomImageView.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();

        // Recupera il ruolo dell'utente loggato
        String userRole = LoggedUser.getRole();

        if ("Doctor".equalsIgnoreCase(userRole)) {
            // Se il ruolo è Doctor, passa alla schermata DoctorPanelScreen
            screenChanger.switchScreen("/com/group01/dhsa/View/DicomListScreen.fxml", stage, "DICOM Files");
        } else if ("Patient".equalsIgnoreCase(userRole)) {
            // Se il ruolo è Patient, passa alla schermata PatientPanelScreen
            screenChanger.switchScreen("/com/group01/dhsa/View/PatientDicomScreen.fxml", stage, "Patient DICOM Files");
        } else {
            // Opzione predefinita in caso di ruolo sconosciuto
            System.err.println("[ERROR] Unknown user role: " + userRole);
            // Mostra un messaggio di errore o esegui un'azione alternativa
        }
    }


    public void onCloseButtonClick(ActionEvent actionEvent) {
        stopPlayback();
        System.out.println("[DEBUG] Returning to the DICOM List screen.");
        Stage stage = (Stage) dicomImageView.getScene().getWindow();
        ChangeScreen screenChanger = new ChangeScreen();
        screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", stage, "Doctor Dashboard");
    }
}
