<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
    
    var measurementLatlng = new google.maps.LatLng(<c:out value="${latitude}"/>, <c:out value="${longitude}"/>);
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
   <c:forEach var="chart" items="${charts}" varStatus="theCount">
    <div id="<c:out value="${chart.identifier}"/>"></div>
      <c:if test="${theCount.count == 3}">
      <div id="control"></div>
      </c:if>
   </c:forEach>

	</div>
	<div id="map_canvas"></div>

	<br />
<%-- 	<%=mHelper.getTable("Device", mHelper.getDevice())%> --%>
<%-- 	<%=mHelper.getTable("MeasurementSession", mHelper.getMeasurementSession())%> --%>
<%-- 	<%=mHelper.getTable("MagneticSession", mHelper.getMagneticSession())%> --%>
  
  
  <div id="table"></div>
  
	<script type="text/javascript">
    // Load the Visualization API and the piechart package.
    google.load('visualization', '1.1', {
        'packages' : [ 'corechart', 'controls', 'table' ]
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
		        'hAxis': {'baselineColor': 'none', 'minValue': 0, 'maxValue': <%=request.getAttribute("endTime")%>},
			    'width': chartWidth,
			    'height': controlChartHeight
		      },
		      // Display a single series that shows the closing value of the stock.
		      // Thus, this view has two columns: the date (axis) and the stock value (line series).
		      'chartView': {
		        'columns': [0, 1]
		      },
		      // 2 seconds
		      'minRangeSize': 1
		    }
		  },
		  'state': {'range': {'start': 0, 'end': 30}}
		});
		
// 		var chart1 = new google.visualization.ChartWrapper({
// 		  'chartType': 'ScatterChart',
// 		  'containerId': 'chart1',
// 		  'options': {
// 		    // Use the same chart area width as the control for axis alignment.
// 		    'chartArea': {'left': chartAreaLeft,'height': chartAreaHeight, 'width': chartAreaWidth},
// 		    'series' : [{"lineWidth": 1, "pointSize": 2},
// 		                {"lineWidth": 1, "pointSize": 2},
// 		                {"lineWidth": 1, "pointSize": 2}],
// 		    'vAxis': {'title': "windspeed (m/s)"},
// 		    //'legend': {'position': 'none'},
// 		    'width': chartWidth,
// 		    'height': chartHeight
// 		  },
// 		  'view': {
// 		    'columns': [0, 1]
// 		  }
// 		});
		
// 		var chart2 = new google.visualization.ChartWrapper({
// 			  'chartType': 'ScatterChart',
// 			  'containerId': 'chart2',
// 			  'options': {
// 			    // Use the same chart area width as the control for axis alignment.
// 			    'chartArea': {'left': chartAreaLeft, 'height': chartAreaHeight, 'width': chartAreaWidth},
// 			    'series' : [{"lineWidth": 1, "pointSize": 0}, 
// 				            {"lineWidth": 1, "pointSize": 0}, 
// 				            {"lineWidth": 1, "pointSize": 0}],
// 			    'vAxis': {'title': "magneticField (mu-Tesla)"},
// 			    //'legend': {'position': 'none'},
// 			    'width': chartWidth,
// 			    'height': chartHeight
// 			  },
// 			  // Convert the first column from 'date' to 'string'.
// 			  'view': {
// 			    'columns': [0,2,3,4]
// 			  }
// 			});
		
// 	    var chart3 = new google.visualization.ChartWrapper({
// 	        'chartType': 'ScatterChart',
// 	        'containerId': 'chart3',
// 	        'options': {
// 	          // Use the same chart area width as the control for axis alignment.
// 	          'chartArea': {'left': chartAreaLeft, 'height': chartAreaHeight, 'width': chartAreaWidth},
// 	          'series' : [{"lineWidth": 1, "pointSize": 1},
// 	                      {"lineWidth": 1, "pointSize": 1}],
// 	          'vAxis': {'title': "SampleFrequency (Hz)"},
// 	          //'legend': {'position': 'none'},
// 	          'width': chartWidth,
// 	          'height': chartHeight
// 	        },
// 	        // Convert the first column from 'date' to 'string'.
// 	        'view': {
// 	          'columns': [0,8, 11]
// 	        }
// 	      });
		
// 	  var chart4 = new google.visualization.ChartWrapper({
// 	        'chartType': 'ScatterChart',
// 	        'containerId': 'chart4',
// 	        'options': {
// 	          // Use the same chart area width as the control for axis alignment.
// 	          'series' : [{"lineWidth": 0, "pointSize": 1}, 
// 	                    {"lineWidth": 0, "pointSize": 1}],
// 	          'vAxis': {'title': "Amplitude (mu-Tesla)"}
// 	        },
// 	        'view': {
// 	          'columns': [5, 7, 10]
// 	        }
// 	      });
		
	  
	  <c:forEach var="chart" items="${charts}">
	      var <c:out value="${chart.identifier}"/> = new google.visualization.ChartWrapper(
	    		   <c:out value="${chart.chartJSON}" escapeXml="false"/>);
	      <c:out value="${chart.identifier}"/>.setOption('chartArea', {
	    	  'left': chartAreaLeft, 
	    	  'height': chartAreaHeight, 
	    	  'width': chartAreaWidth});
	      <c:out value="${chart.identifier}"/>.setOption('width', chartWidth);
	      <c:out value="${chart.identifier}"/>.setOption('height', chartHeight);
	  </c:forEach>
	  
	  
	  
	  
	  
 	  var data = <c:out value="${dataTable}" escapeXml="false"/>;
	  
	  var dataTable = new google.visualization.DataTable(data, 0.6);	  
		
	  
	  <c:forEach var="chart" items="${charts}">
      dashboard.bind(control, <c:out value="${chart.identifier}"/>);
    </c:forEach>
 		
		dashboard.draw(dataTable);
		
    var chart = new google.visualization.Table(document.getElementById('table'));
    chart.draw(dataTable);
		
	}	
 	
</script>

</body>
</html>