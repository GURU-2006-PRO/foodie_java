package com.foodscanner.patterns.proxy;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import org.json.*;

/**
 * PROXY PATTERN
 * Acts as a smart surrogate/proxy for the Google Gemini API:
 * - Intercepts requests to enforce caching (avoids duplicate billable calls)
 * - Safely retrieves API credentials from system environment variable (zero hardcoding)
 * - Supports multimodal image analysis and text prompts using Gemini 2.5 Flash
 */
public class GeminiApiProxy {

    private static final String PRIMARY_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final String FALLBACK_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final String apiKey;

    public GeminiApiProxy() {
        String key = System.getenv("GEMINI_API_KEY");
        this.apiKey = (key != null) ? key.trim() : "";
        if (this.apiKey.isBlank()) {
            System.out.println("[FoodScan Proxy] GEMINI_API_KEY not configured. Running in Demo/Simulation Mode.");
        }
    }

    public boolean isConfigured() {
        return this.apiKey != null && !this.apiKey.isBlank();
    }

    /**
     * Text-only call with proxy caching.
     */
    public String callGemini(String prompt) throws IOException {
        String cacheKey = "text:" + hashString(prompt.strip().toLowerCase());
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        if (!isConfigured()) {
            return generateMockResponse(prompt);
        }

        JSONObject body = buildTextPayload(prompt);
        String result = executePostWithFallback(body);
        cache.put(cacheKey, result);
        return result;
    }

    /**
     * Multimodal Image + Prompt call with proxy caching.
     */
    public String callGeminiWithImage(String prompt, String mimeType, String base64Data) throws IOException {
        String effectiveMime = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";
        String rawBase64 = stripBase64Header(base64Data);

        String cacheKey = "img:" + hashString(effectiveMime + ":" + rawBase64.length() + ":" + prompt);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        if (!isConfigured()) {
            return generateMockResponse(prompt);
        }

        JSONObject body = buildMultimodalPayload(prompt, effectiveMime, rawBase64);
        String result = executePostWithFallback(body);
        cache.put(cacheKey, result);
        return result;
    }

    private String generateMockResponse(String prompt) {
        String lower = (prompt != null) ? prompt.toLowerCase() : "";
        if (lower.contains("pizza") || lower.contains("burger") || lower.contains("fry") || lower.contains("fast food")) {
            return """
                {
                  "food_name": "Cheese Burger & French Fries (Demo Mode)",
                  "category": "Fast Food",
                  "serving_size": "1 serving (320g)",
                  "nutrients": {
                    "calories": "750 kcal",
                    "protein": "28g",
                    "carbohydrates": "68g",
                    "fat": "38g",
                    "fiber": "4g",
                    "sugar": "12g",
                    "sodium": "1420mg",
                    "potassium": "410mg",
                    "calcium": "180mg",
                    "iron": "4.5mg",
                    "vitamin_c": "6mg"
                  },
                  "health_score": 4,
                  "health_benefits": [
                    "Provides quick dietary energy and substantial protein",
                    "Contains iron and calcium from dairy and meat components"
                  ],
                  "health_warnings": {
                    "diabetes": "Moderate glycemic load. Monitor portion size to avoid postprandial glucose spikes.",
                    "hypertension": "High sodium warning (1420mg)! Exceeds 60% of daily recommended intake.",
                    "obesity": "High caloric density and saturated fat content; limit consumption during weight loss.",
                    "general": "Deep-fried fast food items contain trans-fats; consume infrequently."
                  },
                  "recommendation": "High calorie and high sodium fast food option. Consume sparingly and balance with fresh fruits or greens."
                }
                """;
        } else if (lower.contains("donut") || lower.contains("cake") || lower.contains("sweet") || lower.contains("soda") || lower.contains("dessert")) {
            return """
                {
                  "food_name": "Chocolate Glazed Donut (Demo Mode)",
                  "category": "Dessert",
                  "serving_size": "1 piece (85g)",
                  "nutrients": {
                    "calories": "360 kcal",
                    "protein": "4g",
                    "carbohydrates": "46g",
                    "fat": "18g",
                    "fiber": "1.5g",
                    "sugar": "29g",
                    "sodium": "260mg",
                    "potassium": "110mg",
                    "calcium": "45mg",
                    "iron": "1.8mg",
                    "vitamin_c": "0mg"
                  },
                  "health_score": 3,
                  "health_benefits": [
                    "Rapid carbohydrate replenishment for immediate energy"
                  ],
                  "health_warnings": {
                    "diabetes": "High sugar content (29g)! Persons with diabetes must AVOID or strictly limit this item.",
                    "hypertension": "Moderate sodium, but high saturated fat may adversely affect cardiovascular profile.",
                    "obesity": "Empty calories with negligible satiety index; promotes fat storage.",
                    "general": "Refined flour and simple sugars trigger swift insulin surges."
                  },
                  "recommendation": "High sugar and refined carbohydrate dessert. Avoid for diabetes management and weight control."
                }
                """;
        } else {
            return """
                {
                  "food_name": "Mediterranean Quinoa Bowl (Demo Mode)",
                  "category": "Protein/Vegetable",
                  "serving_size": "1 bowl (350g)",
                  "nutrients": {
                    "calories": "380 kcal",
                    "protein": "26g",
                    "carbohydrates": "42g",
                    "fat": "11g",
                    "fiber": "8g",
                    "sugar": "5g",
                    "sodium": "340mg",
                    "potassium": "680mg",
                    "calcium": "95mg",
                    "iron": "3.8mg",
                    "vitamin_c": "28mg"
                  },
                  "health_score": 9,
                  "health_benefits": [
                    "Rich in complete plant protein and dietary fiber supporting metabolic health",
                    "High potassium and antioxidants promote cardiovascular wellness",
                    "Low glycemic index provides steady, sustained energy release"
                  ],
                  "health_warnings": {
                    "diabetes": "Low sugar content (5g). Safe in moderation with excellent complex carbohydrates.",
                    "hypertension": "Low sodium content (340mg). Heart-friendly and aligned with DASH diet.",
                    "obesity": "High satiety index with low caloric density; ideal for weight management.",
                    "general": "Wholesome natural ingredients with zero artificial additives."
                  },
                  "recommendation": "Nutritionally balanced meal with optimal macronutrient profile. Highly recommended for daily wellness and blood sugar regulation."
                }
                """;
        }
    }

    private JSONObject buildTextPayload(String prompt) {
        JSONObject body = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();

        parts.put(new JSONObject().put("text", prompt));
        content.put("parts", parts);
        contents.put(content);
        body.put("contents", contents);
        return body;
    }

    private JSONObject buildMultimodalPayload(String prompt, String mimeType, String base64Data) {
        JSONObject body = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();

        // Text prompt
        parts.put(new JSONObject().put("text", prompt));

        // Multimodal inline image
        JSONObject inlineData = new JSONObject();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Data);
        parts.put(new JSONObject().put("inline_data", inlineData));

        content.put("parts", parts);
        contents.put(content);
        body.put("contents", contents);
        return body;
    }

    private String executePostWithFallback(JSONObject body) throws IOException {
        try {
            return executePost(PRIMARY_API_URL, body);
        } catch (IOException e) {
            // If primary endpoint fails, try fallback
            return executePost(FALLBACK_API_URL, body);
        }
    }

    private String executePost(String endpointUrl, JSONObject body) throws IOException {
        URL url = new URL(endpointUrl + "?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(45000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code < 400) ? conn.getInputStream() : conn.getErrorStream();
        String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        if (code != 200) {
            throw new IOException("Gemini API error (" + code + "): " + response);
        }

        JSONObject resp = new JSONObject(response);
        JSONArray candidates = resp.optJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IOException("No candidates returned by Gemini: " + response);
        }

        return candidates.getJSONObject(0)
                         .getJSONObject("content")
                         .getJSONArray("parts")
                         .getJSONObject(0)
                         .getString("text");
    }

    private String stripBase64Header(String base64) {
        if (base64 == null) return "";
        int commaIndex = base64.indexOf(",");
        if (commaIndex != -1 && base64.substring(0, commaIndex).contains("base64")) {
            return base64.substring(commaIndex + 1).trim();
        }
        return base64.trim();
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    public void clearCache() {
        cache.clear();
    }
}