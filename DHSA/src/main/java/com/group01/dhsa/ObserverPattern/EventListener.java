package com.group01.dhsa.ObserverPattern;

import java.io.File;

/**
 * The EventListener interface defines a contract for handling events in an observer pattern.
 * Classes implementing this interface can subscribe to an {@link EventObservable} and handle specific events.
 */
public interface EventListener {

    /**
     * Handles an event when it is triggered by the observable.
     *
     * @param eventType The type of event that occurred (e.g., "csv_upload", "cda_generated").
     * @param file      The file associated with the event, if applicable.
     */
    void handleEvent(String eventType, File file);
}
