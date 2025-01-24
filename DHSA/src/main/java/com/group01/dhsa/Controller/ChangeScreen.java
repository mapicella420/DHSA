package com.group01.dhsa.Controller;

import java.io.IOException;
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
            newStage.setTitle(title);

            // Set modality to prevent interaction with other stages
            newStage.initModality(Modality.APPLICATION_MODAL);

            // Set the current stage as the owner of the new stage
            newStage.initOwner(currentStage);

            // Show the new stage and wait until it is closed
            newStage.showAndWait();

            // Return the controller of the loaded FXML
            return loader.getController();
        } catch (IOException ex) {
            // Show an error dialog if loading fails
            Platform.runLater(() -> {
                DialogUtil.showDialog("Error loading the screen: " + path, Alert.AlertType.ERROR, "Screen Change Error");
            });
        }
        return null;
    }
}
