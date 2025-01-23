package com.group01.dhsa;

import com.group01.dhsa.Model.CsvImporter;
import com.group01.dhsa.Model.FhirExporter;
import com.group01.dhsa.ObserverPattern.EventObservable;

public class EventManager {

    private static EventManager instance;
    private final EventObservable eventObservable;

    /**
     * Costruttore privato per il Singleton
     */
    private EventManager() {
        this.eventObservable = new EventObservable();

        // Inizializza e registra i listener
        initializeListeners();
    }

    /**
     * Metodo per ottenere l'unica istanza Singleton.
     *
     * @return L'istanza di EventManager.
     */
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    /**
     * Ottiene l'EventObservable.
     *
     * @return L'istanza di EventObservable.
     */
    public EventObservable getEventObservable() {
        return eventObservable;
    }

    /**
     * Inizializza i listener e registra le classi necessarie.
     */
    private void initializeListeners() {
        // Registra il CsvImporter come listener per l'importazione
        CsvImporter csvImporter = new CsvImporter();
        eventObservable.subscribe("csv_upload", csvImporter);


        FhirExporter fhirExporter = new FhirExporter();
        eventObservable.subscribe("export_request", fhirExporter);

    }
}
