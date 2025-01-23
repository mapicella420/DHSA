package com.group01.dhsa.ObserverPattern;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The EventObservable class represents the "observable" component in the observer pattern.
 * It maintains a registry of listeners (observers) and notifies them when specific events occur.
 */
public class EventObservable {

    // A map associating event types to their respective lists of listeners
    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    /**
     * Subscribes a listener to a specific type of event.
     *
     * @param eventType The type of event the listener wants to subscribe to.
     * @param listener  The listener to register for the event.
     */
    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Unsubscribes a listener from a specific type of event.
     *
     * @param eventType The type of event the listener wants to unsubscribe from.
     * @param listener  The listener to remove from the event registry.
     */
    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> users = listeners.get(eventType);
        if (users != null) {
            users.remove(listener);
        }
    }

    /**
     * Notifies all listeners subscribed to a specific event type.
     *
     * @param eventType The type of event that occurred.
     * @param file      The file associated with the event, if applicable.
     */
    public void notify(String eventType, File file) {
        List<EventListener> users = listeners.get(eventType);
        if (users != null) {
            for (EventListener listener : users) {
                listener.handleEvent(eventType, file);
            }
        }
    }
}
