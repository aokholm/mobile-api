<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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

		var measurementLatlng = new google.maps.LatLng(
				<c:out value="${latitude}"/>, <c:out value="${longitude}"/>);

		var mapCanvas1 = document.getElementById('map_canvas1');
		var mapOptions1 = {
			center : measurementLatlng,
			zoom : 18,
			mapTypeId : google.maps.MapTypeId.SATELLITE
		};
		var map1 = new google.maps.Map(mapCanvas1, mapOptions1);

		var marker1 = new google.maps.Marker({
			position : measurementLatlng,
			map : map1,
			title : "!"
		});

		var mapCanvas2 = document.getElementById('map_canvas2');
		var mapOptions2 = {
			center : measurementLatlng,
			zoom : 4,
			mapTypeId : google.maps.MapTypeId.SATELLITE
		};
		var map2 = new google.maps.Map(mapCanvas2, mapOptions2);
		var marker2 = new google.maps.Marker({
			position : measurementLatlng,
			map : map2,
			title : "!"
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
			<button onclick="changeRange();">Set time: 10:40</button>
			<div id="jsAnalysis"></div>
		</div>
	</div>
	<div id="col2">
		<c:forEach var="sensor" items="${sensors}">
			<c:out value="${sensor}" />
			<br />
		</c:forEach>

		<c:out value="${deviceTable}" escapeXml="false" />
		<div id="map_canvas1" class="map_canvas"></div>
		<div id="map_canvas2" class="map_canvas"></div>
		<c:out value="${measurementSessionTable}" escapeXml="false" />
		<c:out value="${magneticSessionTable}" escapeXml="false" />

		<!-- Data table -->
		<button onclick="table.draw(dataView);">Show Table</button>
		<div id="table"></div>

	</div>



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

		function changeRange() {
			control.setState({
				'range' : {
					'start' : 10,
					'end' : 40
				}
			});
			control.draw();
		}
		
		var ms = {};
		
		ms.startTime = 0;
		<c:if test="${not empty startTime}">
		  ms.startTime = <c:out value="${startTime}"/>;
    </c:if>
    
    ms.sampleId = null;
    ms.windSpeed = null;
    ms.testDescription = null;
    
    source = "<c:out value="${measurementSession.source}"/>";
    <c:if test="${not empty measurementSession.source}">
    splitStr = source.split(", ");
    if (splitStr.length == 3) {
    	ms.sampleId = splitStr[0];
    	ms.windSpeed = splitStr[1];
    	ms.testDescription = splitStr[2];
    }
    </c:if> 


		var table;
		var dataView;
		var control;

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

			var dashboard = new google.visualization.Dashboard(document
					.getElementById('dashboard'));

			//
			// Control chart
			// 
			control = new google.visualization.ControlWrapper(
					{
						'controlType' : 'ChartRangeFilter',
						'containerId' : 'control',
						'options' : {
							// Filter by the time axis.
							'filterColumnIndex' : 0,
							'ui' : {
								'chartType' : 'LineChart',
								'chartOptions' : {
									'chartArea' : {
										'left' : chartAreaLeft,
										'width' : chartAreaWidth
									},
									'hAxis' : {
										'baselineColor' : 'none',
										'minValue' : 0
									},
									'width' : chartWidth,
									'height' : controlChartHeight
								},
								// Display a single series that shows the closing value of the stock.
								// Thus, this view has two columns: the date (axis) and the stock value (line series).
								'chartView' : {
									'columns' : [
	<%=request.getAttribute("controlChartColumn")%>
		]
								},
								// 2 seconds
								'minRangeSize' : 0.1
							}
						},
						'state' : {
							'range' : {
								'start' : 0,
								'end' : 30
							}
						}
					});

			//
			// Settings for each chart 
			// 
			<c:forEach var="chart" items="${charts}">
			var <c:out value="${chart.identifier}"/> = new google.visualization.ChartWrapper(
					<c:out value="${chart.chartJSON}" escapeXml="false"/>);
			<c:out value="${chart.identifier}"/>.setOption('chartArea', {
				'left' : chartAreaLeft,
				'height' : chartAreaHeight,
				'width' : chartAreaWidth
			});
			<c:out value="${chart.identifier}"/>.setOption('width', chartWidth);
			<c:out value="${chart.identifier}"/>.setOption('height',
					chartHeight);
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

			dataView = new google.visualization.DataView(dataTable);
			table = new google.visualization.Table(document
					.getElementById('table'));

			dashboard.draw(dataTable);

			google.visualization.events.addListener(dashboard, 'ready',
					function() {
						jsFreqAnalysis();
					});

			function jsFreqAnalysis() {
				controlCols = control.getOption('ui.chartView').columns;

				freqCols = controlCols.slice(1);
				rows = [];

				for (var i = 0; i < freqCols.length; i++) {
					rows[i] = dataView.getFilteredRows([ {
						column : 0,
						minValue : control.getState().range.start,
						maxValue : control.getState().range.end
					}, {
						column : freqCols[i],
						minValue : 0
					} // remove null
					]);
				}
				 
				means = [];
				maxs = [];
				mins = [];
				stds = [];
				time = [];

				for (var i = 0; i < freqCols.length; i++) {
					sum = 0;
					max = 0;
					min = dataView.getValue(rows[i][0], freqCols[i]);
					SS = 0;
					timeView = Math.floor(ms.startTime + dataView.getValue(rows[i][0], 0)*1000);
					console.log(timeView);
					
					date = new Date(timeView);
					
					console.log(date);
					
					timeStr = date.getFullYear() + "-" + ('0' + date.getMonth()).slice(-2)  + "-" + ('0' + date.getDay()).slice(-2) + " "
					  + ('0' + date.getHours()).slice(-2) + ":" + ('0' + date.getMinutes()).slice(-2) + ":" + ('0' + date.getSeconds()).slice(-2) 
					  + "," + ('0000' + timeView%1000).slice(-3);
					
					console.log(timeStr);

					for (var j = 0; j < rows[i].length; j++) {
						val = dataView.getValue(rows[i][j], freqCols[i]);
						// sum
						sum += val;
						// max
						if (val > max) {
							max = val;
						}

						if (val < min) {
							min = val;
						}

						SS += Math.pow(val, 2);

					}

					means[i] = sum / rows[i].length;
					maxs[i] = max;
					mins[i] = min;
					stds[i] = Math.sqrt(1 / rows[i].length * SS
							- Math.pow(means[i], 2));
					time[i] = timeStr; 
				}
				  
				analysis = "<table><tr><th>name</th><th>time</th><th>mean</th><th>max</th><th>min</th><th>std</th>";
				analysis += "<th>sampleId</th><th>WindSpeed</th><th>testDescrip</th></tr>";

				for (var i = 0; i < freqCols.length; i++) {
					name = dataView.getColumnLabel(freqCols[i]);
					analysis += "<tr><td>" + name 
					    + "</td><td>" + time[i] 
					    + "</td><td>" + means[i].toPrecision(5)
							+ "</td><td>" + maxs[i].toPrecision(4) 
							+ "</td><td>" + mins[i].toPrecision(4)
							+ "</td><td>" + stds[i].toPrecision(4)
							+ "</td><td>" + ms.sampleId
							+ "</td><td>" + ms.windSpeed
							+ "</td><td>" + ms.testDescription + "</td></tr>";
				}

				analysis += "</table>";

				$("#jsAnalysis").html(analysis);
			}
		}
	</script>

</body>
</html>