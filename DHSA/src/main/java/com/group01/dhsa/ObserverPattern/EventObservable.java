package com.group01.dhsa.ObserverPattern;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventObservable {

    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    // Registra un listener per un tipo di evento
    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    // Rimuove un listener per un tipo di evento
    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> users = listeners.get(eventType);
        if (users != null) {
            users.remove(listener);
        }
    }

    // Notifica tutti i listener registrati per un tipo di evento
    public void notify(String eventType, File file) {
        List<EventListener> users = listeners.get(eventType);
        if (users != null) {
            for (EventListener listener : users) {
                listener.handleEvent(eventType, file);
            }
        }
    }
}
