package com.group01.dhsa.Controller;

import com.group01.dhsa.LoggedUser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import static com.mongodb.client.model.Filters.eq;

public class LoginUserController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Label errorMessage;

    @FXML
    private Button loginButton;

    @FXML
    private MenuButton myHospitalMenuItem;

    @FXML
    private MenuItem otherHospitalMenuItem;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private ImageView togglePasswordIcon;

    private boolean isPasswordVisible = false;

    private final Image visibleIcon = new Image(getClass().getResourceAsStream("/com/group01/dhsa/icons/visual.png"));
    private final Image notVisibleIcon = new Image(getClass().getResourceAsStream("/com/group01/dhsa/icons/not-visible.png"));

    @FXML
    private void initialize() {
        // Configura icona iniziale
        togglePasswordIcon.setImage(notVisibleIcon);

        // Aggiungi listener per "Invio"
        usernameField.setOnKeyPressed(this::handleEnterPressed);
        passwordField.setOnKeyPressed(this::handleEnterPressed);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();

        String organization = myHospitalMenuItem.getText();

        String role = validateCredentials(username, password, organization);

        if (role != null) {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            ChangeScreen screenChanger = new ChangeScreen();

            switch (role) {
                case "doctor":
                    screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", currentStage, "Doctor Dashboard");
                    break;
                case "patient":
                    screenChanger.switchScreen("/com/group01/dhsa/View/PatientPanelScreen.fxml", currentStage, "Patient Dashboard");
                    break;
                default:
                    errorMessage.setText("Unknown user role!");
            }
        } else {
            errorMessage.setText("Invalid credentials! Please try again.");
        }
    }

    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loginButton.fire(); // Simula il clic del pulsante login
        }
    }

    @FXML
    private void changeOrganization() {
        String currentOrg = myHospitalMenuItem.getText();
        String choosenOrg = otherHospitalMenuItem.getText();
        myHospitalMenuItem.setText(choosenOrg);
        otherHospitalMenuItem.setText(currentOrg);
    }

    @FXML
    private void togglePasswordVisibility() {
        if (visiblePasswordField == null) {
            System.err.println("visiblePasswordField is null!");
            return;
        }
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Cambia l'icona e rendi il testo della password visibile
            togglePasswordIcon.setImage(visibleIcon);
            passwordField.setPromptText(passwordField.getText());
            passwordField.clear();
            passwordField.setManaged(false); // Nasconde il PasswordField
            passwordField.setVisible(false);

            visiblePasswordField.setManaged(true); // Mostra il TextField
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setText(passwordField.getPromptText());
        } else {
            // Cambia l'icona e torna al PasswordField
            togglePasswordIcon.setImage(notVisibleIcon);
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setManaged(true);
            passwordField.setVisible(true);

            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
        }
    }

    private String validateCredentials(String username, String password, String organization) {
        String connectionString="";

        if (organization.equals("My Hospital")) {
            connectionString = "mongodb://admin:mongodb@localhost:27017";
        } else if (organization.equals("Other Hospital")) {
            connectionString = "mongodb://admin:mongodb@localhost:27018";
        }
        try (MongoClient mongoClient = MongoClients.create(connectionString)){
            MongoDatabase mongoDatabase = mongoClient.getDatabase("data_app");
            MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
            Document user = usersCollection.find(eq("username", username)).first();

            if (user != null) {
                String storedHash = user.getString("passwordHash");
                if (BCrypt.checkpw(password, storedHash)) {
                    LoggedUser userLog = LoggedUser.getInstance();
                    userLog.setFhirId(user.getString("fhirID"));
                    LoggedUser.setOrganization(user.getString("organization"));
                    LoggedUser.setRole(user.getString("role"));
                    return user.getString("role");
                }

            }

        }
        return null;
    }

}
