package com.vaavud.server.web.analysis;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vaavud.sensor.MeasurementAnalyzer;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.frequency.FrequencySensor;
import com.vaavud.sensor.ref.revolution.RevolutionSensorRef;
import com.vaavud.sensor.revolution.RevSensorConfig;
import com.vaavud.sensor.revolution.RevolutionSensor;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MagneticSession;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.web.analysis.chart.ChartConfig;
import com.vaavud.server.web.analysis.post.DatabaseSensor;
import com.vaavud.server.web.analysis.util.Event;
import com.vaavud.server.web.analysis.util.render.Table;

public class MeasurementViewSession extends MeasurementView {

    private Session hibernateSession;
    private MeasurementSession measurementSession;
    private Device device;
    private MagneticSession magneticSession;
    
    public MeasurementViewSession(List<ChartConfig> list, HttpServletRequest request, Session session) {
        super(list);
        
        hibernateSession = session;
        
        measurementSession = getMeasurementSession(request.getParameter("session_id"));
        device = measurementSession.getDevice();
        
        // magneticSession can be null
        magneticSession = getMagneticSession(measurementSession.getUuid());
        List<SensorEvent> events = new ArrayList<>();
        
        List<SensorEvent> windEvents = Event.windEvents(measurementSession.getPoints()
                , measurementSession.getStartTime().getTime());
        events.addAll(windEvents);
        
        if (magneticSession != null) {
            RevSensorConfig config = new RevSensorConfig();
            
            
            if (request.getParameter("movAvg") != null) {
                config.setMovAvg(Integer.valueOf(request.getParameter("movAvg")));
            }
            
            MeasurementAnalyzer analyzer = new MeasurementAnalyzer(config, Type.FREQUENCY, Type.MAGNETIC_FIELD, Type.SAMPLE_FREQUENCY);
            
            if (request.getParameter("new") != null) {
                if (Boolean.parseBoolean(request.getParameter("new"))) {
                    analyzer.addSensor(new RevolutionSensor(config));
                }
            }
            
            if (request.getParameter("windTunnel") != null) {
                if (Boolean.parseBoolean(request.getParameter("windTunnel"))) {
                    analyzer.addSensor(new RevolutionSensor(config));
                }
            }
            
            if (request.getParameter("ref") != null) {
                if (Boolean.parseBoolean(request.getParameter("ref"))) {
                    analyzer.addSensor(new RevolutionSensorRef(config));
                }
            }
            
            if (request.getParameter("freq") != null) {
                if (Boolean.parseBoolean(request.getParameter("freq"))) {
                    analyzer.addSensor(new FrequencySensor());
                }
            }
            
            analyzer.addSensor(new RevolutionSensor(config));
            analyzer.addSensor(new DatabaseSensor(magneticSession)); // database sensor should be added last
            events.addAll(analyzer.getEvents());
        }
        
        generateCharts(events);
    }
    
    
    public Double getLatitude() {
        if (measurementSession.getPosition() == null) {
            return 0d;
        } else {
            return measurementSession.getPosition().getLatitude();
        }

    }

    public Double getLongitude() {
        if (measurementSession.getPosition() == null) {
            return 0d;
        } else {
            return measurementSession.getPosition().getLongitude();
        }

    }
    
    public String getDeviceTableHTML() {
        return Table.getTable("Device", device);   
    }
    
    public String getMeasurementSessionTableHTML() {
        return Table.getTable("MeasurementSession", measurementSession);   
    }
    
    public String getMagneticSessionHTML() {
        return Table.getTable("MagneticSession", magneticSession);   
    }
    
    private MeasurementSession getMeasurementSession(String session_idString) {
        Long session_id = null;
        
        if (session_idString != null)  {
            session_id = Long.parseLong(session_idString);
        }
            
        if (session_id == null) {
            return (MeasurementSession) hibernateSession.createQuery(
                    "from MeasurementSession order by id DESC").setMaxResults(1).uniqueResult();
        } else {
            return (MeasurementSession) hibernateSession.get(MeasurementSession.class, session_id);
        }
    }
    
    private MagneticSession getMagneticSession(String sessionUuid) {
        
        Query query = hibernateSession
                .createQuery("from MagneticSession where measurementSessionUuid = :measurementSessionUuid");
        query = query.setParameter("measurementSessionUuid", sessionUuid);
        return (MagneticSession) query.uniqueResult();
    }
    
    public Long getMeasurementTime() {
        return measurementSession.getStartTime().getTime();
    }
    
    public MeasurementSession getMeasurementSession() {
        return measurementSession;
    }

}
