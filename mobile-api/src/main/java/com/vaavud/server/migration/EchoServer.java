package com.vaavud.server.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.apache.maven.surefire.shade.org.apache.commons.io.IOUtils;

//import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.entity.User;

import net.sf.json.JSONObject;

/**
 * @ServerEndpoint gives the relative name for the end point This will be
 *                 accessed via ws://localhost:8080/EchoChamber/echo Where
 *                 "localhost" is the address of the host, "EchoChamber" is the
 *                 name of the package and "echo" is the address to access this
 *                 class from the server
 */
@ServerEndpoint("/echo")
public class EchoServer {

	private static final Logger logger = Logger.getLogger(EchoServer.class);

	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		processDevice(message);
	}
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		// Add session to the connected sessions set
		clients.add(session);
		session.getBasicRemote().sendText("Welcome");
//		session.getBasicRemote().sendText(getDevice("5"));
	}

	@OnClose
	public void onClose(Session session) {
		// Remove session from the connected sessions set
		clients.remove(session);
	}
	
	public void processDevice(String id) {
		org.hibernate.Session hibernateSession = Model.get().getSessionFactory().openSession();
		
		String deviceID = id;		
		try {
			
			Device device = (Device) hibernateSession.createQuery("from Device where id=:id")
					.setString("id", deviceID).uniqueResult();
			
			if (device != null) {
				patchDevice(device);
			}
			
			
			User user = device.getUser();
			if (user != null) {
				patchUser(user);
			}
			
			
//			@SuppressWarnings("unchecked")
			List<MeasurementSession> measurements = (List<MeasurementSession>) hibernateSession.createQuery(
					"select s from MeasurementSession s where s.deleted=0 and s.device.id=:deviceId")
					.setLong("deviceId", device.getId()).list();
			
			patchMeasurements(measurements);
			
//			patchPoints
			
//			measurements = measurementsX;
			
			logger.info("Found " + measurements.size() + " measurements !");
				
		} catch (Exception e) {
			logger.error("Error processing service " + getClass().getName(), e);
			
		} finally {
			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
				hibernateSession.getTransaction().rollback();
			}
			hibernateSession.close();
		}
	}
		
	public void patchDevice(Device device) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(device.getDeviceKey(), device2json(device).toString());
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
		patch("devices.json", json.toString());
	}
	
	public void patchUser(User user) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(user.getUserKey(), user2json(user).toString());
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
		patch("users.json", json.toString());
	}
	
	public void patchMeasurements(List<MeasurementSession> measurements) {
		Map<String, String> map = new HashMap<String, String>();
		
		for (MeasurementSession measurement : measurements) {
			map.put(measurement.getSessionKey(), measurement2json(measurement));
		}
		
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
		patch("measurements.json", json.toString());
	}
	
	
	public void patch(String uri, String jsonBody) {
		Future<HttpResponse<JsonNode>> future = Unirest.patch("https://vaavud-migration.firebaseio.com/" + uri)
				  .header("accept", "application/json")
				  .body(jsonBody)
				  .asJsonAsync(new Callback<JsonNode>() {

					    public void failed(UnirestException e) {
					        System.out.println("The request has failed");
					    }

					    public void completed(HttpResponse<JsonNode> response) {
					         int code = response.getStatus();
//					         Map<String, List<String>> headers = response.getHeaders();
					         JsonNode body = response.getBody();
					         InputStream rawBody = response.getRawBody();
					         
					         StringWriter writer = new StringWriter();
					         try {
								IOUtils.copy(rawBody, writer, "UTF-8");
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					         String theString = writer.toString();
					         
					         synchronized (clients) {
					 			// Iterate over the connected sessions
					 			// and broadcast the received message
					 			
					 			for (Session client : clients) {
					 				
					 				try {
										client.getBasicRemote().sendText("Http response code: " + String.valueOf(code) + "for " + theString);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
					 			}
					 		}
					         
					    }

					    public void cancelled() {
					        System.out.println("The request has been cancelled");
					    }

					});
			
	}
	

	public String user2json(User user) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("created", user.getCreationTime().getTime() );
		map.put("email", user.getEmail());
		map.put("deleted", user.isDeleted());
				
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
	    return json.toString();
	}
	
	public String device2json(Device device) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("created", device.getCreationTime().getTime() );
		map.put("vendor", device.getVendor());
		map.put("model", converModelNames(device.getModel()));
		map.put("version", device.getAppVersion());
		
		if (device.getUser() != null) {
			map.put("userKey", device.getUser().getUserKey());
		}
	
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
	    return json.toString();
	}
	
	public String measurement2json(MeasurementSession measurement) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("timeStart", measurement.getStartTime().getTime() );
		map.put("windMean", measurement.getWindSpeedAvg() );

		
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
	    return json.toString();
	}
	
	public String converModelNames(String model) {
		if (model.equals("Simulator")) {return "i386";}
		if (model.equals("iPhone2G")) {return "iPhone1,1";}
		if (model.equals("iPhone3G")) {return "iPhone1,2";}
		if (model.equals("iPhone3GS")) {return "iPhone2,1";}
		if (model.equals("iPhone4GSM")) {return "iPhone3,1";}
		if (model.equals("iPhone4GSMRevA")) {return "iPhone3,2";}
		if (model.equals("iPhone4GSM+CDMA")) {return "iPhone3,3";}
		if (model.equals("iPhone4S")) {return "iPhone4,1";}
		if (model.equals("iPhone5GSM")) {return "iPhone5,1";}
		if (model.equals("iPhone5GSM+CDMA")) {return "iPhone5,2";}
		if (model.equals("iPod1stGen")) {return "iPod1,1";}
		if (model.equals("iPod2ndGen")) {return "iPod2,1";}
		if (model.equals("iPod3rdGen")) {return "iPod3,1";}
		if (model.equals("iPod4thGen")) {return "iPod4,1";}
		if (model.equals("iPod5thGen")) {return "iPod5,1";}
		if (model.equals("iPadWiFi")) {return "iPad1,1";}
		if (model.equals("iPad3G")) {return "iPad1,2";}
		if (model.equals("iPad2WiFi")) {return "iPad2,1";}
		if (model.equals("iPad2GSM")) {return "iPad2,2";}
		if (model.equals("iPad2CDMA")) {return "iPad2,3";}
		if (model.equals("iPad2WiFiRevA")) {return "iPad2,4";}
		if (model.equals("ipad3WiFi")) {return "iPad3,1";}
		if (model.equals("ipad3GSM")) {return "iPad3,2";}
		if (model.equals("ipad3CDMA")) {return "iPad3,3";}
		if (model.equals("iPad4WiFi")) {return "iPad3,4";}
		if (model.equals("iPad4GSM")) {return "iPad3,5";}
		if (model.equals("iPad4GSM+CDMA")) {return "iPad3,6";}
		if (model.equals("iPadMini1GWiFi")) {return "iPad2,5";}
		if (model.equals("iPadMini1GGSM")) {return "iPad2,6";}
		if (model.equals("iPadMini1GGSM+CDMA")) {return "iPad2,7";}
		
		return model;
	}
}