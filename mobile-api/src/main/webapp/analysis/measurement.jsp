<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,org.hibernate.*,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.vaavud.server.api.util.*,com.fasterxml.jackson.databind.*"%><%
	
    String pass = "2gh7yJfJ6H";     
         
    if (!pass.equals(request.getParameter("pass"))) {
        ServiceUtil.sendUnauthorizedErrorResponse(response);
        return;
    }
    
    Session hibernateSession = Model.get().getSessionFactory().openSession();
    
    String measurementPointsSQLend;
    String session_id;
    if (request.getParameter("session_id") == null) {
    	Number latestSessionId = (Number) hibernateSession.createSQLQuery(
        		"select id " +
        		"from MeasurementSession " +
        		"ORDER BY id DESC limit 0,1").uniqueResult();
    	session_id = latestSessionId.toString();
    } else {
    	session_id = request.getParameter("session_id");
    }
        	

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
    		"session_id" : "<%=session_id%>"
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