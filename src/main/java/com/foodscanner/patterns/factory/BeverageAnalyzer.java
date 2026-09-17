package com.foodscanner.patterns.factory;

public class BeverageAnalyzer implements NutrientAnalyzer {
    @Override
    public String buildPrompt(String foodName) {
        return """
            Analyze the beverage: "%s"
            Provide a detailed JSON response with EXACTLY this structure:
            {
              "food_name": "%s",
              "category": "Beverage",
              "serving_size": "250ml",
              "nutrients": {
                "calories": "value in kcal",
                "protein": "value in g",
                "carbohydrates": "value in g",
                "fat": "value in g",
                "fiber": "value in g",
                "sugar": "value in g",
                "sodium": "value in mg",
                "potassium": "value in mg",
                "vitamin_c": "value in mg",
                "calcium": "value in mg",
                "caffeine": "value in mg if applicable"
              },
              "health_score": <integer 1-10>,
              "health_benefits": ["benefit1", "benefit2", "benefit3"],
              "health_warnings": {
                "diabetes": "specific warning if sugary",
                "hypertension": "caffeine/sodium warning if applicable",
                "obesity": "calorie warning if applicable",
                "general": "general health note"
              },
              "recommendation": "overall recommendation in 2 sentences"
            }
            Return ONLY the JSON, no extra text.
            """.formatted(foodName, foodName);
    }

    @Override
    public String getCategory() { return "Beverage"; }
}
