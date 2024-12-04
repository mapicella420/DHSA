package com.group01.dhsa.Model;

import java.io.File;

public class DicomImporter extends ObservableModel {
    public void importDicom(File file) {
        try {
            // Simula l'importazione del file DICOM
            System.out.println("Importing DICOM: " + file.getName());
            Thread.sleep(1000); // Simula un'elaborazione

            // Dopo aver completato, notifica gli observer
            notifyObservers("DICOM file imported successfully: " + file.getName());
        } catch (Exception e) {
            notifyObservers("Failed to import DICOM file: " + file.getName());
        }
    }
}
