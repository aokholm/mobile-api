package com.vaavud.server.web.analysis.chart;

import com.vaavud.sensor.Sensor;

public class DataSet {
    
    private final Sensor.Type sensorType;
    private final EventField eventField;
    private final String sensorName;
    private final Boolean seperateNames;
    
    public DataSet(Sensor.Type sensorType, EventField eventField, Object sensorName) {
        this.sensorType = sensorType;
        this.eventField = eventField;
        
        
        if (sensorName == null) {
            sensorName = false;
        }
        
        if (sensorName instanceof Boolean) {
            this.seperateNames = (Boolean) sensorName;
        }
        else {
            this.seperateNames = true;
        }
        
        if (sensorName instanceof String) {
            this.sensorName = (String) sensorName;
        }
        else {
            this.sensorName = null;
        }
    }

    public EventField getEventField() {
        return eventField;
    }
    
    public Boolean getSeperateNames() {
        return seperateNames;
    }
    
    public String getSensorName() {
        return sensorName;
    }
    
    public Sensor.Type getSensorType() {
        return sensorType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventField == null) ? 0 : eventField.hashCode());
        result = prime * result + ((sensorName == null) ? 0 : sensorName.hashCode());
        result = prime * result + ((sensorType == null) ? 0 : sensorType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataSet other = (DataSet) obj;
        if (eventField != other.eventField)
            return false;
        if (sensorName == null) {
            if (other.sensorName != null)
                return false;
        } else if (!sensorName.equals(other.sensorName))
            return false;
        if (sensorType != other.sensorType)
            return false;
        return true;
    }
    
    
}
