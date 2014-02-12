package com.vaavud.server.web.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.hibernate.Session;

import com.vaavud.sensor.Sensor;
import com.vaavud.sensor.SensorEvent;
import com.vaavud.sensor.Sensor.Type;
import com.vaavud.server.analysis.post.MeasurementAnalyzer;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MagneticSession;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.web.analysis.chart.Chart;
import com.vaavud.server.web.analysis.chart.ChartConfig;
import com.vaavud.server.web.analysis.chart.DataSet;
import com.vaavud.server.web.analysis.chart.EventField;
import com.vaavud.server.web.analysis.util.Event;

/**
 * Servlet implementation class MeasurementServlet
 */
@WebServlet("/MeasurementServlet")
public class MeasurementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		

	    String pass = "2gh7yJfJ6H";

	    if (!pass.equals(request.getParameter("pass"))) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        return;
	    }
	    
//	    if (request.getParameter("session_id") == null) {
//	        request.setAttribute("message", "Please provide a session_id");
//	        request.getRequestDispatcher("/analysis/error.jsp").forward(request, response);
//	        return;
//	    }
	    
	    MeasurementView measurement;
	    
	    if ( "true".equals(request.getParameter("test")) ) {
	        measurement = new MeasurementViewTest(charts());
	    }
	    else {
	        MeasurementViewSession viewSession = new MeasurementViewSession(request.getParameter("session_id"), charts());
	        
	        request.setAttribute("latitude", viewSession.getLatitude());
	        request.setAttribute("longitude",  viewSession.getLongitude());
	        request.setAttribute("deviceTable", viewSession.getDeviceTableHTML());
	        request.setAttribute("measurementSessionTable", viewSession.getMeasurementSessionTableHTML());
	        request.setAttribute("magneticSessionTable", viewSession.getMagneticSessionHTML());
	        
	        measurement = viewSession;
	    }
	    
	    request.setAttribute("endTime", "30");
	    request.setAttribute("dataTable", measurement.getChart().dataTableJSON());
        request.setAttribute("charts", measurement.getChart().chartsWrapped());
        
        request.getRequestDispatcher("/analysis/measurement.jsp").forward(request, response);
        
        

	}
	
	private final static List<ChartConfig> charts() {
        List<ChartConfig> chartConfigs = new ArrayList<>();
        
        ChartConfig freqChart = new ChartConfig(
                "Frequency",
                new DataSet(null, EventField.TIME, false), 
                new DataSet(Type.FREQUENCY, EventField.FREQ, true),
                new DataSet(Type.WINDSPEED, EventField.VALUE, true));
        freqChart.setvAxisTitle("Rotation Freq (Hz)/(m/s)");
        chartConfigs.add(freqChart);
        
        ChartConfig magChart = new ChartConfig(
                "MagneticField",
                new DataSet(null, EventField.TIME, false),
                new DataSet(Type.MAGNETIC_FIELD, EventField.X, true),
                new DataSet(Type.MAGNETIC_FIELD, EventField.Y, true),
                new DataSet(Type.MAGNETIC_FIELD, EventField.Z, true)); 
        magChart.setPointSize(0);
        magChart.setvAxisTitle("Magnetic Field (mu-Tesla)");
        chartConfigs.add(magChart);
        
        ChartConfig sfChart = new ChartConfig(
                "SampleFrequency",
                new DataSet(null,  EventField.TIME, false), 
                new DataSet(Type.FREQUENCY, EventField.SF, true));
        sfChart.setvAxisTitle("Sample Frequency (Hz)");
        chartConfigs.add(sfChart);
        
        ChartConfig freqAmp = new ChartConfig(
                "FrequencyAmplitude",
                new DataSet(Type.FREQUENCY, EventField.FREQ, false), 
                new DataSet(Type.FREQUENCY, EventField.AMP, true));
        freqAmp.setLineWidth(0);
        freqAmp.setvAxisTitle("Amplitude (mu-Tesla)");
        chartConfigs.add(freqAmp);
        
        return chartConfigs;
    }
	
	


}
