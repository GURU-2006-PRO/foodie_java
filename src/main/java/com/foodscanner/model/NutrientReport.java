package com.foodscanner.model;

import java.util.List;

/**
 * Model representing a completed nutrient analysis report.
 */
public class NutrientReport {
    private String foodName;
    private String rawJson;
    private List<String> healthWarnings;
    private int healthScore;

    public NutrientReport(String foodName, String rawJson, List<String> healthWarnings, int healthScore) {
        this.foodName = foodName;
        this.rawJson = rawJson;
        this.healthWarnings = healthWarnings;
        this.healthScore = healthScore;
    }

    public String getFoodName() { return foodName; }
    public String getRawJson() { return rawJson; }
    public List<String> getHealthWarnings() { return healthWarnings; }
    public int getHealthScore() { return healthScore; }
}
