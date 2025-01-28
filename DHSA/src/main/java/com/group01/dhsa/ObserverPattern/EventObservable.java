package com.group01.dhsa.ObserverPattern;

import java.io.File;
import java.util.*;

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
            Iterator<EventListener> iterator = users.iterator();
            boolean removed = false;

            while (iterator.hasNext()) {
                EventListener user = iterator.next();

                // Estrai il nome della classe principale ignorando la parte anonima
                String listenerClassName = listener.getClass().getName().split("\\$\\$")[0];
                String userClassName = user.getClass().getName().split("\\$\\$")[0];

                if (listenerClassName.equals(userClassName)) {
                    iterator.remove();
                    removed = true;
                    System.out.println("[INFO] Rimosso listener di tipo: " + listenerClassName);
                    break; // Una volta rimosso, fermiamo il ciclo
                }
            }

            if (!removed) {
                System.out.println("[INFO] Nessun listener rimosso: " + listener.getClass().getName());
                System.out.println("[DEBUG] Lista attuale:");
                users.forEach(u -> System.out.println(" - " + u.getClass().getName()));
            }
        } else {
            System.out.println("[WARNING] Nessun listener registrato per l'evento: " + eventType);
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
        if (users != null && !users.isEmpty()) {
            for (EventListener listener : users) {
                try {
                    listener.handleEvent(eventType, file);
                } catch (Exception e) {
                    System.err.println("Error notifying listener for event type '" + eventType + "': " + e.getMessage());
                }
            }
        } else {
            System.err.println("No listeners registered for event type: " + eventType);
        }
    }

    /**
     * Checks if any listeners are registered for a specific event type.
     *
     * @param eventType The type of event to check.
     * @return True if there are listeners registered for the event type, false otherwise.
     */
    public boolean hasListeners(String eventType) {
        List<EventListener> users = listeners.get(eventType);
        return users != null && !users.isEmpty();
    }
}
