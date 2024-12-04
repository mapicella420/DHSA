package com.group01.dhsa.Model;

import java.util.ArrayList;
import java.util.List;

public abstract class ObservableModel {
    private final List<ModelObserver> observers = new ArrayList<>();

    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String message) {
        for (ModelObserver observer : observers) {
            observer.onModelUpdate(message);
        }
    }
}
