<%@page import="org.hibernate.type.IntegerType"%>
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
  </style>
  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"
        type="text/javascript"></script>
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
</head>
<body>
	<div id="chart_div"></div>
	
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
    google.setOnLoadCallback(drawChart);
 	
     	
 	function drawChart() {
    	
    	var parameters = {
    		"pass" : "2gh7yJfJ6H", 
    		"session_id" : "<%=measurementSession.getId()%>"
    	};
    	   	
    	var options = 
    	{
    		"title" : "Wind speed",
    		"series" : [{"lineWidth": 0, "pointSize": 2}]
    	};
    	
    	$.getJSON("/analysis/json/windspeed.jsp", parameters, function(json) {
    	    var data = new google.visualization.DataTable(json);
    	    
            // Instantiate and draw our chart, passing in some options.
            var chart = new google.visualization.LineChart(document
                    .getElementById('chart_div'));
            chart.draw(data, options);
    	});
    	
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