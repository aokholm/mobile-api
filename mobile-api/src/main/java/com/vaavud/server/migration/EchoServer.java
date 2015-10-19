package com.vaavud.server.migration;

import com.vaavud.server.migration.DeviceMigrator;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

/**
 * @ServerEndpoint gives the relative name for the end point This will be
 *                 accessed via ws://localhost:8080/EchoChamber/echo Where
 *                 "localhost" is the address of the host, "EchoChamber" is the
 *                 name of the package and "echo" is the address to access this
 *                 class from the server
 */
@ServerEndpoint("/echo")
public class EchoServer implements DeviceMigratorDelegate{

	private static final Logger logger = Logger.getLogger(EchoServer.class);
	private DeviceMigrator migrator = new DeviceMigrator(this);

	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	
	private static boolean running = false;
	private static long index = 0;
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		migrator.processDevice(53454);
		writeToClient("recived");
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
	
	public void writeToClient(String text) {
		synchronized (clients) {
 			// Iterate over the connected sessions
 			// and broadcast the received message
 			
 			for (Session client : clients) {
 				
 				try {
					client.getBasicRemote().sendText(text);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			}
 		}
	}


	@Override
	public void completed(long deviceID) {
		// TODO Auto-generated method stub
		String message = "Sucess" + String.valueOf(deviceID);
		logger.info(message);
		writeToClient(message);
	}
}



//		if (message.substring(0, 1).equals("p")) {
//String[] strings = message.substring(1).split(":");
//
//writeToClient(message.substring(1));
//writeToClient(String.valueOf(strings.length));
//
//if (strings.length == 2) {
////	processPoints(Long.valueOf(strings[0]), Long.valueOf(strings[1]));
//}
//else {
//	writeToClient("wrong format");
//}
//
//} else if (message.substring(0, 1).equals("s")) {
//
//} else {
////processDevice(message);
//}

//	public void processDevices() {
//		
//	}
//	
//	
//	public void processPoints(long idLower, long idUpper) {
//		org.hibernate.Session hibernateSession = Model.get().getSessionFactory().openSession();
//		try {
//			
//			@SuppressWarnings("unchecked")
//			List<MeasurementPoint> points = (List<MeasurementPoint>) hibernateSession.createQuery("from MeasurementPoint where id>=:id_lower and id<:id_upper")
//					.setLong("id_lower", idLower).setLong("id_upper", idUpper).list();
//			
////			patchPoints(points);
//			
//			logger.info("Found " + points.size() + " wind Points !");
//				
//		} catch (Exception e) {
//			logger.error("Error processing service " + getClass().getName(), e);
//			
//		} finally {
//			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
//				hibernateSession.getTransaction().rollback();
//			}
//			hibernateSession.close();
//		}
//	}
//	
//	public void processDevice(String id) {
//		org.hibernate.Session hibernateSession = Model.get().getSessionFactory().openSession();
//		
//		String deviceID = id;		
//		try {
//			
//			Device device = (Device) hibernateSession.createQuery("from Device where id=:id")
//					.setString("id", deviceID).uniqueResult();
//			
//			if (device != null) {
////				patchDevice(device, deviceID);
//			}
//			
//			User user = device.getUser();
////			if (user != null) {
////				patchUser(user, deviceID);
////			}
////			
//			
////			@SuppressWarnings("unchecked")
////			List<MeasurementSession> measurements = (List<MeasurementSession>) hibernateSession.createQuery(
////					"select s from MeasurementSession s where s.deleted=0 and s.device.id=:deviceId")
////					.setLong("deviceId", device.getId()).list();
////			
////			patchMeasurements(measurements, deviceID);
////			
////			patchPoints
//			
////			measurements = measurementsX;
//			
////			logger.info("Found " + measurements.size() + " measurements !");
//				
//		} catch (Exception e) {
//			logger.error("Error processing service " + getClass().getName(), e);
//			
//		} finally {
//			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
//				hibernateSession.getTransaction().rollback();
//			}
//			hibernateSession.close();
//		}
//	}
		
//	public void patchDevice(Device device, String deviceID) {
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(device.getDeviceKey(), device2json(device).toString());
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//		patch("devices.json", json.toString(), deviceID);
//	}
//	
//	public void patchUser(User user, String deviceID) {
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(user.getUserKey(), user2json(user).toString());
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//		patch("users.json", json.toString(), deviceID);
//	}
//	
//	public void patchMeasurements(List<MeasurementSession> measurements, String deviceID) {
//		Map<String, String> map = new HashMap<String, String>();
//		
//		for (MeasurementSession measurement : measurements) {
//			map.put(measurement.getSessionKey(), measurement2json(measurement));
//		}
//		
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//		patch("measurements.json", json.toString(), deviceID);
//	}
	
//	public void patchPoints(List<MeasurementPoint> points, String deviceID) {
//		Map<String, String> map = new HashMap<String, String>();
//		
//		for (MeasurementPoint point : points) {
//			map.put(point.getWindKey(), point2json(point));
//		}
//		
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//		patch("winds.json", json.toString(), deviceID);
//	}
//	
//	
//	public void patch(String uri, String jsonBody, String deviceID) {
//		Future<HttpResponse<JsonNode>> future = Unirest.patch("https://vaavud-migration.firebaseio.com/" + uri)
//		  .header("accept", "application/json")
//		  .body(jsonBody)
//		  .asJsonAsync(new MyCallback<JsonNode>(deviceID, uri));
//	}
//	
//	public class MyCallback<JsonNode> implements Callback<JsonNode> {
//		
//		String deviceID = "";
//		String uri = "";
//		
//		MyCallback(String deviceID, String uri) {
//			this.deviceID = deviceID;
//			this.uri = uri;
//		}
//		
//		public void failed(UnirestException e) {
//	        System.out.println("The request has failed");
//	    }
//
//	    public void completed(HttpResponse<JsonNode> response) {
//	         int code = response.getStatus();
//
//	         if (code == 200) {
//	        	 for (Session client : clients) {
//	 				try {
//	 					client.getBasicRemote().sendText("Sucess - deviceID:" + deviceID + " item: " + uri);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//	 			}
//	         } else {
//	        	 for (Session client : clients) {
//	 				try {
//	 					client.getBasicRemote().sendText("Sucess - deviceID:" + deviceID + " item: " + uri + "code: " + code);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//	 			}
//	         } 
//	    }
//
//	    public void cancelled() {
//	        System.out.println("The request has been cancelled");
//	    }
//	}
//	
//	
//	public void setCompletion(String uri, String jsonBody) {
//		Future<HttpResponse<JsonNode>> future = Unirest.patch("https://vaavud-migration.firebaseio.com/" + uri)
//		  .header("accept", "application/json")
//		  .body(jsonBody)
//		  .asJsonAsync(new Callback<JsonNode>() {
//
//			    public void failed(UnirestException e) {
//			        System.out.println("The request has failed");
//			    }
//
//			    public void completed(HttpResponse<JsonNode> response) {
//			         int code = response.getStatus();
////					         Map<String, List<String>> headers = response.getHeaders();
//			         JsonNode body = response.getBody();
//			         InputStream rawBody = response.getRawBody();
//			         
//			         StringWriter writer = new StringWriter();
//			         try {
//						IOUtils.copy(rawBody, writer, "UTF-8");
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//			         String theString = writer.toString();
//			         
//			         synchronized (clients) {
//			 			// Iterate over the connected sessions
//			 			// and broadcast the received message
//			 			
//			 			for (Session client : clients) {
//			 				
//			 				try {
//								client.getBasicRemote().sendText("Http response code: " + String.valueOf(code) + "for " + theString);
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//			 			}
//			 		}
//			         
//			    }
//
//			    public void cancelled() {
//			        System.out.println("The request has been cancelled");
//			    }
//
//			});
//	
//	}
//
//	public String point2json(MeasurementPoint point) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("time", point.getTime().getTime() );
//		map.put("speed", point.getWindSpeed() );
//		map.put("direction", point.getWindDirection() );
//		map.put("sessionKey", point.getSession().getSessionKey());
//
//		
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//	    return json.toString();
//	}
//}
//
//

//					         Map<String, List<String>> headers = response.getHeaders();

//
//JsonNode body = response.getBody();
//InputStream rawBody = response.getRawBody();
//
//StringWriter writer = new StringWriter();
//try {
//	IOUtils.copy(rawBody, writer, "UTF-8");
//} catch (IOException e1) {
//	// TODO Auto-generated catch block
//	e1.printStackTrace();
//}
//String theString = writer.toString();
//
//synchronized (clients) {
//	// Iterate over the connected sessions
//	// and broadcast the received message
//	
//	for (Session client : clients) {
//		
//		try {
//			client.getBasicRemote().sendText("Http response code: " + String.valueOf(code) + "for " + theString);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}