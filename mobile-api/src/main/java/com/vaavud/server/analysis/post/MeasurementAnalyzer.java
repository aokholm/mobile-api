package com.vaavud.server.analysis.post;

import java.util.ArrayList;
import java.util.List;

import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorListener;
import com.vaavud.sensor.SensorManager;
import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;
import com.vaavud.server.model.entity.MagneticSession;

public class MeasurementAnalyzer implements SensorListener {
    private List<SensorEvent> events;

    public MeasurementAnalyzer(MagneticSession magneticSession) {

        events = new ArrayList<SensorEvent>();

        RevSensorConfig config = new RevSensorConfig();
        config.revSensorUpdateRateUs = 100000; // 10 time a second
        SensorManager sensorManager = new SensorManager();
        sensorManager.addSensor(new RevolutionSensor(config));
        sensorManager.addSensor(new DatabaseSensor(magneticSession));
        sensorManager.addListener(this, new Sensor.Type[] { Type.FREQUENCY,
                Type.MAGNETIC_FIELD });

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
        return events;
    }
}
