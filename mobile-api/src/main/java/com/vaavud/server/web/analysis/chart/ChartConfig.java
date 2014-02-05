package com.vaavud.server.web.analysis.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class ChartConfig {
    
    private final String chartId;
    private final DataSet x;
    private final List<DataSet> yList;
    private List<Integer> colIndex;
    
    public ChartConfig(String chartId, DataSet x, DataSet... yList) {
        this.chartId = chartId;
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
}
