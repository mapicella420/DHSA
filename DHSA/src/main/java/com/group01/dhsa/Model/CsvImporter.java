package com.group01.dhsa.Model;

import java.io.File;

public class CsvImporter extends ObservableModel {
    public void importCsv(File file) {
        try {
            // Simula l'importazione del file CSV
            System.out.println("Importing CSV: " + file.getName());
            Thread.sleep(1000); // Simula un'elaborazione

            // Dopo aver completato, notifica gli observer
            notifyObservers("CSV file imported successfully: " + file.getName());
        } catch (Exception e) {
            notifyObservers("Failed to import CSV file: " + file.getName());
        }
    }
}
