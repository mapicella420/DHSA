package com.group01.dhsa.Model;

import com.group01.dhsa.Model.FhirResources.*;
import com.group01.dhsa.ObserverPattern.EventListener;

import java.io.File;

public class CsvImporter implements EventListener {

    @Override
    public void handleEvent(String eventType, File file) {
        if ("csv_upload".equals(eventType)) {
            importCsv(file);
        }
    }

    public void importCsv(File file) {
        try {
            // Ottieni il nome del file
            String fileName = file.getName();

            // Usa la factory per ottenere l'importer corretto
            FhirResourceImporter importer = FhirImporterFactoryManager.getImporter(fileName);

            // Importa il CSV creando risorse FHIR
            System.out.println("Importing CSV: " + fileName);
            importer.importCsvToFhir(file.getAbsolutePath());

            // Notifica gli observer dopo il completamento
        } catch (IllegalArgumentException e) {
            // Gestisce il caso in cui il tipo di risorsa non sia supportato
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // Gestione generica delle eccezioni
            System.err.println("Error during import: " + e.getMessage());
        }
    }

}
