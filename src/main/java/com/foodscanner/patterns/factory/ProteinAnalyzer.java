package com.foodscanner.patterns.factory;

public class ProteinAnalyzer implements NutrientAnalyzer {
    @Override
    public String buildPrompt(String foodName) {
        return """
            Analyze the protein food: "%s"
            Provide a detailed JSON response with EXACTLY this structure:
            {
              "food_name": "%s",
              "category": "Protein/Meat",
              "serving_size": "100g",
              "nutrients": {
                "calories": "value in kcal",
                "protein": "value in g",
                "carbohydrates": "value in g",
                "fat": "value in g",
                "fiber": "value in g",
                "sugar": "value in g",
                "sodium": "value in mg",
                "potassium": "value in mg",
                "vitamin_b12": "value in mcg",
                "calcium": "value in mg",
                "iron": "value in mg"
              },
              "health_score": <integer 1-10>,
              "health_benefits": ["benefit1", "benefit2", "benefit3"],
              "health_warnings": {
                "diabetes": "warning or safe message",
                "hypertension": "warning about sodium/fat if applicable",
                "obesity": "warning or safe message",
                "general": "general health note"
              },
              "recommendation": "overall recommendation in 2 sentences"
            }
            Return ONLY the JSON, no extra text.
            """.formatted(foodName, foodName);
    }

    @Override
    public String getCategory() { return "Protein/Meat"; }
}
