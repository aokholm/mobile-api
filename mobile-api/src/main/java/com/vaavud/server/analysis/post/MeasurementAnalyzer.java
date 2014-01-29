package com.vaavud.server.analysis.post;

import java.util.ArrayList;
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
  private List<SensorEvent> magEvents;
  private List<SensorEvent> freqEventsRef;

  public MeasurementAnalyzer (MagneticSession magneticSession) {
    freqEvents = new ArrayList<SensorEvent>();
    magEvents = new ArrayList<SensorEvent>();
    freqEventsRef = new ArrayList<SensorEvent>();
    
    RevSensorConfig config = new RevSensorConfig();
    config.revSensorUpdateRateUs=100000; // 10 time a second
    SensorManager sensorManager = new SensorManager();
    sensorManager.addSensor(new RevolutionSensor(config));
    sensorManager.addSensor(new DatabaseSensor(magneticSession));
    sensorManager.addListener(this, new SensorType[]{
        SensorType.TYPE_FREQUENCY,
        SensorType.TYPE_FREQUENCY_REF,
        SensorType.TYPE_MAGNETIC_FIELD});
    
    try {
      sensorManager.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void newEvent(SensorEvent event) {
      
    switch (event.sensor) {
    case TYPE_FREQUENCY:
      freqEvents.add(event);
      break;
    case TYPE_FREQUENCY_REF:
      freqEventsRef.add(event);
      break;
    case TYPE_MAGNETIC_FIELD:
      magEvents.add(event);
      break;
    default:
      System.out.println("UneXpected sensor: " + event.sensor);
      break;
    }
  }
  
  public List<SensorEvent> getFreqEvents() {
    return freqEvents;
  }

  public List<SensorEvent> getMagEverts() {
    return magEvents;
  }
  
  public List<SensorEvent> getFreqEventsRef() {
    return freqEventsRef;
  }
}
