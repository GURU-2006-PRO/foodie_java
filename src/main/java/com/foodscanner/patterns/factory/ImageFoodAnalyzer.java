package com.foodscanner.patterns.factory;

/**
 * FACTORY METHOD PATTERN — Concrete Product for Image Scans
 * Formulates the multimodal prompt for Gemini Vision to analyze food photos.
 */
public class ImageFoodAnalyzer implements NutrientAnalyzer {

    @Override
    public String buildPrompt(String contextHint) {
        String hintText = (contextHint != null && !contextHint.isBlank())
            ? " Optional user note about the image: \"" + contextHint + "\"."
            : "";

        return """
            You are an expert clinical nutritionist and food scientist.
            Analyze the food or meal displayed in this uploaded image.%s
            Identify the food accurately and calculate its estimated nutritional composition per serving.

            CRITICAL: Assess health risks for common conditions including DIABETES (check sugar, carbs, glycemic impact),
            HYPERTENSION (check sodium, saturated fat), and OBESITY (check caloric density).
            Clearly state if a person with diabetes or high blood sugar should avoid or limit it.

            Respond with a strict JSON object having EXACTLY this schema:
            {
              "food_name": "Identified Food Name",
              "category": "e.g., Fruit, Beverage, Bakery, Fast Food, Protein/Meat, Vegetable, Dairy, Grain, Dessert",
              "serving_size": "e.g., 1 medium piece (150g) or 1 plate (300g)",
              "nutrients": {
                "calories": "value with unit, e.g. 210 kcal",
                "protein": "value in g, e.g. 5g",
                "carbohydrates": "value in g, e.g. 35g",
                "fat": "value in g, e.g. 7g",
                "fiber": "value in g, e.g. 3g",
                "sugar": "value in g, e.g. 22g",
                "sodium": "value in mg, e.g. 180mg",
                "potassium": "value in mg, e.g. 250mg",
                "calcium": "value in mg",
                "iron": "value in mg",
                "vitamin_c": "value in mg"
              },
              "health_score": <integer from 1 to 10>,
              "health_benefits": [
                "Key benefit 1",
                "Key benefit 2",
                "Key benefit 3"
              ],
              "health_warnings": {
                "diabetes": "Explicit instruction. If high in sugar or carbs, say: High sugar content - Persons with diabetes must AVOID or strictly limit this item. Otherwise state if it is safe in moderation.",
                "hypertension": "Explicit sodium and blood pressure warning or clearance.",
                "obesity": "Calorie density and weight management advice.",
                "general": "General health remark or allergen warning."
              },
              "recommendation": "Overall clinical dietitian verdict in 2-3 concise sentences."
            }

            Return ONLY raw valid JSON. Do not include markdown ticks (```json), prefixes, or trailing notes.
            """.formatted(hintText);
    }

    @Override
    public String getCategory() {
        return "Image Food Scan";
    }
}