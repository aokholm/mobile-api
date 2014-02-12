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

#col1 {
  width: 1000px;
  float: left;
}

#col2 {
  width: 900px;
  float: left;
}

.map_canvas {
	width: 450px;
	height: 400px;
	float: left;
}


</style>
<script type="text/javascript"
	src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>
<script>
  function initialize() {
    
	  <c:if test="${not empty latitude}">
	  
		  var measurementLatlng = new google.maps.LatLng(<c:out value="${latitude}"/>, <c:out value="${longitude}"/>);
		  
		  var mapCanvas1 = document.getElementById('map_canvas1');
	    var mapOptions1 = {
	      center: measurementLatlng,
	      zoom: 18,
	      mapTypeId: google.maps.MapTypeId.SATELLITE
	    };
	    var map1 = new google.maps.Map(mapCanvas1, mapOptions1);
	   
	    var marker1 = new google.maps.Marker({
	        position: measurementLatlng,
	        map: map1,
	        title:"!"
	    });
	    
	    
	    var mapCanvas2 = document.getElementById('map_canvas2');
	    var mapOptions2 = {
	    	      center: measurementLatlng,
	    	      zoom: 4,
	    	      mapTypeId: google.maps.MapTypeId.SATELLITE
	    	    };
	    var map2 = new google.maps.Map(mapCanvas2, mapOptions2);
	    var marker2 = new google.maps.Marker({
	        position: measurementLatlng,
	        map: map2,
	        title:"!"
	    });
    
    </c:if>
    
  }
  
  google.maps.event.addDomListener(window, 'load', initialize);
</script>
</head>
<body>
  <div id="col1">
	  <div id="dashboard">
	   <c:forEach var="chart" items="${charts}" varStatus="theCount">
	    <div id="<c:out value="${chart.identifier}"/>"></div>
	      <c:if test="${theCount.count == 3}">
	      <div id="control"></div>
	      </c:if>
	   </c:forEach>
	
		</div>
	</div>
	<div id="col2">
		<c:out value="${deviceTable}" escapeXml="false"/>
		<div id="map_canvas1" class="map_canvas"></div>
		<div id="map_canvas2" class="map_canvas"></div>
		<c:out value="${measurementSessionTable}" escapeXml="false"/>
		<c:out value="${magneticSessionTable}" escapeXml="false"/>
  </div>

<!-- Data table -->
<!-- <div id="table"></div> -->
  
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
		//
		// Chart settings
		//
		var chartAreaLeft = 80;
		var chartAreaHeight = '80%';
		var chartAreaWidth = 680;
		var chartWidth = 980;
		var chartHeight = 300;
		var controlChartHeight = 70;
		
		
		var dashboard = new google.visualization.Dashboard(
		     document.getElementById('dashboard'));
		
		//
		// Control chart
		// 
		
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
		
	  
		//
		// Settings for each chart 
		// 
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
	  
	  //
	  // Data for the charts
	  //
	  var data = <c:out value="${dataTable}" escapeXml="false"/>;
	  var dataTable = new google.visualization.DataTable(data, 0.6);	  
		
	  //
	  // Add charts to the dashboard
	  //
	  <c:forEach var="chart" items="${charts}">
      dashboard.bind(control, <c:out value="${chart.identifier}"/>);
    </c:forEach>
 		
		dashboard.draw(dataTable);
		
		// Print data table
//     var chart = new google.visualization.Table(document.getElementById('table'));
//     chart.draw(dataTable);
		
	}	
 	
</script>

</body>
</html>