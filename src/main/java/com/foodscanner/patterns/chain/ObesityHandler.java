package com.foodscanner.patterns.chain;

import org.json.*;
import java.util.*;

/**
 * CHAIN OF RESPONSIBILITY — Obesity Check Handler
 */
public class ObesityHandler extends HealthCheckHandler {

    @Override
    public void handle(JSONObject foodData, List<String> warnings) {
        try {
            JSONObject hw = foodData.optJSONObject("health_warnings");
            if (hw != null) {
                String warn = hw.optString("obesity", "");
                if (!warn.isBlank()) {
                    warnings.add("⚖️ Obesity: " + warn);
                }
            }
        } catch (Exception ignored) {}
        passToNext(foodData, warnings);
    }
}
