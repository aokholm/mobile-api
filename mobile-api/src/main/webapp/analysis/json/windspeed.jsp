<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,org.hibernate.*,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.vaavud.server.api.util.*,com.fasterxml.jackson.databind.*"%><%

    if (!"2gh7yJfJ6H".equals(request.getParameter("pass"))) {
        ServiceUtil.sendUnauthorizedErrorResponse(response);
        return;
    }
    
    String measurementPointsSQLend;
    if (request.getParameter("session_id") == null) {
    	measurementPointsSQLend = "ORDER BY  MS.id DESC LIMIT 0,1";
    } else {
    	measurementPointsSQLend = String.format("WHERE MS.id = %s", request.getParameter("session_id"));
    }
               
    Session hibernateSession = Model.get().getSessionFactory().openSession();
         
    List<Object[]> measurementPoints = hibernateSession.createSQLQuery(
       		"SELECT (MP.time-MS.startTime)/1000 as time, MP.windSpeed, MP.winddirection " + 
       	    "FROM MeasurementPoint AS MP " +
       		"INNER JOIN MeasurementSession AS MS on MP.session_id = MS.id " +
       		measurementPointsSQLend).list();
%>

{ "cols": [
	  {"id":"","label":"Time","pattern":"","type":"number"},
	  {"id":"","label":"Windspeed","pattern":"","type":"number"}
	],
"rows" : [
<%
Iterator<Object[]> itr = measurementPoints.iterator();
while(itr.hasNext()) {
	Object[] values = itr.next();	
	%>{"c":[{"v":<%=values[0]%>},{"v":<%=values[1]%>}]}<% 
	if (itr.hasNext()) { %>,<% }
} %>
		]
}