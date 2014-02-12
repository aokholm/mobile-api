package com.vaavud.server.web.analysis;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.server.analysis.post.DatabaseSensor;
import com.vaavud.server.analysis.post.MeasurementAnalyzer;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MagneticSession;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.web.analysis.chart.ChartConfig;
import com.vaavud.server.web.analysis.util.Event;
import com.vaavud.server.web.analysis.util.render.Table;

public class MeasurementViewSession extends MeasurementView {

    private Session hibernateSession;
    private MeasurementSession measurementSession;
    private Device device;
    private MagneticSession magneticSession;
    
    public MeasurementViewSession(String session_id, List<ChartConfig> chartConfigs) {
        super(chartConfigs);
        
        measurementSession = getMeasurementSession(session_id);
        device = measurementSession.getDevice();
        
        // magneticSession can be null
        magneticSession = getMagneticSession(measurementSession.getUuid());
        List<SensorEvent> events = new ArrayList<>();
        
        List<SensorEvent> windEvents = Event.windEvents(measurementSession.getPoints()
                , measurementSession.getStartTime().getTime());
        events.addAll(windEvents);
        
        if (magneticSession != null) {
            MeasurementAnalyzer analyzer = new MeasurementAnalyzer(Type.FREQUENCY, Type.MAGNETIC_FIELD);
            analyzer.addSensor(new DatabaseSensor(magneticSession));
            events.addAll(analyzer.getEvents());
        }
        
        generateCharts(events);
        
        closeSession();
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
            return (MeasurementSession) getSession().createQuery(
                    "from MeasurementSession order by id DESC").setMaxResults(1).uniqueResult();
        } else {
            return (MeasurementSession) getSession().get(MeasurementSession.class, session_id);
        }
    }
    
    private MagneticSession getMagneticSession(String sessionUuid) {
        
        Query query = getSession()
                .createQuery("from MagneticSession where measurementSessionUuid = :measurementSessionUuid");
        query = query.setParameter("measurementSessionUuid", sessionUuid);
        return (MagneticSession) query.uniqueResult();
    }
    
    private Session getSession() {
        if (hibernateSession == null) {
            hibernateSession = Model.get().getSessionFactory().openSession();
        }
        return hibernateSession;
    }
    
    private void closeSession() {
        // Close transaction - not sure if it's the right place
        if (getSession().getTransaction() != null && getSession().getTransaction().isActive()) {
            getSession().getTransaction().rollback();
        }
        getSession().close();
    }

}
