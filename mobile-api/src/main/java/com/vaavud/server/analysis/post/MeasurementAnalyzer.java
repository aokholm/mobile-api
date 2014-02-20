package com.vaavud.server.analysis.post;

import java.util.ArrayList;
import java.util.List;

import com.vaavud.sensor.BaseSensor;
import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorListener;
import com.vaavud.sensor.SensorManager;
import com.vaavud.sensor.frequency.FrequencySensor;
import com.vaavud.sensor.ref.revolution.RevolutionSensorRef;
import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;

public class MeasurementAnalyzer implements SensorListener {
    private List<SensorEvent> events;
    private SensorManager sensorManager;

    public MeasurementAnalyzer(RevSensorConfig config, Sensor.Type ... sensorTypes) {

        events = new ArrayList<SensorEvent>();
        sensorManager = new SensorManager();
        sensorManager.addSensor(new RevolutionSensor(config));
        sensorManager.addSensor(new RevolutionSensorRef(config));
        sensorManager.addSensor(new FrequencySensor());
        sensorManager.addListener(this, sensorTypes);
    }
    
    public MeasurementAnalyzer(Sensor.Type ... sensorTypes) {
        this(new RevSensorConfig(), sensorTypes);
    }
    
    public void addSensor(BaseSensor sensor) {
        sensorManager.addSensor(sensor);
    }
    
    private void start() {
        try {
            sensorManager.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newEvent(SensorEvent event) {
        events.add(event);
    }

    public List<SensorEvent> getEvents() {
        if (events.size() == 0) {
            start();
        }
        return events;
    }
}
