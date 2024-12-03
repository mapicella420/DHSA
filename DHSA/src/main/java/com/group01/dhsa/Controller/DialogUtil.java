package com.group01.dhsa.Controller;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utility class for displaying dialog boxes.
 */
public class DialogUtil {

    /**
     * Show a dialog with a custom message, alert type, and title.
     *
     * @param message the message to display
     * @param at the type of alert (e.g., Alert.AlertType.ERROR)
     * @param title the title of the dialog
     * @return true if the user confirms the dialog (for CONFIRMATION alerts), otherwise false
     */
    public static boolean showDialog(String message, Alert.AlertType at, String title) {
        Alert alert = new Alert(at);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (at == Alert.AlertType.CONFIRMATION) {
            return showConfirmationDialog(alert);
        } else {
            // Show the dialog for other types
            alert.show();
            return true;
        }
    }

    /**
     * Handles confirmation dialogs, waiting for user input.
     *
     * @param alert the alert dialog to show
     * @return true if the user presses "OK", false otherwise
     */
    private static boolean showConfirmationDialog(Alert alert) {
        Optional<ButtonType> result = alert.showAndWait();
        return result.map(buttonType -> buttonType == ButtonType.OK).orElse(false);
    }
}
