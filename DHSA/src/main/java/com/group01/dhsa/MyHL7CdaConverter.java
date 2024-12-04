package com.group01.dhsa;

import com.group01.dhsa.Model.MongoInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        launch(args);
    }
}
