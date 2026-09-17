package com.foodscanner.patterns.chain;

import org.json.*;
import java.util.*;

/**
 * CHAIN OF RESPONSIBILITY — General Health Handler (end of chain)
 */
public class GeneralHealthHandler extends HealthCheckHandler {

    @Override
    public void handle(JSONObject foodData, List<String> warnings) {
        try {
            JSONObject hw = foodData.optJSONObject("health_warnings");
            if (hw != null) {
                String warn = hw.optString("general", "");
                if (!warn.isBlank()) {
                    warnings.add("ℹ️ General: " + warn);
                }
            }
        } catch (Exception ignored) {}
        // End of chain — no passToNext
    }
}
