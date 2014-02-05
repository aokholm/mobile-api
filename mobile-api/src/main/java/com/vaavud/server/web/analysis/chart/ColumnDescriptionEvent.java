package com.vaavud.server.web.analysis.chart;

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.vaavud.sensor.Sensor;

public class ColumnDescriptionEvent extends ColumnDescription {

    private Sensor.Type sensorType;
    private EventField eventField;
    private String sensorName;

    public ColumnDescriptionEvent(String label, Sensor.Type sensorType, EventField eventField, String sensorName) {
        // TODO Auto-generated constructor stub
        super(label, ValueType.NUMBER, label);
        this.sensorType = sensorType;
        this.eventField = eventField;
        this.sensorName = sensorName;
    }

    public Sensor.Type getSensorType() {
        return sensorType;
    }

    public EventField getEventField() {
        return eventField;
    }

    public String getSensorName() {
        return sensorName;
    }

}
