package com.group01.dhsa;

import com.group01.dhsa.Model.FhirExporter;
import com.group01.dhsa.Model.MongoInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class MyHL7CdaConverter extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/group01/dhsa/View/LoginUserScreen.fxml"));
        primaryStage.setTitle("Login Screen");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    private static void initializeDatabase() {
        try {
            MongoInitializer.initializeDatabase();
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        initializeDatabase();
        FhirExporter exporter = new FhirExporter();

        // Simula un evento export_request per il tipo "Patient"
        File tempFile = new File("Patient");
        exporter.handleEvent("export_request", tempFile);



        launch(args);
    }
}
