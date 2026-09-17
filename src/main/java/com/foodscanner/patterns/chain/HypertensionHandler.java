package com.foodscanner.patterns.chain;

import org.json.*;
import java.util.*;

/**
 * CHAIN OF RESPONSIBILITY — Hypertension Check Handler
 */
public class HypertensionHandler extends HealthCheckHandler {

    @Override
    public void handle(JSONObject foodData, List<String> warnings) {
        try {
            JSONObject hw = foodData.optJSONObject("health_warnings");
            if (hw != null) {
                String warn = hw.optString("hypertension", "");
                if (!warn.isBlank()) {
                    warnings.add("❤️ Hypertension: " + warn);
                }
            }
        } catch (Exception ignored) {}
        passToNext(foodData, warnings);
    }
}
