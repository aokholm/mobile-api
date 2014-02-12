package com.vaavud.server.analysis.post;

import java.util.ArrayList;
import java.util.List;

import com.vaavud.sensor.BaseSensor;
import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorListener;
import com.vaavud.sensor.SensorManager;
import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;

public class MeasurementAnalyzer implements SensorListener {
    private List<SensorEvent> events;
    private SensorManager sensorManager;

    public MeasurementAnalyzer(Sensor.Type ... sensorTypes) {

        events = new ArrayList<SensorEvent>();

        RevSensorConfig config = new RevSensorConfig();
        config.revSensorUpdateRateUs = 100_000; // 10 time a second
        sensorManager = new SensorManager();
        sensorManager.addSensor(new RevolutionSensor(config));
        sensorManager.addListener(this, sensorTypes);


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
