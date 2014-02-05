package com.vaavud.server.web.analysis.chart;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.JsonRenderer;
import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.server.analysis.post.ColumnDescriptionEvent;

public class Chart {
    private final List<ChartConfig> chartConfigs;
    private final EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap;
    private final List<SensorEvent> sortedEvents;
    
    public Chart(List<ChartConfig> chartConfigs,
            EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap,
            List<SensorEvent> sortedEvents) {
        this.chartConfigs = chartConfigs;
        this.eventMap = eventMap;
        this.sortedEvents = sortedEvents;
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ChartWrapper> chartsWrapped() {
        
        List<ChartWrapper> chartsWrapped = new ArrayList<>();
        
        for (ChartConfig chartConfig : chartConfigs) {
            Map chartMap = new HashMap<String, Object>();
            
            chartMap.put("chartType", "ScatterChart");
            chartMap.put("containerId", chartConfig.getChartId());
            
            // options
            Map options = new HashMap<String, Object>();
            options.put("lineWidth", 1);
            options.put("pointSize", 1);
            
            Map vAxis = new HashMap<String, String>();
            vAxis.put("title", chartConfig.getChartId());
            options.put("vAxis", vAxis);
            chartMap.put("options", options);
            
            // view
            Map view = new HashMap<String, Object>();
            view.put("columns", chartConfig.getColIndex());
            chartMap.put("view", view);
            
            JSONObject chart = (JSONObject) JSONSerializer.toJSON( chartMap );
            
            chartsWrapped.add(new ChartWrapper(chartConfig.getChartId(), chart.toString()));
        }
        
        return chartsWrapped;
    }
    
    public DataTable dataTable() {
        // Create a data table,
        DataTable data = new DataTable();
        ArrayList<ColumnDescription> cds = new ArrayList<ColumnDescription>();

        cds.addAll(generateColumnDescriptions());

        data.addColumns(cds);

        // Fill the data table.
        try {
            for (SensorEvent event : sortedEvents) {
                Object[] row = new Object[cds.size()];

                int i = 0;
                for (ColumnDescription cd : cds) {
                    ColumnDescriptionEvent colDes = (ColumnDescriptionEvent) cd;
                    if (colDes.getSensorType() == null) {
                        row[i] = colDes.getEventField().get(event);
                    } else if (colDes.getSensorType() == event.getSensor().getType()) {
                        if (colDes.getSensorName() == null || colDes.getSensorName() == event.getSensor().getName()) {
                            row[i] = colDes.getEventField().get(event);
                        }
                    }
                    i++;
                }

                data.addRowFromValues(row);
            }

        } catch (TypeMismatchException e) {
            System.out.println("Invalid type!");
        }
        return data;
    }
    
    public String dataTableJSON() {
        // public static CharSequence renderDataTable(DataTable dataTable,
        // boolean includeValues, boolean includeFormatting, boolean
        // renderDateAsDateConstructor)
        return JsonRenderer.renderDataTable(dataTable(), true, true, false).toString();
    }
    
    
    private ArrayList<ColumnDescription> generateColumnDescriptions() {
        List<DataSet> dataSets = getColumns();
        
        
        ArrayList<ColumnDescription> cds = new ArrayList<ColumnDescription>();
        
        for (DataSet dataSet : dataSets) {
            StringBuilder sb = new StringBuilder(20);
            
            if(dataSet.getSensorType() != null) {
                sb.append(getName(dataSet.getSensorType(), 4));
            }
            
            if(dataSet.getEventField() != null) {
                if (sb.length() != 0) sb.append("-");
                sb.append(getName(dataSet.getEventField(), 4));
            }
            
            if(dataSet.getSensorName() != null) {
                if (sb.length() != 0) sb.append("-");
                sb.append(getName(dataSet.getSensorName(), 8));
            }
            
            cds.add(new ColumnDescriptionEvent(sb.toString(), dataSet.getSensorType(), dataSet.getEventField(), dataSet.getSensorName()));
        }
        
        return cds;
    }
    
    
    private List<DataSet> getColumns() {
        
        List<DataSet> dataSets = new ArrayList<>();
        
        for (ChartConfig chartConfig : chartConfigs) {
            
            List<DataSet> chartDataSets = new ArrayList<>();
            chartDataSets.add(chartConfig.getX());
            chartDataSets.addAll(chartConfig.getyList());
            
            for (DataSet chartDataSet : chartDataSets) {
                if (!dataSets.contains( chartDataSet )) {
                    
                    if (eventMap.containsKey(chartDataSet.getSensorType()) || chartDataSet.getSensorType() == null) {
                        if (chartDataSet.getSeperateNames()) {
                            for (String sensorName : eventMap.get(chartDataSet.getSensorType()).keySet()) {
                                DataSet dataSet = new DataSet(chartDataSet.getSensorType(), chartDataSet.getEventField(), sensorName);
                                dataSets.add(dataSet);
                                chartConfig.addColIndex(dataSets.size()-1);
                            }
                        }
                        else {
                            dataSets.add(chartDataSet);
                            chartConfig.addColIndex(dataSets.size()-1);  
                        }
                    }
                }
                else {
                    chartConfig.addColIndex(dataSets.indexOf(chartDataSet));
                }
            }
        }        
        return dataSets;
    }
    
    private static String getName(String string, int maxLength) {
        if (string.length() >  maxLength ) {
            return string.substring(0, maxLength);
        }
        else {
            return string;
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static String getName(Enum enumerate, int maxLength) {
        return getName(enumerate.toString(), maxLength);
    }

}
