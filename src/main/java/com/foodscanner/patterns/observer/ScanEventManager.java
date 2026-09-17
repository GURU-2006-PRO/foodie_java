package com.foodscanner.patterns.observer;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 * OBSERVER PATTERN — Subject
 * Manages a list of observers and notifies them when a scan is completed.
 */
public class ScanEventManager {

    private final List<ScanObserver> observers = new ArrayList<>();

    public void subscribe(ScanObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(ScanObserver observer) {
        observers.remove(observer);
    }

    public void notifyScanCompleted(String foodName, JSONObject scanResult, String warnings) {
        for (ScanObserver observer : observers) {
            observer.onScanCompleted(foodName, scanResult, warnings);
        }
    }
}
