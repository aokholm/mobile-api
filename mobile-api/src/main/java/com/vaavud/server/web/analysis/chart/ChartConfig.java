package com.vaavud.server.web.analysis.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChartConfig {
    
    private final String chartId;
    private final DataSet x;
    private final List<DataSet> yList;
    private List<Integer> colIndex;
    private int lineWidth = 1;
    private int pointSize = 1;
    private String vAxisTitle;
    
    
    public ChartConfig(String chartId, DataSet x, DataSet... yList) {
        this.chartId = chartId;
        this.vAxisTitle = chartId;
        this.x = x;
        this.yList = new ArrayList<>(Arrays.asList(yList));
        colIndex = new ArrayList<>();
    }
    
    public String getChartId() {
        return chartId;
    }
    
    public DataSet getX() {
        return x;
    }

    public List<DataSet> getyList() {
        return yList;
    }
    
    public void addColIndex(Integer col) {
        colIndex.add(col);
    }
    
    public List<Integer> getColIndex() {
        return colIndex;
    }
    
    public int getLineWidth() {
        return lineWidth;
    }
    
    public int getPointSize() {
        return pointSize;
    }
    
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
    
    public void setPointSize(int pointSize) {
        this.pointSize = pointSize;
    }
    
    public void setvAxisTitle(String vAxisTitle) {
        this.vAxisTitle = vAxisTitle;
    }
    
    public String getvAxisTitle() {
        return vAxisTitle;
    }
}
