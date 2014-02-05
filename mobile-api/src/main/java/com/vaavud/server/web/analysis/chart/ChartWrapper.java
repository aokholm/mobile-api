package com.vaavud.server.web.analysis.chart;

public class ChartWrapper {
    
    private final String chartJSON;
    private final String identifier;
    
    public ChartWrapper(String identifier, String chartJSON) {
        this.chartJSON = chartJSON;
        this.identifier = identifier;
    }

    public String getChartJSON() {
        return chartJSON;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
}
