package com.foodscanner.patterns.factory;

import org.json.JSONObject;

/**
 * FACTORY METHOD PATTERN — Product Interface
 * All food analyzers implement this interface.
 */
public interface NutrientAnalyzer {
    /**
     * Builds the Gemini prompt for analyzing this food type.
     */
    String buildPrompt(String foodName);

    /**
     * Returns the food category this analyzer handles.
     */
    String getCategory();
}
