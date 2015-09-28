package com.vaavud.server.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.entity.User;

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
		
		try {
			
			device = (Device) hibernateSession.createQuery("from Device where id=:id")
					.setString("id", deviceID).uniqueResult();
			
			user = device.getUser();
			
//			@SuppressWarnings("unchecked")
			measurements = (List<MeasurementSession>) hibernateSession.createQuery(
					"select s from MeasurementSession s where s.deleted=0 and s.device.id=:deviceId")
					.setLong("deviceId", device.getId()).list();
			
//			measurements = measurementsX;
			
			
		} catch (Exception e) {
			logger.error("Error processing service " + getClass().getName(), e);
			
		} finally {
			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
				hibernateSession.getTransaction().rollback();
			}
			hibernateSession.close();
		}
		
		logger.info("Found " + measurements.size() + " measurements !");
		
		String deviceText = device != null ? FireBasePushIdGenerator.generatePushId(device.getCreationTime(), device.getId()) : "no device";
		
		
		synchronized (clients) {
			// Iterate over the connected sessions
			// and broadcast the received message
			
			for (Session client : clients) {
				client.getBasicRemote().sendText("Device id: " + deviceText + " and number of measurements: "+ measurements.size() + " measurements !");
			}
		}

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