package com.foodscanner.patterns.proxy;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Properties;
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
        this.apiKey = resolveApiKey();
        if (this.apiKey.isBlank()) {
            System.err.println("[FoodScan Proxy] WARNING: No GEMINI_API_KEY detected in config.properties, .env, or system environment.");
        } else {
            System.out.println("[FoodScan Proxy] GEMINI_API_KEY loaded successfully from configuration.");
        }
    }

    private String resolveApiKey() {
        // 1. System property
        String key = System.getProperty("GEMINI_API_KEY");
        if (key != null && !key.isBlank()) return key.trim();

        // 2. System environment variable
        key = System.getenv("GEMINI_API_KEY");
        if (key != null && !key.isBlank()) return key.trim();

        // 3. config.properties in project root
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (InputStream is = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(is);
                key = props.getProperty("GEMINI_API_KEY");
                if (key != null && !key.isBlank()) return key.trim();
            } catch (Exception ignored) {}
        }

        // 4. .env in project root
        File envFile = new File(".env");
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("GEMINI_API_KEY=")) {
                        key = line.substring("GEMINI_API_KEY=".length()).trim();
                        if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
                            key = key.substring(1, key.length() - 1);
                        }
                        if (!key.isBlank()) return key.trim();
                    }
                }
            } catch (Exception ignored) {}
        }

        return "";
    }

    public boolean isConfigured() {
        return this.apiKey != null && !this.apiKey.isBlank();
    }

    /**
     * Text-only call with proxy caching.
     */
    public String callGemini(String prompt) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured! Please save your API key in config.properties (GEMINI_API_KEY=your_key) or set the GEMINI_API_KEY environment variable.");
        }

        String cacheKey = "text:" + hashString(prompt.strip().toLowerCase());
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
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
        if (!isConfigured()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured! Please save your API key in config.properties (GEMINI_API_KEY=your_key) or set the GEMINI_API_KEY environment variable.");
        }

        String effectiveMime = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";
        String rawBase64 = stripBase64Header(base64Data);

        String cacheKey = "img:" + hashString(effectiveMime + ":" + rawBase64.length() + ":" + prompt);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        JSONObject body = buildMultimodalPayload(prompt, effectiveMime, rawBase64);
        String result = executePostWithFallback(body);
        cache.put(cacheKey, result);
        return result;
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