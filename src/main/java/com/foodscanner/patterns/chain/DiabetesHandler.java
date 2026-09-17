package com.foodscanner.patterns.chain;

import org.json.*;
import java.util.*;

/**
 * CHAIN OF RESPONSIBILITY — Diabetes Check Handler
 * Evaluates sugar content, glycemic risk, and AI health warnings
 * to provide explicit dietary guidance (e.g. "If you have sugar/diabetes, avoid it").
 */
public class DiabetesHandler extends HealthCheckHandler {

    @Override
    public void handle(JSONObject foodData, List<String> warnings) {
        try {
            JSONObject hw = foodData.optJSONObject("health_warnings");
            JSONObject nutrients = foodData.optJSONObject("nutrients");

            String aiWarning = (hw != null) ? hw.optString("diabetes", "") : "";
            String sugarVal = (nutrients != null) ? nutrients.optString("sugar", "") : "";

            double parsedSugar = parseNumeric(sugarVal);

            if (parsedSugar >= 12.0 || aiWarning.toLowerCase().contains("avoid") || aiWarning.toLowerCase().contains("high sugar")) {
                String sugarNote = (parsedSugar > 0) ? " (Contains " + sugarVal + " sugar)" : "";
                warnings.add("🩺 Diabetes Alert: High sugar / glycemic impact" + sugarNote +
                             ". If you have diabetes or high blood sugar, AVOID or strictly restrict this item. " + aiWarning);
            } else if (!aiWarning.isBlank()) {
                warnings.add("🩺 Diabetes Guidance: " + aiWarning);
            } else if (parsedSugar > 0) {
                warnings.add("🩺 Diabetes Guidance: Contains " + sugarVal + " sugar. Consume moderately if monitoring blood glucose.");
            }
        } catch (Exception ignored) {}

        passToNext(foodData, warnings);
    }

    private double parseNumeric(String val) {
        if (val == null || val.isBlank()) return 0.0;
        try {
            String clean = val.replaceAll("[^0-9.]", "");
            return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}