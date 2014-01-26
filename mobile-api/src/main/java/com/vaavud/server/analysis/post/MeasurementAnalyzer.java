package com.vaavud.server.analysis.post;

import java.util.List;

import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorListener;
import com.vaavud.sensor.SensorManager;
import com.vaavud.sensor.SensorType;
import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;
import com.vaavud.server.model.entity.MagneticSession;

public class MeasurementAnalyzer implements SensorListener{  
  private List<SensorEvent> freqEvents;
  private List<SensorEvent> magEverts;

  public MeasurementAnalyzer (MagneticSession magneticSession) {
    RevSensorConfig config = new RevSensorConfig();
    config.revSensorUpdateRateUs=100000; // 10 time a second
    SensorManager sensorManager = new SensorManager();
    sensorManager.addSensor(new RevolutionSensor(config));
    sensorManager.addSensor(new DatabaseSensor(magneticSession));
    
    sensorManager.addListener(this, new SensorType[]{SensorType.TYPE_FREQUENCY, SensorType.TYPE_MAGNETIC_FIELD});
    
    sensorManager.start();
  }
  
  @Override
  public void newEvent(SensorEvent event) {
      
    switch (event.sensor) {
    case TYPE_FREQUENCY:
      freqEvents.add(event);
      break;
    case TYPE_MAGNETIC_FIELD:
      magEverts.add(event);
    default:
      System.out.println("Unexpected sensor: " + event.sensor);
      break;
    }
  }
  
  public List<SensorEvent> getFreqEvents() {
    return freqEvents;
  }

  public List<SensorEvent> getMagEverts() {
    return magEverts;
  }
}
