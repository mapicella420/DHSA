package com.group01.dhsa.Model;

import com.group01.dhsa.ObserverPattern.EventListener;

import java.io.File;

public class DicomImporter implements EventListener {
    @Override
    public void handleEvent(String eventType, File file) {

    }

    public void importDicom(File file) {
        try {
            // Simula l'importazione del file DICOM
            System.out.println("Importing DICOM: " + file.getName());
            Thread.sleep(1000); // Simula un'elaborazione

            // Dopo aver completato, notifica gli observer
        } catch (Exception e) {
        }
    }
}
