<%@page import="com.vaavud.server.analysis.model.CoreMeasurementPoint"%>
<%@page import="com.vaavud.server.analysis.magnetic.FFTManager"%>
<%@page import="com.vaavud.server.analysis.model.CoreMagneticPoint"%>
<%@page import="com.vaavud.server.analysis.magnetic.DataManager"%>
<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.lang.reflect.Field,java.util.*,org.hibernate.*,org.hibernate.type.StandardBasicTypes,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.vaavud.server.api.util.*,com.fasterxml.jackson.databind.*"%><%
	
    // Check password     
    String pass = "2gh7yJfJ6H";     
         
    if (!pass.equals(request.getParameter("pass"))) {
        ServiceUtil.sendUnauthorizedErrorResponse(response);
        return;
    }
    
    // Start hibernate Session
    Session hibernateSession = Model.get().getSessionFactory().openSession();
    
    MeasurementSession measurementSession;
	Device device;
	MagneticSession magneticSession;
    
    if (request.getParameter("session_id") == null) {
    	measurementSession = (MeasurementSession) hibernateSession.createQuery("from MeasurementSession order by id DESC LIMIT 1").uniqueResult();
//     	setMaxResults(1)
    } else {
    	measurementSession = (MeasurementSession) hibernateSession.get(MeasurementSession.class, Long.parseLong(request.getParameter("session_id")));
    }
    
    device = measurementSession.getDevice();
    Query query = hibernateSession.createQuery("from MagneticSession where measurementSessionUuid = :measurementSessionUuid");
   	query = query.setParameter("measurementSessionUuid", measurementSession.getUuid());
    magneticSession = (MagneticSession) query.uniqueResult();
        
	// create wind time array // MP.time-MS.startTime)/1000
	List<MeasurementPoint> mesPoints = measurementSession.getPoints();
	double[] mpTime = new double[mesPoints.size()];
	
	for (int i=0; i<mesPoints.size(); i++) {
		mpTime[i] = (mesPoints.get(i).getTime().getTime() - measurementSession.getStartTime().getTime())/1000d;
	}
	
	// magnetic points 
	List<MagneticPoint> magPoints = magneticSession.getMagneticPoints();
	
	// End timestep shown
	double endTime;
	if (magPoints.get(magPoints.size() -1).getTime() > mpTime[mpTime.length-1] ) {
		endTime = magPoints.get(magPoints.size() -1).getTime();
	} else {
		endTime = mpTime[mpTime.length-1];
	}
	
	
	
	//********* GENERATE MAGANALYSIS *********//
	
	DataManager dataManager = new DataManager();
	
	List<CoreMagneticPoint> coreMagneticPoints = new ArrayList<CoreMagneticPoint>(magPoints.size());
	
	// generate list of coreMagneticPoints
	for (int i = 0; i < magPoints.size(); i++) {
		coreMagneticPoints.add( new CoreMagneticPoint( magPoints.get(i).getTime(), magPoints.get(i).getX(), magPoints.get(i).getY(), magPoints.get(i).getZ() ));
	}
	dataManager.addMagneticFieldReadings(coreMagneticPoints);
	
	FFTManager fftManager = new FFTManager(dataManager);
	
	List<CoreMeasurementPoint> coreMeasurementPoints = fftManager.getMeasurementPoints();
	

//************* START OF WEBPAGE ************/
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  <title>Vaavud</title>
  <style type="text/css">
      html {height:100%}
      body {height:100%; margin:0; padding:0}
      table {min-width:400px;}
      td {border:1px solid #000000;}
      
      .left {text-align:left;}
      .right {text-align:right;}
      #map_canvas {
        width: 500px;
        height: 400px;
      }
      
      
  </style>
  <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
  <script>
  function initialize() {
    var mapCanvas = document.getElementById('map_canvas');
    
    var measurementLatlng = new google.maps.LatLng(<%=measurementSession.getPosition().getLatitude()%>, <%=measurementSession.getPosition().getLongitude()%>);
    
    var mapOptions = {
      center: measurementLatlng,
      zoom: 11,
      mapTypeId: google.maps.MapTypeId.SATELLITE
    };
    var map = new google.maps.Map(mapCanvas, mapOptions);
    
 	// To add the marker to the map, use the 'map' property
    var marker = new google.maps.Marker({
        position: measurementLatlng,
        map: map,
        title:"!"
    });
  }
  google.maps.event.addDomListener(window, 'load', initialize);
</script>
</head>
<body>
	<div id="chart_windspeed"></div>
	<div id="chart_frequency"></div>
	<div id="chart_magneticField"></div>
	<div id="map_canvas"></div>
	
	<br />
	<h2>Device</h2>
	<table>	
			<%
			for (Field field : device.getClass().getDeclaredFields()) {
			    field.setAccessible(true);
			    String name = field.getName();
			    Object value = field.get(device);
			    %><tr><td><%=name%></td><td><%=value%></td></tr><%  
			}
			%>
	</table>
	<h2>Measurement Session</h2>
	<table>	
			<%
			if (measurementSession != null) {
				for (Field field : measurementSession.getClass().getDeclaredFields()) {
				    field.setAccessible(true);
				    String name = field.getName();
				    Object value = field.get(measurementSession);
				    if (name == "device") {
				    	%><tr><td>device</td><td>...</td></tr><%
				    }
				    else if (name == "points") {
				    	%><tr><td>points</td><td>...</td></tr><%
				    }
				    else {
				    	%><tr><td><%=name%></td><td><%=value%></td></tr><%
				    }   
				}
			}
			%>
	</table>
	<h2>Magnetic Session</h2>
	<table>	
			<%
			if (magneticSession != null) {
				for (Field field : magneticSession.getClass().getDeclaredFields()) {
				    field.setAccessible(true);
				    String name = field.getName();
				    Object value = field.get(magneticSession);
				    if (name == "magneticPoints") {
				    	%><tr><td>magneticPoints</td><td>...</td></tr><%
				    }
				    else {
				    	%><tr><td><%=name%></td><td><%=value%></td></tr><%
				    }  
				}
			}
			%>
	</table>
	

<script type="text/javascript">
    // Load the Visualization API and the piechart package.
    google.load('visualization', '1', {
        'packages' : [ 'corechart' ]
    });
 
    // Set a callback to run when the Google Visualization API is loaded.
    google.setOnLoadCallback(drawCharts);
 	
    function drawCharts() {
    	drawWindspeedChart();
    	drawFrequencyChart();
    	drawMagneticFieldChart();
    }
    
     	
 	function drawWindspeedChart() {
    	

    	   	
    	var options = 
    	{
    		title : "Wind speed",
    		series : [{"lineWidth": 1, "pointSize": 2}],
    		vAxis: {
    			title: "windspeed (m/s)"
    		},
	    	hAxis: { 
	            title: "Time (s)", 
	            viewWindowMode:'explicit',
	            viewWindow:{
	              min:0,
	              max:<%=endTime%>
	            }
	          }
    	};
    	
    	var data = google.visualization.arrayToDataTable([
          ['time', 'windspeed'],
        <% // [2,  1000],
        for (int i = 0; i < mesPoints.size() ; i++) {
        	%>[<%=mpTime[i]%>, <%=mesPoints.get(i).getWindSpeed()%>],<%
        }
        %>  
        ]);
    	
    	var chart = new google.visualization.LineChart(document.getElementById('chart_windspeed'));
        chart.draw(data, options);
    	
//		JSON CHART - SAVE FOR LATER
//    	var parameters = {
//    		"pass" : "2gh7yJfJ6H", 
//    		"session_id" : "X"
//    	};
//     	$.getJSON("/analysis/json/windspeed.jsp", parameters, function(json) {
//     	    var data = new google.visualization.DataTable(json);
//             // Instantiate and draw our chart, passing in some options.
//             var chart = new google.visualization.LineChart(document
//                     .getElementById('chart_div'));
//             chart.draw(data, options);
//     	});
    	
    }
 	
 	function drawFrequencyChart() {
    	
    	var options = 
    	{
    		title : "Frequency",
    		series : [{"lineWidth": 1, "pointSize": 0}],
    		vAxis: {
    			title: "Frequency (Hz)"
    		},
	    	hAxis: { 
	            title: "Time (s)", 
	            viewWindowMode:'explicit',
	            viewWindow:{
	              min:0,
	              max:<%=endTime%>
	            }
	          }
    	};
    	
    	var data = google.visualization.arrayToDataTable([
          ['time', 'windspeed'],
        <% // [2,  1000],
        for (int i = 0; i < coreMeasurementPoints.size() ; i++) {
        	%>[<%=coreMeasurementPoints.get(i).getTime()%>, <%=coreMeasurementPoints.get(i).getFrequency()%>],<%
        }
        %>  
        ]);
    	
    	var chart = new google.visualization.LineChart(document.getElementById('chart_frequency'));
        chart.draw(data, options);    	
    }
 	
	function drawMagneticFieldChart() {
    	
    	var options = 
    	{
    		title : "Magnetic Field",
    		series : [{"lineWidth": 1, "pointSize": 0}, 
    		            {"lineWidth": 1, "pointSize": 0}, 
    		            {"lineWidth": 1, "pointSize": 0}],
    		vAxis: {
    			title: "magneticField (mu-Tesla)"
    		},
            hAxis: { 
	            title: "Time (s)", 
	            viewWindowMode:'explicit',
	            viewWindow:{
	              min:0,
	              max:<%=endTime%>
	            }
	          }
    	};
    	
    	var data = google.visualization.arrayToDataTable([
          ['time', 'x', 'y', 'z'],
        <% // [2,  1000],
        for (int i = 0; i < magPoints.size() ; i++) {
        	%>[<%=magPoints.get(i).getTime()%>, <%=magPoints.get(i).getX()%>, <%=magPoints.get(i).getY()%>, <%=magPoints.get(i).getZ()%>, ],<%
        }
        %>  
        ]);
    	
    	var chart = new google.visualization.LineChart(document.getElementById('chart_magneticField'));
        chart.draw(data, options);
    	
    }
 	
 	
</script>

</body>
</html>
<%
if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
    hibernateSession.getTransaction().rollback();
}
hibernateSession.close();
%>