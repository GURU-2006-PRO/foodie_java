package com.foodscanner.patterns.bridge;

import org.json.*;

/**
 * BRIDGE PATTERN — Concrete Implementor
 * Renders the report as a JSON string.
 */
public class JsonRenderer implements ReportOutputRenderer {
    @Override
    public String render(Object data) {
        if (data instanceof JSONObject obj) {
            return obj.toString(2);
        }
        return data.toString();
    }
}
