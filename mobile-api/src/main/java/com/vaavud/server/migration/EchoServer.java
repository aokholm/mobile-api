package com.vaavud.server.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
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

		
		org.hibernate.Session hibernateSession = Model.get().getSessionFactory().openSession();
		
		String deviceID = message;
		Device device = null;
		User user = null;
		List<MeasurementSession> measurements = new ArrayList<MeasurementSession>();
		String deviceText = null;
		String userText = null;
		
		try {
			
			device = (Device) hibernateSession.createQuery("from Device where id=:id")
					.setString("id", deviceID).uniqueResult();
			
			user = device.getUser();
			
//			@SuppressWarnings("unchecked")
			measurements = (List<MeasurementSession>) hibernateSession.createQuery(
					"select s from MeasurementSession s where s.deleted=0 and s.device.id=:deviceId")
					.setLong("deviceId", device.getId()).list();
			
//			measurements = measurementsX;
			
			logger.info("Found " + measurements.size() + " measurements !");
			
			deviceText = device != null ? device2json(device) : "no device";
			userText = user != null ? user2json(user) : "no user";
			
			
		} catch (Exception e) {
			logger.error("Error processing service " + getClass().getName(), e);
			
		} finally {
			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
				hibernateSession.getTransaction().rollback();
			}
			hibernateSession.close();
		}
		
//		try {
//			HttpResponse<com.mashape.unirest.http.JsonNode> jsonResponse = Unirest.post("https://vaavud-migration.firebaseio.com/users/x.json")
//					  .header("accept", "application/json")
//					  .body("{\"parameter\":\"value\", \"foo\":\"bar\"}")
//					  .asJson();
//			
//			logger.info(jsonResponse.toString());
//			
//		} catch (UnirestException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		synchronized (clients) {
			// Iterate over the connected sessions
			// and broadcast the received message
			
			for (Session client : clients) {
				client.getBasicRemote().sendText("Device id: " + deviceText + " and user " + userText + " and number of measurements: "+ measurements.size() + " measurements !");
			}
		}

	}

	public String user2json(User user) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("created", String.valueOf(user.getCreationTime().getTime()) );
		map.put("email", user.getEmail());
		map.put("deleted", String.valueOf(user.isDeleted()));
				
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
	    return json.toString();
	}
	
	public String device2json(Device device) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("created", String.valueOf(device.getCreationTime().getTime()) );
		map.put("vendor", device.getVendor());
		map.put("model", converModelNames(device.getModel()));
		map.put("version", device.getAppVersion());
		
		if (device.getUser() != null) {
			map.put("userKey", device.getUser().getUserKey());
		}
		
		
		JSONObject json = new JSONObject();
	    json.putAll( map );
		
	    return json.toString();
//		map.put(key, value)
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
}