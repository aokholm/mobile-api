<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,java.text.DecimalFormat,org.hibernate.*,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.vaavud.server.api.util.*,com.fasterxml.jackson.databind.*,org.apache.log4j.Logger"%><%

    String pass = "2gh7yJfJ6H";     
   	Logger logger = Logger.getLogger("SESSIONS.JSP");
         
    if (!"2gh7yJfJ6H".equals(request.getParameter("pass"))) {
        ServiceUtil.sendUnauthorizedErrorResponse(response);
        return;
    }
         
    Session hibernateSession = Model.get().getSessionFactory().openSession();
         
    String[] queryParams = {"D.vendor", "D.model", "D.id", "D.os", "MS.windMeter"};
    
    String whereClause = "";
    boolean isFirst = true;
    for (String param : queryParams) {
    	if (request.getParameter(param) != null && request.getParameter(param) != ""){
    		if (isFirst){
    			whereClause += " where ";
    			isFirst = false;
    		}else{
    			whereClause += " AND ";
    		}
    		whereClause +=  param + " like '" + request.getParameter(param) + "' ";
    		
    	}
    }
    
    String limit = "1000";
    
    String sql = 
    	"SELECT                                                                          " +
    	"    MS.id,                                                                      " +
    	"    MagS.id as magneticSession_id,                                              " +
    	"    D.id as device_id,                                                          " +
    	"    D.vendor,                                                                   " +
    	"	   D.model,                                                                    " +
    	"    D.magneticFieldSensor,                                                      " +
    	"    D.appVersion,                                                               " +
    	"    from_unixtime(MS.startTime/1000) As startTime,                              " +     
    	"    MS.windSpeedAvg,                                                            " +
    	"    MS.windSpeedMax,                                                            " +
    	"    count(MP.id) As measurementPoints,                                          " +
    	"	   (MS.endTime - MS.startTime ) / 1000 as measurementLength,                   " +
    	"    D.osVersion                                                                 " +
    	"FROM                                                                            " +
    	"    Device AS D                                                                 " +
    	"        INNER JOIN                                                              " +
    	"    MeasurementSession AS MS ON D.id = MS.device_id                             " +
    	"        LEFT JOIN                                                               " +
    	"    MagneticSession AS MagS ON MS.uuid = MagS.measurementSessionUuid            " +
    	"        LEFT JOIN                                                               " +
    	"    MeasurementPoint AS MP ON MS.id = MP.session_id                             " +
    	whereClause +                                                                    
    	"GROUP BY MS.id                                                                  " +
    	"order by MS.id  																" +
    	"DESC                                                                            " +
    	"LIMIT 0 , " + limit;                                                                
	
    logger.info("Sessions SQL: "+ sql);
    
    List<Object[]> session_ids = hibernateSession.createSQLQuery(sql).list();
    
    DecimalFormat dfd = new DecimalFormat("0.00");
    DecimalFormat dfs = new DecimalFormat("0.0");


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

	<form action="#" method="get">
		<table>
		<% for (String param : queryParams) { %>
			<tr><td><%=param %></td><td><input type="text" name="<%=param%>" value="<%=request.getParameter(param)%>"></td></tr>
		<%}%>
			<tr><td>Extended</td><td><input type="checkbox" name="extended" value="1"<% 
				if ("1".equals(request.getParameter("extended"))) {
					%> checked="checked" <%
			} %>></td></tr>
		  </table>
		<input type="hidden" name="pass" value="<%=pass%>">
	  <input type="submit" value="Submit">
	</form>

  <table>
    <tr>
    	<th class="left">MS id</th>
    	<th class="left">MagS id</th>
    	<th class="left">D id</th>
    	<th class="left">Vendor</th>
    	<th class="left">Model</th>
    	<th class="left">MFSensor</th>
    	<th class="left">AppVersion</th>
    	<th class="left">osVersion</th>
    	<th class="left">StartTime</th>
    	<th class="left">Mean</th>
    	<th class="left">Max</th>
    	<th class="left">#Measurement Points</th>
    </tr>
    <%
    for (Object[] values : session_ids) {
        %><tr>
        	<td class="right"><a href='../analysis/measurement?pass=<%=pass%>&session_id=<%=values[0]%>'><%=values[0]%></a></td>
        	<td class="right"><%=values[1]%></td>
        	<td class="right"><%=values[2]%></td>
        	<td class="left"><%=values[3]%></td>
        	<td class="left"><%=values[4]%></td>
        	<td class="left"><%=values[5]%></td>
        	<td class="left"><%=values[6]%></td>
        	<td class="left"><%=values[12]%></td>
        	<td class="left"><%=values[7]%></td>
          <td class="right"><%=values[8]%></td>
        	<td class="right"><%=values[9]%></td>
        	<td class="right"><%=values[10]%></td>
        </tr><%
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