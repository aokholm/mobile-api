package com.vaavud.sensor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.Session;

import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;
import com.vaavud.serial.SerialSensor;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MagneticPoint;
import com.vaavud.server.model.entity.MagneticSession;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.util.UUIDUtil;

public class Tester implements SensorListener {
  private SensorManager sensorManager;
  
  private List<SensorEvent> sensorEvents;
  public boolean active;
  private MeasurementSession measurementSession;
  private MagneticSession magneticSession;
  
  private double freqSum;
  private double freqMax;
  
  public Tester() {
      sensorManager = new SensorManager();
      RevSensorConfig revSensorConfig = new RevSensorConfig();
      revSensorConfig.setWindtunnelTest(true);
      revSensorConfig.setRevSensorRateUs(500_000);
      sensorManager.addSensor(new RevolutionSensor(revSensorConfig));
      sensorManager.addSensor(new SerialSensor());
      
      sensorManager.addListener(this, new Sensor.Type[]{Sensor.Type.FREQUENCY, Sensor.Type.MAGNETIC_FIELD});
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
      
      sensorEvents = new ArrayList<>();
      
      try {
          sensorManager.start();
          active = true;
      } catch (Exception e) {
          active = false;
          throw new RuntimeException(e.getMessage() + ". Could not start sensor manager");
      }
      
      measurementSession = new MeasurementSession();
      measurementSession.setUuid(UUIDUtil.generateUUID());
      measurementSession.setSource("WindTunnel");
      measurementSession.setStartTime(new Date());
      measurementSession.setTimezoneOffset((long) TimeZone.getDefault().getOffset(measurementSession.getStartTime().getTime()));
      measurementSession.setEndTime(measurementSession.getStartTime());
      measurementSession.setMeasuring(true);
      measurementSession.setUploaded(false);
      measurementSession.setStartIndex(0);
      measurementSession.setEndIndex(0);
      measurementSession.setPosition(null);
      
      
      magneticSession= new MagneticSession();
      magneticSession.setStartIndex(0);
      magneticSession.setMeasurementSessionUuid(measurementSession.getUuid());
  }
  
  public void stop() {
      try {
          sensorManager.stop();          
      } catch (Exception e) {
          throw new RuntimeException(e.getMessage() + ". Could not stop sensor manager");
      }
      
      active = false;
      
      try {
          uploadMeasurement();
      } catch (Exception e) {
          throw new RuntimeException(e.getMessage() + ". Could not upload measurement Sessions");
      }
      
      freqSum = 0;
      freqMax = 0;

  }
  
  @Override
  public void newEvent(SensorEvent event) {
      
      switch (event.getSensor().getType()) {
        case FREQUENCY:
            SensorEventFreq eventfreq = (SensorEventFreq) event;
            
            sensorEvents.add(eventfreq);
            
            MeasurementPoint mp = new MeasurementPoint();
            mp.setSession(measurementSession);
            mp.setTime(new Date());
            mp.setWindSpeed((float) (double) eventfreq.getFreq());
            measurementSession.getPoints().add(mp);
            
            freqSum += eventfreq.getFreq();
            if (eventfreq.getFreq() > freqMax) {
                freqMax = eventfreq.getFreq();
            }
            
            System.out.println(event);
            break;
    
        case MAGNETIC_FIELD:
            SensorEvent3D event3d = (SensorEvent3D) event;
            magneticSession.getMagneticPoints().add(
                    new MagneticPoint(magneticSession, 
                            magneticSession.getMagneticPoints().size(), 
                            new Float[] {event3d.getTime().floatValue(),
                            event3d.getX().floatValue(), event3d.getY().floatValue(), event3d.getZ().floatValue()}));

            break;
        default:
            System.out.println("Unexpected sensor: " + event.getSensor());
            break;
        }
     
      
          
  }
  
  
  private void uploadMeasurement(){
      
      
      
      Session hibernateSession = Model.get().getSessionFactory().openSession();
      try {
         
          hibernateSession.beginTransaction();
          
          Device device = (Device) hibernateSession
                  .createQuery("from Device where id=:id")
                  .setString("id", "4325")
                  .uniqueResult();
          measurementSession.setDevice(device);
          
          measurementSession.setEndTime(new Date());
          measurementSession.setMeasuring(false);
          
          measurementSession.setEndIndex(measurementSession.getPoints().size());
          
          double freqAvg = freqSum / measurementSession.getPoints().size();
          measurementSession.setWindSpeedAvg((float) freqAvg); 
          measurementSession.setWindSpeedMax((float) freqMax);
          
          hibernateSession.save(measurementSession);
          hibernateSession.getTransaction().commit();
          
          
          hibernateSession.beginTransaction();

          magneticSession.setEndIndex(magneticSession.getMagneticPoints().size());
          
          hibernateSession.save(magneticSession);          
          hibernateSession.getTransaction().commit();
          
          
      }
      catch (RuntimeException e) {
          throw new RuntimeException("Error processing service " + getClass().getName(), e);
      }
      finally {
          if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
              hibernateSession.getTransaction().rollback();
          }
          hibernateSession.close();
      }
  }
    
}
