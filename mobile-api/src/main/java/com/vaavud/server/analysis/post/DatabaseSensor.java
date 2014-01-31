package com.vaavud.server.analysis.post;

import java.util.List;

import com.vaavud.sensor.BaseSensor;
import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorListener;
import com.vaavud.server.model.entity.MagneticPoint;
import com.vaavud.server.model.entity.MagneticSession;

public class DatabaseSensor extends BaseSensor {
  
  private MagneticSession magneticSession;
  private SensorListener listener;
  private Sensor sensor;

  public DatabaseSensor(MagneticSession magneticSession) {
    this.magneticSession = magneticSession;
    this.sensor = new Sensor(Type.MAGNETIC_FIELD, "Database");
  }
  
  
  @Override
  public void setReciever(SensorListener listener) {
    this.listener = listener;
  }
    
  @Override
  public void start() {

    List<MagneticPoint>  magPoints = magneticSession.getMagneticPoints();  
    
    for (MagneticPoint magPoint : magPoints) {
      SensorEvent event = new SensorEvent(sensor, (long) ( magPoint.getTime()*1000000 ), 
          new double[] {magPoint.getX(), magPoint.getY(), magPoint.getZ()});
      listener.newEvent(event);
    }
    
  }

  @Override
  public void stop() {
    // do nothing
  }

}
