package com.foodscanner.patterns.bridge;

/**
 * BRIDGE PATTERN — Implementor Interface
 * Defines how data is rendered.
 */
public interface ReportOutputRenderer {
    String render(Object data);
}
