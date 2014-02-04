package com.vaavud.server.analysis.post.chart;

import com.vaavud.sensor.Sensor;
import com.vaavud.server.analysis.post.EventField;

public class DataSet {
    
    private final Sensor.Type sensorType;
    private final EventField eventField;
    private final String sensorName;
    
    public DataSet(Sensor.Type sensorType, EventField eventField, String sensorName) {
        this.sensorType = sensorType;
        this.sensorName = sensorName;
        this.eventField = eventField;
    }

    public EventField getEventField() {
        return eventField;
    }
    
    public String getSensorName() {
        return sensorName;
    }
    
    public Sensor.Type getSensorType() {
        return sensorType;
    }
}
