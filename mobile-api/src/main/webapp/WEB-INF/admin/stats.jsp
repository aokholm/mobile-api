<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,org.hibernate.*,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.fasterxml.jackson.databind.*"%><%

    Session hibernateSession = Model.get().getSessionFactory().openSession();
         
    Number allTimeAvgMeasurementsPerDay = (Number) hibernateSession.createSQLQuery(
    		"select avg(countPerDay) " +
    		"from (" +
    			"select date(from_unixtime(startTime/1000)) as day, count(*) as countPerDay " +
    		    "from MeasurementSession " +
    			"where from_unixtime(startTime/1000)>'2013-08-01 00:00:00' " +
    		    "group by date(from_unixtime(startTime/1000))) a").uniqueResult();

    Number countriesWithMeasurements = (Number) hibernateSession.createSQLQuery(
    		"select count(*) as numOfCountries " +
    		"from (" +
    		    "select count(*) " +
    			"from MeasurementSession m, Device d " +
    		    "where m.device_id=d.id " +
    			"group by d.country) a").uniqueResult();

    Number numberOfDevices = (Number) hibernateSession.createSQLQuery(
            "select count(*) from Device").uniqueResult();

    List<Object[]> measurementsPerDay = hibernateSession.createSQLQuery(
       		"select date(from_unixtime(startTime/1000)) as day, count(*) as countPerDay " + 
       	    "from MeasurementSession " +
       		"where from_unixtime(startTime/1000)>'2013-08-01 00:00:00' " +
       	    "group by date(from_unixtime(startTime/1000))").list();

    List<Object[]> measurementsPerCountry = hibernateSession.createSQLQuery(
    		"select country, count(*) " +
    		"from MeasurementSession m, Device d " +
    		"where m.device_id=d.id " +
    		"group by d.country " +
    		"order by count(*) desc").list();

    List<Object[]> models = hibernateSession.createSQLQuery(
            "select os, model, count(*) " +
            "from Device " +
            "group by os, model " +
            "order by os, count(*) desc").list();

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
</head>
<body>

  <table>
    <tr><td>Avg # of measurements per day:</td><td><%=Math.round(allTimeAvgMeasurementsPerDay.doubleValue())%></td></tr>
    <tr><td># of countries with measurements:</td><td><%=countriesWithMeasurements%></td></tr>
    <tr><td># of devices:</td><td><%=numberOfDevices%></td></tr>
  </table>

  <table>
    <tr><th class="left">Date</th><th class="right"># of measurements</th></tr>
    <%
    for (Object[] values : measurementsPerDay) {
    	%><tr><td class="left"><%=values[0]%></td><td class="right"><%=values[1]%></td></tr><%
    }
    %>
  </table>

  <table>
    <tr><th class="left">Country</th><th class="right"># of measurements</th></tr>
    <%
    for (Object[] values : measurementsPerCountry) {
        %><tr><td class="left"><%=values[0]%></td><td class="right"><%=values[1]%></td></tr><%
    }
    %>
  </table>

  <table>
    <tr><th class="left">OS</th><th class="left">Model</th><th class="right">#</th></tr>
    <%
    for (Object[] values : models) {
        %><tr><td class="left"><%=values[0]%></td><td class="left"><%=values[1]%></td><td class="right"><%=values[2]%></td></tr><%
    }
    %>
  </table>

</body>
</html>
<%
if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
    hibernateSession.getTransaction().rollback();
}
hibernateSession.close();
%>