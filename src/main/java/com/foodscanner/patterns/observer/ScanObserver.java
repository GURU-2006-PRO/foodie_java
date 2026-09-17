package com.foodscanner.patterns.observer;

import org.json.JSONObject;

/**
 * OBSERVER PATTERN — Observer Interface
 * Any class interested in scan events implements this.
 */
public interface ScanObserver {
    void onScanCompleted(String foodName, JSONObject scanResult, String warnings);
}
