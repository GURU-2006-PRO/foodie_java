package com.foodscanner.patterns.factory;

/**
 * FACTORY METHOD PATTERN — Creator
 * Creates the appropriate NutrientAnalyzer based on input type or category.
 */
public class NutrientAnalyzerFactory {

    /**
     * Factory method creating analyzer for image scans.
     */
    public static NutrientAnalyzer createImageAnalyzer() {
        return new ImageFoodAnalyzer();
    }

    /**
     * Factory method creating analyzer based on identifier or category.
     */
    public static NutrientAnalyzer createAnalyzer(String input) {
        if (input == null || input.isBlank() || input.equalsIgnoreCase("image") || input.startsWith("image/")) {
            return new ImageFoodAnalyzer();
        }

        String lower = input.toLowerCase();

        if (lower.matches(".*(juice|smoothie|drink|water|tea|coffee|milk|shake).*")) {
            return new BeverageAnalyzer();
        } else if (lower.matches(".*(fruit|apple|banana|mango|orange|grape|berry|melon|pear|peach).*")) {
            return new FruitAnalyzer();
        } else if (lower.matches(".*(vegetable|carrot|spinach|broccoli|kale|lettuce|tomato|potato|onion|pepper).*")) {
            return new VegetableAnalyzer();
        } else if (lower.matches(".*(chicken|beef|mutton|fish|egg|meat|pork|lamb|seafood|prawn|shrimp).*")) {
            return new ProteinAnalyzer();
        } else {
            return new GeneralFoodAnalyzer();
        }
    }
}