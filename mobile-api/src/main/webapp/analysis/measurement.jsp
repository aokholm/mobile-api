<%@page import="com.vaavud.sensor.Sensor.Type"%>
<%@page import="com.vaavud.sensor.SensorEvent"%>
<%@page import="com.vaavud.server.analysis.post.MeasurementAnalyzer"%>
<%@ page language="java" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.lang.reflect.Field,java.util.*,org.hibernate.*,org.hibernate.type.StandardBasicTypes,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.vaavud.server.api.util.*,com.fasterxml.jackson.databind.*"%>
<%
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
	measurementSession = (MeasurementSession) hibernateSession
			.createQuery("from MeasurementSession order by id DESC LIMIT 1").uniqueResult();
} else {
	measurementSession = (MeasurementSession) hibernateSession
			.get(MeasurementSession.class, Long.parseLong(request.getParameter("session_id")));
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

List<SensorEvent> events = null;
List<SensorEvent> magEvents = null;
List<SensorEvent> freqEvents = null;
List<SensorEvent> freqEventsRef = null;

if (magneticSession != null){
	
  MeasurementAnalyzer analyzer = new MeasurementAnalyzer(magneticSession);
  
  events = analyzer.getEvents();
  
  magEvents = new ArrayList<SensorEvent>();
  freqEvents = new ArrayList<SensorEvent>();
  freqEventsRef = new ArrayList<SensorEvent>();
  
  for (SensorEvent event : events) {
      if (event.sensor.getType() == Type.MAGNETIC_FIELD) {
          magEvents.add(event);
      }
          
      if (event.sensor.getName() == "Freq_1") {
          freqEvents.add(event);
      }
      
      if (event.sensor.getName() == "Freq_Reference") {
          freqEventsRef.add(event);
      }
  }
}



//End timestep shown
double endTime = mpTime[mpTime.length-1];
if (magEvents != null) {
	if (magEvents.get(magEvents.size() -1).getTime() > endTime ) {
		endTime = magEvents.get(magEvents.size() -1).getTime();
	}
}



//************* START OF WEBPAGE ************/
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
<title>Vaavud</title>
<style type="text/css">
html {
	height: 100%
}

body {
	height: 100%;
	margin: 0;
	padding: 0
}

table {
	min-width: 400px;
}

td {
	border: 1px solid #000000;
}

.left {
	text-align: left;
}

.right {
	text-align: right;
}

#map_canvas {
	width: 500px;
	height: 400px;
}
</style>
<script type="text/javascript"
	src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
<script>
  function initialize() {
    var mapCanvas = document.getElementById('map_canvas');
    
    <%if (measurementSession.getPosition() != null) {%>var measurementLatlng = new google.maps.LatLng(
          <%=measurementSession.getPosition().getLatitude()%>, <%=measurementSession.getPosition().getLongitude()%>);<%} else {%>var measurementLatlng = new google.maps.LatLng(0, 0);<%}%>
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
	<div id="dashboard">
		<div id="chart1"></div>
		<div id="chart2"></div>
		<div id="chart3"></div>
		<div id="control"></div>
		<div id="chart4"></div>
	</div>
	<div id="map_canvas"></div>

	<br />
	<h2>Device</h2>
	<table>
		<%
		    for (Field field : device.getClass().getDeclaredFields()) {
					    field.setAccessible(true);
					    String name = field.getName();
					    Object value = field.get(device);
		%><tr>
			<td><%=name%></td>
			<td><%=value%></td>
		</tr>
		<%
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
		%><tr>
			<td>device</td>
			<td>...</td>
		</tr>
		<%
		    }
						    else if (name == "points") {
		%><tr>
			<td>points</td>
			<td>...</td>
		</tr>
		<%
		    }
						    else {
		%><tr>
			<td><%=name%></td>
			<td><%=value%></td>
		</tr>
		<%
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
		%><tr>
			<td>magneticPoints</td>
			<td>...</td>
		</tr>
		<%
		    }
						    else {
		%><tr>
			<td><%=name%></td>
			<td><%=value%></td>
		</tr>
		<%
		    }  
						}
					}
		%>
	</table>


	<script type="text/javascript">
    // Load the Visualization API and the piechart package.
    google.load('visualization', '1.1', {
        'packages' : [ 'corechart', 'controls' ]
    });
 
    // Set a callback to run when the Google Visualization API is loaded.
    google.setOnLoadCallback(drawCharts);
 	
    function drawCharts() {
    	drawVisualization();
    }
    
    
	
	function drawVisualization() {
		
		var chartAreaLeft = 80;
		var chartAreaHeight = '80%';
		var chartAreaWidth = 680;
		var chartWidth = 900;
		var chartHeight = 300;
		var controlChartHeight = 70;
		
		var dashboard = new google.visualization.Dashboard(
		     document.getElementById('dashboard'));
		
		var control = new google.visualization.ControlWrapper({
		  'controlType': 'ChartRangeFilter',
		  'containerId': 'control',
		  'options': {
		    // Filter by the time axis.
		    'filterColumnIndex': 0,
		    'ui': {
		      'chartType': 'LineChart',
		      'chartOptions': {
		        'chartArea': {'left':chartAreaLeft, 'width': chartAreaWidth},
		        'hAxis': {'baselineColor': 'none', 'minValue': 0, 'maxValue': <%=endTime%>},
			    'width': chartWidth,
			    'height': controlChartHeight
		      },
		      // Display a single series that shows the closing value of the stock.
		      // Thus, this view has two columns: the date (axis) and the stock value (line series).
		      'chartView': {
		        'columns': [0, 1, 2, 8]
		      },
		      // 2 seconds
		      'minRangeSize': 1
		    }
		  },
		  'state': {'range': {'start': 0, 'end': 30}}
		});
		
		var chart1 = new google.visualization.ChartWrapper({
		  'chartType': 'LineChart',
		  'containerId': 'chart1',
		  'options': {
		    // Use the same chart area width as the control for axis alignment.
		    'chartArea': {'left': chartAreaLeft,'height': chartAreaHeight, 'width': chartAreaWidth},
		    'series' : [{"lineWidth": 1, "pointSize": 2},
		                {"lineWidth": 1, "pointSize": 2},
		                {"lineWidth": 1, "pointSize": 2}],
		    'vAxis': {'title': "windspeed (m/s)"},
		    //'legend': {'position': 'none'},
		    'width': chartWidth,
		    'height': chartHeight
		  },
		  // Convert the first column from 'date' to 'string'.
		  'view': {
		    'columns': [0, 1, 2, 8]
		  }
		});
		
		var chart2 = new google.visualization.ChartWrapper({
			  'chartType': 'LineChart',
			  'containerId': 'chart2',
			  'options': {
			    // Use the same chart area width as the control for axis alignment.
			    'chartArea': {'left': chartAreaLeft, 'height': chartAreaHeight, 'width': chartAreaWidth},
			    'series' : [{"lineWidth": 1, "pointSize": 0}, 
				            {"lineWidth": 1, "pointSize": 0}, 
				            {"lineWidth": 1, "pointSize": 0}],
			    'vAxis': {'title': "magneticField (mu-Tesla)"},
			    //'legend': {'position': 'none'},
			    'width': chartWidth,
			    'height': chartHeight
			  },
			  // Convert the first column from 'date' to 'string'.
			  'view': {
			    'columns': [0,5,6,7]
			  }
			});
		
	    var chart3 = new google.visualization.ChartWrapper({
	        'chartType': 'LineChart',
	        'containerId': 'chart3',
	        'options': {
	          // Use the same chart area width as the control for axis alignment.
	          'chartArea': {'left': chartAreaLeft, 'height': chartAreaHeight, 'width': chartAreaWidth},
	          'series' : [{"lineWidth": 1, "pointSize": 1}],
	          'vAxis': {'title': "SampleFrequency (Hz)"},
	          //'legend': {'position': 'none'},
	          'width': chartWidth,
	          'height': chartHeight
	        },
	        // Convert the first column from 'date' to 'string'.
	        'view': {
	          'columns': [0,4, 10]
	        }
	      });
		
	  var chart4 = new google.visualization.ChartWrapper({
	        'chartType': 'ScatterChart',
	        'containerId': 'chart4',
	        'options': {
	          // Use the same chart area width as the control for axis alignment.
	          'chartArea': {'left': chartAreaLeft, 'height': chartAreaHeight, 'width': chartAreaWidth},
	          'series' : [{"lineWidth": 0, "pointSize": 1}, 
	                    {"lineWidth": 0, "pointSize": 1}],
	          'vAxis': {'title': "Amplitude (mu-Tesla)"},
	          //'legend': {'position': 'none'},
	          'width': chartWidth,
	          'height': chartHeight
	        },
	        // Convert the first column from 'date' to 'string'.
	        'view': {
	          'columns': [11,3, 9]
	        }
	      });
		
		
	  var cols = [{id: 'time', label: 'time', type: 'number'},
	              {id: 'windspeed', label: 'windspeed', type: 'number'},
	              {id: 'frequencyRef', label: 'frequencyRef', type: 'number'},
	              {id: 'amplitudeRef', label: 'amplitudeRef', type: 'number'},
	              {id: 'sfRef', label: 'sfRef', type: 'number'},
	              {id: 'magx', label: 'magx', type: 'number'},
	              {id: 'magy', label: 'magy', type: 'number'},
	              {id: 'magz', label: 'magz', type: 'number'},
	              {id: 'frequency', label: 'frequency', type: 'number'},
	              {id: 'amplitude', label: 'amplitude', type: 'number'},
	              {id: 'SF', label: 'SF', type: 'number'},
	              {id: 'freq', label: 'freq', type: 'number'},
	              ];
	  
	  
	  var rows = [
<%for (int i = 0; i < mesPoints.size() ; i++) {%>{c:[{v:<%=mpTime[i]%>}, {v:<%=mesPoints.get(i).getWindSpeed()%>}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}]},
<%}
if (freqEventsRef != null) {
  for (int i = 0; i < freqEventsRef.size() ; i++) {%>{c:[{v:<%=freqEventsRef.get(i).getTime()%>}, {}, {v:<%=freqEventsRef.get(i).values[0]%>}, {v:<%=freqEventsRef.get(i).values[1]%>}, {v:<%=freqEventsRef.get(i).values[2]%>}, {}, {}, {}, {}, {}, {}, {v:<%=freqEventsRef.get(i).values[0]%>}]},
<%}
}
if (freqEvents != null) {
  for (int i = 0; i < freqEvents.size() ; i++) {%>{c:[{v:<%=freqEvents.get(i).getTime()%>}, {}, {}, {}, {}, {}, {}, {}, {v:<%=freqEvents.get(i).values[0]%>}, {v:<%=freqEvents.get(i).values[1]%>}, {v:<%=freqEvents.get(i).values[2]%>}, {v:<%=freqEvents.get(i).values[0]%>}]},
<%}
}
if (magEvents != null) {
  for (int i = 0; i < magEvents.size() ; i++) {%>{c:[{v:<%=magEvents.get(i).getTime()%>}, {}, {}, {}, {}, {v:<%=magEvents.get(i).values[0]%>}, {v:<%=magEvents.get(i).values[1]%>}, {v:<%=magEvents.get(i).values[2]%>}, {}, {}, {}, {}]},
<%}
}%>
	              ];
	  
	  
	  var dataTable = new google.visualization.DataTable({'cols': cols, 'rows': rows}, 0.6);	  
		
 		dashboard.bind(control, chart1);
 		dashboard.bind(control, chart2);
 		dashboard.bind(control, chart3);
 		dashboard.bind(control, chart4);
 		
		dashboard.draw(dataTable);
		
	}	
 	
</script>

</body>
</html>
<%
    if (hibernateSession.getTransaction() != null
            && hibernateSession.getTransaction().isActive()) {
        hibernateSession.getTransaction().rollback();
    }
    hibernateSession.close();
%>