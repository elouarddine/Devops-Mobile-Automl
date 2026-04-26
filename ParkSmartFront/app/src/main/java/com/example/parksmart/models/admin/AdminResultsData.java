package com.example.parksmart.models.admin;

import java.util.Map;

public class AdminResultsData {
    private String mode;
    private Map<String, Double> metrics;
    private String summary;

    public String getMode() { return mode; }
    public Map<String, Double> getMetrics() { return metrics; }
    public String getSummary() { return summary; }
}
