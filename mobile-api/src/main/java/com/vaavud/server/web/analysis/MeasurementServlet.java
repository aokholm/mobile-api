package com.vaavud.server.web.analysis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.vaavud.sensor.Sensor.Type;
import com.vaavud.server.model.Model;
import com.vaavud.server.web.analysis.chart.ChartConfig;
import com.vaavud.server.web.analysis.chart.DataSet;
import com.vaavud.server.web.analysis.chart.EventField;

/**
 * Servlet implementation class MeasurementServlet
 */
@WebServlet("/MeasurementServlet")
public class MeasurementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			    
	    String pass = "2gh7yJfJ6H";
	    Session hibernateSession = Model.get().getSessionFactory().openSession();
	    
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
        MeasurementViewSession viewSession = new MeasurementViewSession(charts(),request, hibernateSession);
        
        request.setAttribute("latitude", viewSession.getLatitude());
        request.setAttribute("longitude",  viewSession.getLongitude());
        request.setAttribute("deviceTable", viewSession.getDeviceTableHTML());
        request.setAttribute("measurementSessionTable", viewSession.getMeasurementSessionTableHTML());
        request.setAttribute("magneticSessionTable", viewSession.getMagneticSessionHTML());
        request.setAttribute("startTime", viewSession.getMeasurementTime());
        request.setAttribute("measurementSession", viewSession.getMeasurementSession());
        
        measurement = viewSession;
	    
	    request.setAttribute("sensors", measurement.getSensors());
//	    request.setAttribute("endTime", "30");
	    request.setAttribute("dataTable", measurement.getChart().dataTableJSON());
        request.setAttribute("charts", measurement.getChart().chartsWrapped());
        request.setAttribute("controlChartColumn", measurement.getControlChartColumns());
        
        request.getRequestDispatcher("/analysis/measurement.jsp").forward(request, response);
        
        closeSession(hibernateSession);
	}
	
	
	private final static List<ChartConfig> charts() {
        List<ChartConfig> chartConfigs = new ArrayList<>();
        
        
        // first chart contains the control chart columns !!!
        ChartConfig freqChart = new ChartConfig(
                "Frequency",
                new DataSet(null, EventField.TIME, false), 
                new DataSet(Type.FREQUENCY, EventField.FREQ, true),
                new DataSet(Type.WINDSPEED, EventField.VALUE, true));
        freqChart.setvAxisTitle("Rotation Freq (Hz)/(m/s)");
        chartConfigs.add(freqChart);
        
//        ChartConfig snChart = new ChartConfig(
//                "signalNoise",
//                new DataSet(null, EventField.TIME, false),
//                new DataSet(Type.FREQUENCY, EventField.SN, true));
//        snChart.setvAxisTitle("S/N ()");
//        chartConfigs.add(snChart);
        
//        ChartConfig ampChart = new ChartConfig(
//                "ampChart",
//                new DataSet(null, EventField.TIME, false),
//                new DataSet(Type.FREQUENCY, EventField.AMP, true));
//        ampChart.setvAxisTitle("Amplitude (mu-Tesla)");
//        chartConfigs.add(ampChart);
        
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
                new DataSet(Type.FREQUENCY, EventField.SF, true),
                new DataSet(Type.SAMPLE_FREQUENCY, EventField.VALUE, false));
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

    
    private static void closeSession(Session hibernateSession) {
        if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
            hibernateSession.getTransaction().rollback();
        }
        hibernateSession.close();
    }


}
