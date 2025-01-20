package com.group01.dhsa.Controller;

import com.group01.dhsa.Model.LoggedUser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import org.mindrot.jbcrypt.BCrypt;

import static com.mongodb.client.model.Filters.eq;

public class LoginUserController{

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessage;

    @FXML
    private Button loginButton;

    @FXML
    private MenuButton myHospitalMenuItem;

    @FXML
    private MenuItem otherHospitalMenuItem;

    @FXML
    private void initialize() {
        // Aggiunge un listener all'intero layout per gestire "Invio"
        usernameField.setOnKeyPressed(this::handleEnterPressed);
        passwordField.setOnKeyPressed(this::handleEnterPressed);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

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

    private String validateCredentials(String username, String password, String organization) {
        if (organization.equals("My Hospital")){
            try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27017")) {
                MongoDatabase mongoDatabase = mongoClient.getDatabase("data_app");
                MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
                Document user = usersCollection.find(eq("username", username)).first();

                if (user != null) {
                    String storedHash = user.getString("passwordHash");
                    if (BCrypt.checkpw(password, storedHash)) {
                        LoggedUser userLog = LoggedUser.getInstance();
                        userLog.setFhirId(user.getString("fhirID"));
                        System.out.println(userLog.getFhirId());
                        return user.getString("role");
                    }

                }
            }
        }
        else {
            try (MongoClient mongoClient = MongoClients.create("mongodb://admin:mongodb@localhost:27018")) {
                MongoDatabase mongoDatabase = mongoClient.getDatabase("data_app");
                MongoCollection<Document> usersCollection = mongoDatabase.getCollection("users");
                Document user = usersCollection.find(eq("username", username)).first();

                if (user != null) {
                    String storedHash = user.getString("passwordHash");
                    if (BCrypt.checkpw(password, storedHash)) {

                        LoggedUser userLog = LoggedUser.getInstance();
                        userLog.setFhirId(user.getString("fhirID"));
                        return user.getString("role");
                    }

                }
            }
        }
        return null;
    }
}
