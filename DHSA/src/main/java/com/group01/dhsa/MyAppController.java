package com.group01.dhsa;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class MyAppController {

    @FXML
    private Label welcomeText;

    @FXML
    private TextArea emailInput;

    @FXML
    private TextArea passwordInput;

    @FXML
    private ImageView imageView;

    @FXML
    private Button helloButton;

    @FXML
    private void onHelloButtonClick() {
        // Example: Displaying a welcome message based on input
        String username = emailInput.getText();
        welcomeText.setText("Welcome, " + username + "!");
    }
}