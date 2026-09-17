package com.foodscanner.patterns.chain;

import org.json.*;
import java.util.*;

/**
 * CHAIN OF RESPONSIBILITY PATTERN — Base Handler
 * Each handler checks a specific health condition against the food data.
 */
public abstract class HealthCheckHandler {

    protected HealthCheckHandler next;

    public HealthCheckHandler setNext(HealthCheckHandler next) {
        this.next = next;
        return next;
    }

    /**
     * Process the health check. Adds warnings to the list if condition applies.
     */
    public abstract void handle(JSONObject foodData, List<String> warnings);

    protected void passToNext(JSONObject foodData, List<String> warnings) {
        if (next != null) {
            next.handle(foodData, warnings);
        }
    }
}
