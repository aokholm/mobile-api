package com.vaavud.server.web.analysis;

import java.util.List;

import com.vaavud.sensor.Sensor.Type;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.test.TestSensor;
import com.vaavud.server.analysis.post.MeasurementAnalyzer;
import com.vaavud.server.web.analysis.chart.ChartConfig;

public class MeasurementViewTest extends MeasurementView {

    public MeasurementViewTest(List<ChartConfig> chartConfigs) {
        super(chartConfigs);

        MeasurementAnalyzer analyzer = new MeasurementAnalyzer(Type.FREQUENCY, Type.MAGNETIC_FIELD);
        analyzer.addSensor(new TestSensor());
        List<SensorEvent> events = analyzer.getEvents();
        generateCharts(events);
    }

}
