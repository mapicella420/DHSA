package com.group01.dhsa;

import com.group01.dhsa.Model.MongoInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The MyHL7CdaConverter class is the main entry point for the application.
 * It extends the JavaFX Application class and manages the initialization and
 * display of the primary user interface, as well as the setup of the database.
 */
public class MyHL7CdaConverter extends Application {

    /**
     * The start method is called when the JavaFX application is launched.
     * It loads the login screen UI from the FXML file and sets it as the primary stage.
     *
     * @param primaryStage The primary stage for this application.
     * @throws Exception If there is an issue loading the FXML file.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the LoginUserScreen.fxml to create the initial user interface.
        Parent root = FXMLLoader.load(getClass().getResource("/com/group01/dhsa/View/LoginUserScreen.fxml"));

        // Set the title of the primary stage.
        primaryStage.setTitle("Login Screen");

        // Set the scene with dimensions 600x400 and show the primary stage.
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    /**
     * Initializes the MongoDB database by invoking the MongoInitializer class.
     * This method ensures the database is properly set up before the application starts.
     * If there is an error during initialization, the application logs the error and exits.
     */
    private static void initializeDatabase() {
        try {
            // Call the MongoInitializer to set up the database.
            MongoInitializer.initializeDatabase();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            // Log an error if database initialization fails.
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();

            // Exit the application in case of a critical database error.
            System.exit(1);
        }
    }

    /**
     * The main method is the entry point of the application.
     * It initializes the database and launches the JavaFX application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Initialize the database before starting the application.
        initializeDatabase();

        // Launch the JavaFX application.
        launch(args);
    }
}
