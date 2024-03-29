<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,org.hibernate.*,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.vaavud.server.api.util.*,com.vaavud.util.MathUtil,com.fasterxml.jackson.databind.*"%><%

    if (!"2gh7yJfJ6H".equals(request.getParameter("pass"))) {
        ServiceUtil.sendUnauthorizedErrorResponse(response);
        return;
    }
         
    Session hibernateSession = Model.get().getSessionFactory().openSession();

    Number numberOfMeasurements = (Number) hibernateSession.createSQLQuery(
            "select count(*) " +
            "from MeasurementSession " +
            "where from_unixtime(startTime/1000)>'2013-08-01 00:00:00'").uniqueResult();

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
    
    Long actualTime = new Date().getTime();
    String maxSpeedLocationSql =
			"SELECT "+
				"windSpeedAvg, "+
				"windDirection, "+
				"from_unixtime(creationTime/1000), "+
				"geoLocationNameLocalized, "+
				"latitude, "+
				"longitude, "+
				"id, "+
				"endIndex "+
			"FROM "+
			 "MeasurementSession "+
			"WHERE "+
				"deleted != '1' "+
				"AND "+
				"longitude is not null "+
				"AND "+
				"startTime > " + (actualTime-604800000) +" "+
				"AND "+
				"endTime < " + actualTime +" "+
				"AND "+
				"endIndex > 30 " +
			"ORDER BY windSpeedAvg DESC LIMIT 0,10";
			
	List<Object[]> highestWindSpeed = hibernateSession.createSQLQuery(maxSpeedLocationSql).list();
	
  List<Object[]> usersPerDay = hibernateSession.createSQLQuery(
    	    "select date(from_unixtime(creationTime/1000)) as day, " + 
    	           "count(*) as countPerDay, " +
    	           "sum(if(passwordHash,1,0)) as password, " +
    	           "sum(if(facebookId,1,0)) as facebook " +
    	    "from User " +
            "where from_unixtime(creationTime/1000) > '2014-03-20 00:00:00' " +
    	    "group by date(from_unixtime(creationTime/1000)) " +
            "order by date(from_unixtime(creationTime/1000)) desc").list();
    
    List<Object[]> devicesPerDayPerCountry = hibernateSession.createSQLQuery(
    	    "select date(from_unixtime(creationTime/1000)) as day, " +
    	    "country, " +
    	    "count(*) as countPerDay " + 
    	    "from Device " +
    	    "where date(from_unixtime(creationTime/1000)) > date_sub(now(), interval 10 day) " +
    	    "group by date(from_unixtime(creationTime/1000)), country " +
    	    "having count(*) > 5 " +
    	    "order by date(from_unixtime(creationTime/1000)) desc, count(*) desc").list();

    List<Object[]> measurementsPerDay = hibernateSession.createSQLQuery(
    		"select date(from_unixtime(startTime/1000)) as day, count(*) as countPerDay, " +
            "sum(if(strcmp(d.os,'iPhone OS')=0,1,0)) as iPhone, " +
    		"sum(if(strcmp(d.os,'Android')=0,1,0)) as android, " +
            "sum(if(s.windSpeedAvg,1,0)) as realPerDay, " +
    		"sum(if(strcmp(d.os,'iPhone OS')=0,if(s.windSpeedAvg,1,0),0)) as iPhoneReal, " +
            "sum(if(strcmp(d.os,'Android')=0,if(s.windSpeedAvg,1,0),0)) as androidReal " +
            "from MeasurementSession s,Device d " +
    		"where s.device_id=d.id and from_unixtime(startTime/1000) > '2013-08-01 00:00:00' and from_unixtime(startTime/1000) < now() " +
            "group by date(from_unixtime(startTime/1000)) " +
    		"order by date(from_unixtime(startTime/1000)) desc;").list();

    List<Object[]> measurementsPerCountry = hibernateSession.createSQLQuery(
    		"select country, count(*) " +
    		"from MeasurementSession m, Device d " +
    		"where m.device_id=d.id " +
    		"group by d.country " +
    		"order by count(*) desc").list();

    List<Object[]> deviceByOS = hibernateSession.createSQLQuery(
    		"select os, count(*) from Device group by os order by count(*) desc").list();

    List<Object[]> deviceByOSByMonth = hibernateSession.createSQLQuery(
            "select year(from_unixtime(creationTime/1000)) as year, " +
                   "month(from_unixtime(creationTime/1000)) as month, " +
                   "sum(if(os='iPhone OS',1,0)) as ios, " +
                   "sum(if(os='Android',1,0)) as android " + 
            "from Device " +
            "group by year(from_unixtime(creationTime/1000)), month(from_unixtime(creationTime/1000)) " +
            "order by year(from_unixtime(creationTime/1000)) desc, month(from_unixtime(creationTime/1000)) desc").list();

    List<Object[]> models = hibernateSession.createSQLQuery(
            "select os, model, count(*) " +
            "from Device " +
            "group by os, model " +
            "having count(*) > 200 " +
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
    <%
    int i = 1;
    for (Object[] values: highestWindSpeed) {
        %><tr><td> Max Speed (within last 7 days) #<%=i %> </td>
          <td><%=values[0]  %></td>
          <td><%=MathUtil.toCardinal((Float)values[1]) %></td>
          <td><%=values[2] %></td>
          <td><%=values[3] %></td>
          <td><%=values[4] %></td>
          <td><%=values[5] %></td>
          <td><a href="http://maps.google.com/maps?z=12&t=h&q=loc:<%=values[4] %>+<%=values[5]%>">map</a></td>
          <td><a href="/analysis/measurement?pass=2gh7yJfJ6H&session_id=<%=values[6] %>">details</a></td></tr> <% 
    i++;
    }
    %>
    <tr><td># of measurements:</td><td><%=numberOfMeasurements%></td></tr>
    <tr><td>Avg # of measurements per day:</td><td><%=Math.round(allTimeAvgMeasurementsPerDay.doubleValue())%></td></tr>
    <tr><td># of countries with measurements:</td><td><%=countriesWithMeasurements%></td></tr>
    <tr><td># of devices:</td><td><%=numberOfDevices%></td></tr>
  </table>

  <table>
    <tr><th class="left">Date</th><th class="right"># of users</th><th class="right"># with password</th><th class="right"># with Facebook</th></tr>
    <%
    for (Object[] values : usersPerDay) {
        %><tr><td class="left"><%=values[0]%></td><td class="right"><%=values[1]%></td><td class="right"><%=values[2]%></td><td class="right"><%=values[3]%></td></tr><%
    }
    %>
  </table>

  <table>
    <tr><th class="left">Date</th><th class="right">Country</th><th class="right"># of devices (>5)</th></tr>
    <%
    for (Object[] values : devicesPerDayPerCountry) {
        %><tr><td class="left"><%=values[0]%></td><td class="right"><%=values[1]%></td><td class="right"><%=values[2]%></td></tr><%
    }
    %>
  </table>

  <table>
    <tr><th class="left">Date</th><th class="right"># of measurements</th><th class="right"># on iPhone</th><th class="right"># on Android</th><th class="right"># of real</th><th class="right"># of real iPhone</th><th class="right"># of real Android</th></tr>
    <%
    for (Object[] values : measurementsPerDay) {
    	%><tr><td class="left"><%=values[0]%></td><td class="right"><%=values[1]%></td><td class="right"><%=values[2]%></td><td class="right"><%=values[3]%></td><td class="right"><%=values[4]%></td><td class="right"><%=values[5]%></td><td class="right"><%=values[6]%></td></tr><%
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
    <tr><th class="left">OS</th><th class="right">#</th></tr>
    <%
    for (Object[] values : deviceByOS) {
        %><tr><td class="left"><%=values[0]%></td><td class="right"><%=values[1]%></td></tr><%
    }
    %>
  </table>

  <table>
    <tr><th class="left">Month</th><th class="right"># of iOS devices</th><th class="right"># of Android devices</th></tr>
    <%
    for (Object[] values : deviceByOSByMonth) {
        %><tr><td class="left"><%=values[0] + "-" + values[1]%></td><td class="right"><%=values[2]%></td><td class="right"><%=values[3]%></td></tr><%
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