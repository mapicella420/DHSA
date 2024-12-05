package com.group01.dhsa.ObserverPattern;

import java.io.File;

public interface EventListener {
    void handleEvent(String eventType, File file); // Metodo chiamato per gestire gli eventi
}