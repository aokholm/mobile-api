package com.vaavud.sensor;

import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;
import com.vaavud.serial.SerialSensor;

public class Tester implements SensorListener {
  private SensorManager sensorManager;
  
  public boolean active;
  
  
  public Tester() {
      sensorManager = new SensorManager();
      sensorManager.addSensor(new RevolutionSensor(new RevSensorConfig()));
      sensorManager.addSensor(new SerialSensor());
      
      sensorManager.addListener(this, new Sensor.Type[]{Sensor.Type.FREQUENCY});
  }
  
  public void toogleOnOff() {
      if (active) {
          stop();
      }
      else {
          start();
      }
  }
  
  public void start() {
      
      try {
        sensorManager.start();
        active = true;
      } catch (Exception e) {
        active = false;
      }
  }
  
  public void stop() {
      try {
          sensorManager.stop();
      } catch (Exception e) {
          
      }
      active = false;
  }
  
  @Override
  public void newEvent(SensorEvent event) {
      
      if (event.getSensor().getType() == Sensor.Type.FREQUENCY) {
          System.out.println(event);
      }
      else {
          System.out.println("Unexpected sensor: " + event.getSensor());
      }
  }
  
}
