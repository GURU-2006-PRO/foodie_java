package com.foodscanner.patterns.abstractfactory;

import com.foodscanner.patterns.proxy.GeminiApiProxy;
import com.foodscanner.patterns.bridge.*;
import com.foodscanner.patterns.observer.ScanEventManager;

/**
 * ABSTRACT FACTORY PATTERN — Abstract Factory
 * Defines the family of services needed for a food scan session.
 */
public abstract class ScannerServiceFactory {

    public abstract GeminiApiProxy createApiProxy();
    public abstract ReportFormatter createReportFormatter();
    public abstract ScanEventManager createEventManager();

    /**
     * Factory method to get the default production factory.
     */
    public static ScannerServiceFactory getDefault() {
        return new DefaultScannerServiceFactory();
    }
}
