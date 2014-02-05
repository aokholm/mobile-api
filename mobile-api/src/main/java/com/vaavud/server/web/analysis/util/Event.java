package com.vaavud.server.web.analysis.util;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorEvent1D;
import com.vaavud.server.model.entity.MeasurementPoint;

public class Event {

    
    public static List<SensorEvent> windEvents(List<MeasurementPoint> mesPoints, long msStartMills) {
        // create wind time array // MP.time-MS.startTime)/1000
        Sensor sensor = new Sensor(Type.WINDSPEED, "Original");
        List<SensorEvent> windEvents = new ArrayList<>(mesPoints.size());

        for (MeasurementPoint mesPoint : mesPoints) {
            long timeUs = (long) (mesPoint.getTime().getTime() - msStartMills) * 1_000;
            windEvents.add(new SensorEvent1D(sensor, timeUs, mesPoint.getWindSpeed()));
        }

        return windEvents;
    }
    
    public static EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap(List<SensorEvent> events) {
        EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap = new EnumMap<>(Sensor.Type.class);

        for (SensorEvent event : events) {
            Type eType = event.getSensor().getType();
            String eName = event.getSensor().getName();

            if (!eventMap.containsKey(eType)) {
                eventMap.put(eType, new HashMap<String, List<SensorEvent>>());
            }

            if (!eventMap.get(eType).containsKey(eName)) {
                eventMap.get(eType).put(eName, new ArrayList<SensorEvent>());
            }
            eventMap.get(eType).get(eName).add(event);
        }
        
        return eventMap;
    }
    
    
    public static List<SensorEvent> sortedList(EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap, Integer eventsSize) {

        List<SensorEvent> sortedEvents = new ArrayList<SensorEvent>(eventsSize);

        for (Sensor.Type sensorType : eventMap.keySet()) {
            for (List<SensorEvent> values : eventMap.get(sensorType).values())
                sortedEvents.addAll(values);
        }
        
        return sortedEvents;
    }
    

}
