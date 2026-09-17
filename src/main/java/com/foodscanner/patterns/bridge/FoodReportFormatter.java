package com.foodscanner.patterns.bridge;

import org.json.*;

/**
 * BRIDGE PATTERN — Refined Abstraction
 * Formats the food scan report using the injected renderer.
 */
public class FoodReportFormatter extends ReportFormatter {

    public FoodReportFormatter(ReportOutputRenderer renderer) {
        super(renderer);
    }

    @Override
    public String format(String jsonData) {
        try {
            // Strip markdown code fences if Gemini added them
            String cleaned = jsonData.strip();
            if (cleaned.startsWith("  ")) cleaned = cleaned.substring(3);
            if (cleaned.startsWith("json")) cleaned = cleaned.substring(4);
            if (cleaned.endsWith("  ")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.strip();
            // Remove actual backtick fences
            cleaned = cleaned.replaceAll("^`[a-z]*\\n?", "").replaceAll("`$", "").strip();
            JSONObject obj = new JSONObject(cleaned);
            return renderer.render(obj);
        } catch (JSONException e) {
            return jsonData;
        }
    }
}
