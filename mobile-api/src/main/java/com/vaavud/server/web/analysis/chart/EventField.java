package com.vaavud.server.web.analysis.chart;

import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.SensorEvent1D;
import com.vaavud.sensor.SensorEvent3D;
import com.vaavud.sensor.SensorEventFreq;

public enum EventField {
    TIME {
        public Double get(Object event) {
            SensorEvent e = (SensorEvent) event;
            return e.getTime();
        }
    },    
    VALUE {
        public Double get(Object event) {
            SensorEvent1D e = (SensorEvent1D) event;
            return e.getValue();
        }
    },
    X {
        public Double get(Object event) {
            SensorEvent3D e = (SensorEvent3D) event;
            return e.getX();
        }
    },
    Y {
        public Double get(Object event) {
            SensorEvent3D e = (SensorEvent3D) event;
            return e.getY();
        }
    },
    Z {
        public Double get(Object event) {
            SensorEvent3D e = (SensorEvent3D) event;
            return e.getZ();
        }
    },
    FREQ {
        public Double get(Object event) {
            SensorEventFreq e = (SensorEventFreq) event;
            return e.getFreq();
        }
    },
    AMP {
        public Double get(Object event) {
            SensorEventFreq e = (SensorEventFreq) event;
            return e.getAmp();
        }
    },
    SF {
        public Double get(Object event) {
            SensorEventFreq e = (SensorEventFreq) event;
            return e.getSf();
        }
    },
    SN {
        public Double get(Object event) {
            SensorEventFreq e = (SensorEventFreq) event;
            return e.getSN();
        }
    };

    public abstract Double get(Object event);
}
