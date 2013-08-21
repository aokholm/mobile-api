<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"
         import="java.util.*,org.hibernate.*,com.vaavud.server.model.*,com.vaavud.server.model.entity.*,com.vaavud.server.web.map.*,com.fasterxml.jackson.databind.*"%><%

    List<MapMeasurement> mapMeasurements = Collections.emptyList();
    
    // fetch all measurement sessions
    
    Session hibernateSession = Model.get().getSessionFactory().openSession();
    try {
    	List<MeasurementSession> measurements = hibernateSession.createQuery("from MeasurementSession s where s.position!=null and s.position.latitude!=null and s.position.longitude!=null").list();
        mapMeasurements = MapMeasurement.fromMeasurementSessions(measurements);
    }
    catch (RuntimeException e) {
    	System.err.println("Error in map.jsp fetching data");
    	e.printStackTrace(System.err);
    }
    finally {
        if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
            hibernateSession.getTransaction().rollback();
        }
        hibernateSession.close();
    }
    
    // construct JavaScript
    
    ObjectMapper mapper = new ObjectMapper();
    String jsonMeasurements = mapper.writeValueAsString(mapMeasurements);

%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  <title>Vaavud</title>
  <style type="text/css">
      html {height:100%}
      body {height:100%; margin:0; padding:0}
      #map-canvas {height:100%}
  </style>
  <script type="text/javascript">
      var measurements = <%=jsonMeasurements%>;
  </script>
  <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDrrZsMKRBkCw214SbJA6q2lO-cXbu7m0Y&sensor=false"></script>
  <script type="text/javascript">
      function addMarker(measurement, map) {
          var marker = new google.maps.Marker({
              position: new google.maps.LatLng(measurement.position.latitude,measurement.position.longitude),
              map: map,
              icon: (measurement.iconNum == 0) ? "/gfx/WindMarker.png" : "/gfx/WindMarker2.png"
          });
          var infowindow = new google.maps.InfoWindow({
              content: "Start Time: " + (new Date(measurement.startTime)) + "<br/>" +
                       "End Time: " + (new Date(measurement.endTime)) + "<br/>" +
            	       "Wind Speed Avg: " + measurement.windSpeedAvg + " m/s<br/>" +
                       "Wind Speed Max: " + measurement.windSpeedMax + " m/s<br/>" +
                       "Wind Direction: " + measurement.windDirection + "<br/"
          });
          google.maps.event.addListener(marker, 'click', function() {
                infowindow.open(map,marker);
                map.setZoom(16);
                map.setCenter(marker.getPosition());
              });
      }
  
      function initialize() {
          var mapOptions = {
              center: new google.maps.LatLng(35.68165588378906, 12.607573509216309),
              zoom: 3,
              mapTypeId: google.maps.MapTypeId.ROADMAP
          };
          var map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
          
          for (var i=0; i<measurements.length; i++) {
              var measurement = measurements[i];
              addMarker(measurement, map);
          }
      }
      google.maps.event.addDomListener(window, "load", initialize);      
  </script>
</head>
<body>
  <div id="map-canvas"></div>
</body>
</html>