package com.group01.dhsa.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginUserController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessage;

    @FXML
    private Button loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateCredentials(username, password)) {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            ChangeScreen screenChanger = new ChangeScreen();

            if ("doctor".equals(username)) {
                screenChanger.switchScreen("/com/group01/dhsa/View/DoctorPanelScreen.fxml", currentStage, "Doctor Dashboard");
            } else if ("patient".equals(username)) {
                screenChanger.switchScreen("/com/group01/dhsa/View/PatientPanelScreen.fxml", currentStage, "Patient Dashboard");
            } else {
                errorMessage.setText("Unknown user role!");
            }
        } else {
            errorMessage.setText("Invalid credentials! Please try again.");
        }
    }

    private boolean validateCredentials(String username, String password) {
        return ("doctor".equals(username) && "doctor123".equals(password)) ||
                ("patient".equals(username) && "patient123".equals(password));
    }
}
