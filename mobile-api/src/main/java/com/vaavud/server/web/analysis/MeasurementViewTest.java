package com.vaavud.server.web.analysis;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.test.TestSensor;
import com.vaavud.sensor.test.TestSensorConfig;
import com.vaavud.server.analysis.post.MeasurementAnalyzer;
import com.vaavud.server.web.analysis.chart.ChartConfig;

public class MeasurementViewTest extends MeasurementView {

    public MeasurementViewTest(List<ChartConfig> chartConfigs, HttpServletRequest request) {
        super(chartConfigs);
        
        
        // http://localhost:8080/analysis/measurement?pass=2gh7yJfJ6H&test=true&type=plain&freq=7&sf=100
        TestSensorConfig testSensorConfig = new TestSensorConfig(
                request.getParameter("freq"),
                request.getParameter("sf"), 
                request.getParameter("noise"), 
                request.getParameter("type"));

        
        MeasurementAnalyzer analyzer = new MeasurementAnalyzer(Type.FREQUENCY, Type.MAGNETIC_FIELD);
        analyzer.addSensor(new TestSensor(testSensorConfig));
        List<SensorEvent> events = analyzer.getEvents();
        generateCharts(events);
    }

}
