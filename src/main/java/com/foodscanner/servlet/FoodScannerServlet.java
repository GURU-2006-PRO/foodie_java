package com.foodscanner.servlet;

import com.foodscanner.dao.ScanHistoryDAO;
import com.foodscanner.patterns.abstractfactory.ScannerServiceFactory;
import com.foodscanner.patterns.bridge.ReportFormatter;
import com.foodscanner.patterns.chain.*;
import com.foodscanner.patterns.factory.*;
import com.foodscanner.patterns.observer.ScanEventManager;
import com.foodscanner.patterns.proxy.GeminiApiProxy;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.*;

import java.io.*;
import java.util.*;

/**
 * Main Servlet — Orchestrates all 7 Design Patterns:
 * 1. Abstract Factory: Obtains the core service family
 * 2. Factory Method: Instantiates the dedicated image/food analyzer
 * 3. Proxy: Intercepts Gemini API calls with intelligent caching and security
 * 4. Bridge: Formats and normalizes Gemini output into structured JSON
 * 5. Chain of Responsibility: Evaluates condition-specific health warnings (Diabetes, BP, Obesity)
 * 6. Observer: Automatically broadcasts scan completion events
 * 7. Singleton: Coordinates database persistence via thread-safe connection pool
 */
@WebServlet(urlPatterns = {"/scan", "/history"})
public class FoodScannerServlet extends HttpServlet {

    private GeminiApiProxy geminiProxy;
    private ReportFormatter formatter;
    private ScanEventManager eventManager;
    private ScanHistoryDAO dao;
    private HealthCheckHandler healthChain;

    @Override
    public void init() {
        // 1. ABSTRACT FACTORY creates the service family
        ScannerServiceFactory factory = ScannerServiceFactory.getDefault();
        geminiProxy = factory.createApiProxy();
        formatter = factory.createReportFormatter();
        eventManager = factory.createEventManager();

        // 2. OBSERVER (DAO) subscribed to auto-persist scan events
        dao = new ScanHistoryDAO();
        eventManager.subscribe(dao);

        // 3. CHAIN OF RESPONSIBILITY — Health inspection pipeline
        HealthCheckHandler diabetes = new DiabetesHandler();
        HealthCheckHandler hypertension = new HypertensionHandler();
        HealthCheckHandler obesity = new ObesityHandler();
        HealthCheckHandler general = new GeneralHealthHandler();
        diabetes.setNext(hypertension).setNext(obesity).setNext(general);
        healthChain = diabetes;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if ("/history".equals(path)) {
            List<Map<String, String>> scans = dao.getRecentScans(20);
            resp.getWriter().write(new JSONArray(scans).toString());
            return;
        }

        resp.getWriter().write(new JSONObject()
            .put("status", "running")
            .put("endpoints", new JSONArray().put("POST /scan (Upload food image)").put("GET /history"))
            .toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String body = new String(req.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        if (body.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Request body is empty. Please provide image data.\"}");
            return;
        }

        JSONObject reqJson;
        try {
            reqJson = new JSONObject(body);
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid JSON payload.\"}");
            return;
        }

        String imageBase64 = reqJson.optString("image", "").trim();
        String mimeType = reqJson.optString("mimeType", "image/jpeg").trim();
        String note = reqJson.optString("note", "").trim();
        String foodNameInput = reqJson.optString("foodName", "").trim();

        boolean isImageUpload = !imageBase64.isBlank();

        if (!isImageUpload && foodNameInput.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Please select or drop a food image to upload and scan.\"}");
            return;
        }

        try {
            // 4. FACTORY METHOD — Select the appropriate analyzer
            NutrientAnalyzer analyzer = isImageUpload
                ? NutrientAnalyzerFactory.createImageAnalyzer()
                : NutrientAnalyzerFactory.createAnalyzer(foodNameInput);

            String prompt = analyzer.buildPrompt(isImageUpload ? note : foodNameInput);

            // 5. PROXY — Call Gemini API (with caching)
            String rawText;
            if (isImageUpload) {
                rawText = geminiProxy.callGeminiWithImage(prompt, mimeType, imageBase64);
            } else {
                rawText = geminiProxy.callGemini(prompt);
            }

            // 6. BRIDGE — Format & normalize output
            String formattedJson = formatter.format(rawText);

            JSONObject foodData;
            try {
                foodData = new JSONObject(formattedJson);
            } catch (JSONException e) {
                foodData = new JSONObject()
                    .put("food_name", isImageUpload ? "Scanned Food Item" : foodNameInput)
                    .put("raw_output", rawText);
            }

            String detectedFood = foodData.optString("food_name", isImageUpload ? "Identified Food" : foodNameInput);

            // 7. CHAIN OF RESPONSIBILITY — Condition-based health inspection
            List<String> warnings = new ArrayList<>();
            healthChain.handle(foodData, warnings);

            // 8. OBSERVER — Notify subscribers (ScanHistoryDAO persists to SQLite via SINGLETON)
            eventManager.notifyScanCompleted(detectedFood, foodData, String.join(" | ", warnings));

            // Construct client response
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("detected_food", detectedFood);
            response.put("category", foodData.optString("category", analyzer.getCategory()));
            response.put("data", foodData);
            response.put("health_warnings", new JSONArray(warnings));
            response.put("pattern_summary", new JSONObject()
                .put("factory_method", analyzer.getClass().getSimpleName())
                .put("proxy_cache", "Active (Multimodal cache)")
                .put("bridge_formatter", formatter.getClass().getSimpleName())
                .put("chain_of_responsibility", "Diabetes -> Hypertension -> Obesity -> General")
                .put("observer", "ScanEventManager -> ScanHistoryDAO")
                .put("singleton_db", "DatabaseConnection.getInstance()"));

            resp.getWriter().write(response.toString(2));

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(new JSONObject()
                .put("error", e.getMessage())
                .put("hint", "Verify your GEMINI_API_KEY environment variable is configured and valid.")
                .toString());
        }
    }
}