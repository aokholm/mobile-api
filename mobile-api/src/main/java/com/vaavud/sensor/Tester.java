package com.vaavud.sensor;

import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;
import com.vaavud.serial.SerialSensor;

public class Tester implements SensorListener {
  private SensorManager sensorManager;
  
  public Tester() {
      RevSensorConfig config = new RevSensorConfig();
      config.revSensorUpdateRateUs=100000; // once a second
      sensorManager = new SensorManager();
      sensorManager.addSensor(new RevolutionSensor(config));
      sensorManager.addSensor(new SerialSensor());
      
      sensorManager.addListener(this, new SensorType[]{SensorType.TYPE_FREQUENCY});
      
      sensorManager.start();
  }
  
  @Override
  public void newEvent(SensorEvent event) {
      
      if (event.sensor == SensorType.TYPE_FREQUENCY) {
          System.out.println(event);
      }
      else {
          System.out.println("Unexpected sensor: " + event.sensor);
      }
  }
  
}
