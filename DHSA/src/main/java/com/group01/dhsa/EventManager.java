package com.group01.dhsa;

import com.group01.dhsa.Model.CsvImporter;
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
     *  Metodo per ottenere l'unica istanza Singleton
     * @return instance
     */
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    // Metodo per ottenere l'EventObservable
    public EventObservable getEventObservable() {
        return eventObservable;
    }

    // Inizializza i listener
    private void initializeListeners() {
        // Registra il CsvImporter come listener
        CsvImporter csvImporter = new CsvImporter();
        eventObservable.subscribe("csv_upload", csvImporter);
    }
}
