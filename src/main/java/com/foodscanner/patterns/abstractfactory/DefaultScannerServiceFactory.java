package com.foodscanner.patterns.abstractfactory;

import com.foodscanner.patterns.proxy.GeminiApiProxy;
import com.foodscanner.patterns.bridge.*;
import com.foodscanner.patterns.observer.ScanEventManager;

/**
 * ABSTRACT FACTORY PATTERN — Concrete Factory
 * Creates the real production services.
 */
public class DefaultScannerServiceFactory extends ScannerServiceFactory {

    @Override
    public GeminiApiProxy createApiProxy() {
        return new GeminiApiProxy();
    }

    @Override
    public ReportFormatter createReportFormatter() {
        return new FoodReportFormatter(new JsonRenderer());
    }

    @Override
    public ScanEventManager createEventManager() {
        return new ScanEventManager();
    }
}
