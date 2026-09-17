package com.foodscanner.patterns.bridge;

/**
 * BRIDGE PATTERN — Abstraction
 * Decouples the report logic from the output format (JSON, HTML, plain text).
 */
public abstract class ReportFormatter {

    protected ReportOutputRenderer renderer;

    public ReportFormatter(ReportOutputRenderer renderer) {
        this.renderer = renderer;
    }

    public abstract String format(String jsonData);
}
