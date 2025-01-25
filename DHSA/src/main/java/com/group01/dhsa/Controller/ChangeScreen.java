package com.group01.dhsa.Controller;

import java.io.IOException;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Utility class handling screen transitions and controller retrieval.
 */
public class ChangeScreen {

    /**
     * Switch to a new screen and close the current stage.
     *
     * @param path the path to the FXML file (relative to the `resources` directory).
     * @param currentStage the current stage that will be closed.
     * @param title the title for the new stage.
     * @return the controller of the loaded FXML, or null if an error occurs.
     */
    public Object switchScreen(String path, Stage currentStage, String title) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            // Create a new stage for the loaded scene
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            // Close the current stage
            currentStage.close();

            // Show the new stage
            newStage.setTitle(title);
            newStage.setResizable(true);
            newStage.show();

            // Return the controller of the loaded FXML
            return loader.getController(); // Ritorna il controller effettivo
        } catch (IOException ex) {
            ex.printStackTrace(); // Stampa lo stack trace per il debug
            Platform.runLater(() -> {
                DialogUtil.showDialog("Error loading the screen: " + path, Alert.AlertType.ERROR, "Screen Change Error");
            });
        }
        return null;
    }

    /**
     * Switch to a new screen modally, keeping the current stage open.
     *
     * @param path the path to the FXML file (relative to the `resources` directory).
     * @param currentStage the current stage that will act as the owner of the new stage.
     * @param title the title for the new stage.
     * @return the controller of the loaded FXML, or null if an error occurs.
     */
    public Object switchScreenModal(String path, Stage currentStage, String title) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            // Create a new stage for the loaded scene
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            // Set the title and properties of the new stage
            newStage.setTitle(title);
            newStage.setResizable(true);
            newStage.initOwner(currentStage); // Set the owner for modality (optional)
            newStage.initModality(Modality.WINDOW_MODAL); // Optional: change to Modality.NONE for no modality
            newStage.show();

            // Return the controller of the loaded FXML
            return loader.getController(); // Return the actual controller
        } catch (IOException ex) {
            ex.printStackTrace(); // Print the stack trace for debugging
            Platform.runLater(() -> {
                DialogUtil.showDialog("Error loading the screen: " + path, Alert.AlertType.ERROR, "Screen Change Error");
            });
        }
        return null;
    }

    /**
     * Switch to a new screen, passing data to the new controller, without closing the current stage.
     *
     * @param path the path to the FXML file (relative to the `resources` directory).
     * @param currentStage the current stage that will remain open.
     * @param title the title for the new stage.
     * @param data a map containing the data to pass to the new controller.
     * @return the controller of the loaded FXML, or null if an error occurs.
     */
    public Object switchScreenWithData(String path, Stage currentStage, String title, Map<String, Object> data) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            // Retrieve the controller
            Object controller = loader.getController();

            // Pass data to the new controller if it implements DataReceiver
            if (controller instanceof DataReceiver) {
                ((DataReceiver) controller).receiveData(data);
            }

            // Create a new stage for the loaded scene
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            // Configure the new stage
            newStage.setTitle(title);
            newStage.setResizable(true);

            // Show the new stage without closing the current one
            newStage.show();

            return controller;
        } catch (IOException ex) {
            ex.printStackTrace(); // Print stack trace for debugging
            Platform.runLater(() -> DialogUtil.showDialog(
                    "Error loading the screen: " + path,
                    Alert.AlertType.ERROR,
                    "Screen Change Error"
            ));
        }
        return null;
    }

}
