package com.vaavud.server.analysis.post;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.JsonRenderer;
import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorEvent1D;
import com.vaavud.sensor.SensorEvent3D;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MagneticSession;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;

public class MeasurementHelper {
    
    private MeasurementSession measurementSession;
    private Device device;
    private MagneticSession magneticSession;
    private double endTime;
    private List<SensorEvent3D> magEvents = null;
    private List<SensorEvent> events = null;
    
    public MeasurementHelper(HttpServletRequest request) {
        
        // Start hibernate Session
        Session hibernateSession = Model.get().getSessionFactory().openSession();

        if (request.getParameter("session_id") == null) {
            measurementSession = (MeasurementSession) hibernateSession
                    .createQuery(
                            "from MeasurementSession order by id DESC LIMIT 1")
                    .uniqueResult();
        } else {
            measurementSession = (MeasurementSession) hibernateSession.get(
                    MeasurementSession.class,
                    Long.parseLong(request.getParameter("session_id")));
        }
        
        
        /* Get Device */
        device = measurementSession.getDevice();
        
        /* Get magnetic Session */
        Query query = hibernateSession
                .createQuery("from MagneticSession where measurementSessionUuid = :measurementSessionUuid");
        query = query.setParameter("measurementSessionUuid",
                measurementSession.getUuid());
        magneticSession = (MagneticSession) query.uniqueResult();

        
        
        List<SensorEvent1D> windEvents = generateWindEvents(measurementSession.getPoints()); 
        events = new ArrayList<>();
        events.addAll(windEvents);

        
        if (magneticSession != null) {
            MeasurementAnalyzer analyzer = new MeasurementAnalyzer(magneticSession);
            events.addAll(analyzer.getEvents());
            
        }
            
        //End timestep shown
        endTime =  windEvents.get(windEvents.size()-1).getTime();
        if (magEvents != null) {
            if (magEvents.get(magEvents.size() - 1).getTime() > endTime) {
                endTime = magEvents.get(magEvents.size() - 1).getTime();
            }
        }
        
        
        
        if (hibernateSession.getTransaction() != null
                && hibernateSession.getTransaction().isActive()) {
            hibernateSession.getTransaction().rollback();
        }
        hibernateSession.close();
        
    }
    
    public List<SensorEvent1D> generateWindEvents(List<MeasurementPoint> mesPoints) {
        // create wind time array // MP.time-MS.startTime)/1000
        Sensor sensor = new Sensor(Type.WINDSPEED, "Original");
        List<SensorEvent1D> windEvents = new ArrayList<>(mesPoints.size());
        
        for (MeasurementPoint mesPoint : mesPoints) {
            long timeUs = (long) (mesPoint.getTime().getTime() - measurementSession.getStartTime().getTime()) * 1_000;
            windEvents.add(new SensorEvent1D(sensor, timeUs, mesPoint.getWindSpeed()));
        }
        
        return windEvents;
    }
    
    public String getDataTable() {
     // public static CharSequence renderDataTable(DataTable dataTable, boolean includeValues, boolean includeFormatting, boolean renderDateAsDateConstructor)
        
        return JsonRenderer.renderDataTable(generateDataTable(), true, true, false).toString();
    }
    
    public DataTable generateDataTable() {
        // Create a data table,
        DataTable data = new DataTable();
        ArrayList<ColumnDescription> cds = new ArrayList<ColumnDescription>();
        cds.add(new ColumnDescriptionEvent("time", null, EventField.TIME, null));
        cds.add(new ColumnDescriptionEvent("windspeed", Type.WINDSPEED, EventField.VALUE, null));
        cds.add(new ColumnDescriptionEvent("magx", Type.MAGNETIC_FIELD, EventField.X, null));
        cds.add(new ColumnDescriptionEvent("magy", Type.MAGNETIC_FIELD, EventField.Y, null));
        cds.add(new ColumnDescriptionEvent("magz", Type.MAGNETIC_FIELD, EventField.Z, null));
        cds.add(new ColumnDescriptionEvent("freq", Type.FREQUENCY, EventField.FREQ, null));
        cds.add(new ColumnDescriptionEvent("ref_Freq", Type.FREQUENCY, EventField.FREQ, "Freq_Reference"));
        cds.add(new ColumnDescriptionEvent("ref_amp", Type.FREQUENCY, EventField.AMP, "Freq_Reference"));
        cds.add(new ColumnDescriptionEvent("ref_sf", Type.FREQUENCY, EventField.SF, "Freq_Reference"));
        cds.add(new ColumnDescriptionEvent("f1_Freq", Type.FREQUENCY, EventField.FREQ, "Freq_1"));
        cds.add(new ColumnDescriptionEvent("f1_amp", Type.FREQUENCY, EventField.AMP, "Freq_1"));
        cds.add(new ColumnDescriptionEvent("f1_sf", Type.FREQUENCY, EventField.SF, "Freq_1"));
        
        data.addColumns(cds);

        
        List<SensorEvent> sortedEvents = sortList(events);
        
        // Fill the data table.
        try {
            for (SensorEvent event : sortedEvents) {
                Object[] row = new Object[cds.size()];
                
                int i = 0;
                for (ColumnDescription cd : cds) {
                    ColumnDescriptionEvent colDes = (ColumnDescriptionEvent) cd;
                    if (colDes.getSensorType() == null) {
                        row[i] = colDes.getEventField().get(event);
                    }
                    else if (colDes.getSensorType() == event.getSensor().getType()) {
                        if (colDes.getSensorName() == null || colDes.getSensorName() == event.getSensor().getName()) {
                            row[i] = colDes.getEventField().get(event);
                        }
                    }
                    i++;
                }
                
                data.addRowFromValues(row);
            }
          
        } catch (TypeMismatchException e) {
          System.out.println("Invalid type!");
        }
        return data;
      }
    
    
    
    private List<SensorEvent> sortList(List<SensorEvent> events) {
        
        EnumMap<Sensor.Type, Map<String, List<SensorEvent>>> eventMap = new EnumMap<>(Sensor.Type.class);
        for (Sensor.Type sensorType : Sensor.Type.values()) {
            eventMap.put(sensorType, new HashMap<String, List<SensorEvent>>());
        }
        
        for (SensorEvent event: events) {
            Type eType = event.getSensor().getType();
            String eName = event.getSensor().getName();
            if (! eventMap.get(eType).containsKey(eName)) {
                eventMap.get(eType).put(eName, new ArrayList<SensorEvent>());
            }
            eventMap.get(eType).get(eName).add(event);
        }
        
        List<SensorEvent> sortedEvents = new ArrayList<SensorEvent>(events.size());
        
        for (Sensor.Type sensorType : Sensor.Type.values()) {
            for (Map.Entry<String, List<SensorEvent>> entry : eventMap.get(sensorType).entrySet())
            sortedEvents.addAll(entry.getValue());
        }
        
        return sortedEvents;
    }
    
    public String getTable(String title, Object object) {
        
        StringBuilder sb = new StringBuilder(4000);
        
        
        sb.append("<h2>").append(title).append("</h2>");
        sb.append("<table>");
        
        if (object != null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = null;
                try {
                    value = field.get(object);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (name == "device") {
                    addRow(sb, "Device", "...");
                }
                else if (name == "points") {
                    addRow(sb, "points", "...");
                }
                else if (name == "magneticPoints") {
                    addRow(sb, "magneticPoints", "...");
                }
                else if (value == null) {
                    System.out.println(name);
                }
                else {
                    addRow(sb, name, value.toString());
                }
            }
        }
        sb.append("</table>");
        
        return sb.toString();
        
    }
    
    public void addRow(StringBuilder sb, String field, String value) {
        sb.append("<tr>"); 
        sb.append("<td>").append(field).append("</td>");
        sb.append("<td>").append(value).append("</td>");
        sb.append("</tr>");
    }

    
    // GETTERS
    public MeasurementSession getMeasurementSession() {
        return measurementSession;
    }

    public Device getDevice() {
        return device;
    }

    public MagneticSession getMagneticSession() {
        return magneticSession;
    }
    
    public Double getLatitude() {
        if (measurementSession.getPosition() == null) {
            return 0d;
        }
        else {
            return measurementSession.getPosition().getLatitude();
        }
        
    }
    
    public Double getLongitude() {
        if (measurementSession.getPosition() == null) {
            return 0d;
        }
        else {
            return measurementSession.getPosition().getLongitude();
        }
        
    }
    
    public Double getEndTime() {
        return endTime;
    }
}
