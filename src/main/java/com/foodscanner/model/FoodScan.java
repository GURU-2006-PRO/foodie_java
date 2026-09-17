package com.foodscanner.model;

/**
 * Model representing a food scan request.
 */
public class FoodScan {
    private String foodName;
    private String category;
    private long timestamp;

    public FoodScan(String foodName, String category) {
        this.foodName = foodName;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
    }

    public String getFoodName() { return foodName; }
    public String getCategory() { return category; }
    public long getTimestamp() { return timestamp; }
}
