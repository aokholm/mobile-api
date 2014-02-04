package com.vaavud.server.analysis.post.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class ChartConfig {
    private final DataSet x;
    private final List<DataSet> yList;
    
    public ChartConfig(DataSet x, DataSet... yList) {
        this.x = x;
        this.yList = new ArrayList<>(Arrays.asList(yList));
    }
    
    public DataSet getX() {
        return x;
    }
    public List<DataSet> getyList() {
        return yList;
    }
    
    
    
}
