package com.vaavud.server.web.analysis;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.server.web.analysis.chart.Chart;
import com.vaavud.server.web.analysis.chart.ChartConfig;
import com.vaavud.server.web.analysis.util.Event;

public class MeasurementView {

    protected EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap;
    protected Chart chart = null;
    protected List<SensorEvent> sortedEvents;
    private List<ChartConfig> chartConfigs;
    
    
    public MeasurementView(List<ChartConfig> chartConfigs) {
        this.chartConfigs = chartConfigs;
    }
    
    protected void generateCharts(List<SensorEvent> events) {
        eventMap = Event.eventMap(events);
        sortedEvents = Event.sortedList(eventMap, events.size());

        chart = new Chart(chartConfigs, eventMap, sortedEvents);
    }
    
    public Chart getChart() {
        return chart;
    }    
}
