package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.util.json.DeviceByUUIDModule;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.LatLng;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.entity.WindMeter;

public class MeasureService extends AbstractJSONService<MeasurementSession> {

	private static final Logger logger = Logger.getLogger(MeasureService.class);
	public static final String FIREBASE_BASE_URL = "https://vaavud-tomcat.firebaseio.com/";
	public static final String FIREBASE_SESSION = "session/";
	public static final String FIREBASE_GEO = "geo/";
	public static final String FIREBASE_WIND = "wind/"; 
	private Firebase firebaseSessionClient;
	private Firebase firebaseWindClient;
	private GeoFire geoFireSessionClient;
	private String firebaseSessionKey;

	@Override
	protected Class<MeasurementSession> type() {
		return MeasurementSession.class;
	}

	@Override
	protected ObjectMapper createMapper(Session hibernateSession) {
		ObjectMapper mapper = super.createMapper(hibernateSession);
		mapper.registerModule(new DeviceByUUIDModule(hibernateSession));
		return mapper;
	}

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, MeasurementSession object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {

		firebaseSessionClient = new Firebase(FIREBASE_BASE_URL+FIREBASE_SESSION);
		firebaseWindClient = new Firebase(FIREBASE_BASE_URL+FIREBASE_WIND);
		geoFireSessionClient = new GeoFire(new Firebase(FIREBASE_BASE_URL+FIREBASE_GEO+FIREBASE_SESSION));

		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);

			if (object.getId() != null) {
				logger.warn("Received MeasurementSession with ID, which should be a server-side only field. Setting it to null.");
				object.setId(null);
			}

			if (object.getUuid() == null || object.getUuid().isEmpty()) {
				logger.error("Received MeasurementSession with no UUID");
				throw new ProtocolException("Received MeasurementSession with no uuid");
			}

			if (object.getDevice() == null || object.getDevice().getUuid() == null || object.getDevice().getUuid().isEmpty()) {
				logger.error("Received MeasurementSession with no registered Device");
				throw new ProtocolException("Received MeasurementSession with no registered Device");
			}

			if (object.getWindMeter() == null) {
				object.setWindMeter(WindMeter.MJOLNIR);
			}

			if (object.getWindMeter() == WindMeter.MJOLNIR && object.getDevice().isAppVersionLessThan("1.2.4") ) {
				object.setWindDirection(null);
				for (MeasurementPoint point : object.getPoints()) {
					point.setWindDirection(null);
				}
			}

			if (object.getStartIndex() > 0 || object.getEndIndex() > 0) {
				processIncrementalMeasurementSession(hibernateSession, object);
			}
			else {
				processFullMeasurementSession(hibernateSession, object);
			}
			writeJSONResponse(resp, mapper);
		}
	}

	private void processIncrementalMeasurementSession(Session hibernateSession, MeasurementSession object) {
		hibernateSession.beginTransaction();

		MeasurementSession storedMeasurementSession = (MeasurementSession) hibernateSession
				.createQuery("from MeasurementSession where uuid=:uuid")
				.setString("uuid", object.getUuid())
				.uniqueResult();

		if (storedMeasurementSession == null) {
			setMeasurementSessionOnPoints(object.getPoints(), object);
			if (object.getStartIndex() != 0) {
				logger.warn("Received MeasurementSession that is not already stored but startIndex (" + object.getStartIndex() + ") is greater than 0");
			}
			if (object.getPoints().size() != (object.getEndIndex() - object.getStartIndex())) {
				logger.warn("Received MeasurementSession's endIndex-startIndex (" + (object.getEndIndex() - object.getStartIndex()) + ") doesn't match number of points (" + object.getPoints().size() + ")");
			}
			hibernateSession.save(object);
			updateFirebaseDataSession(object);
			hibernateSession.getTransaction().commit();
		}
		else {
			// we've already got this measurement session
			if (storedMeasurementSession.getEndIndex() < object.getEndIndex()) {
				logger.info("Received MeasurementSession already stored and new end index is greater so appending: storedEndIndex=" + storedMeasurementSession.getEndIndex() + ", receivedEndIndex=" + object.getEndIndex());

				if (storedMeasurementSession.getEndIndex() < object.getStartIndex()) {
					logger.warn("Apparent whole in measurement points, stored MeasurementSession's endIndex=" + storedMeasurementSession.getEndIndex() + " < received MeasurementSession's startIndex=" + object.getStartIndex());
				}
				else if (storedMeasurementSession.getEndIndex() > object.getStartIndex()) {
					logger.warn("Partly retransmission, stored MeasurementSession's endIndex=" + storedMeasurementSession.getEndIndex() + " > received MeasurementSession's startIndex=" + object.getStartIndex());
				}

				int num = object.getStartIndex();
				for (MeasurementPoint point : object.getPoints()) {
					if (num >= storedMeasurementSession.getEndIndex()) {
						if (point.getSession() != null) {
							logger.error("MeasurementPoint is already associated with a MeasurementSession");
						}
						else {
							point.setSession(storedMeasurementSession);
							hibernateSession.save(point);
						}
					}
					else {
						logger.warn("Skipping point already received with index=" + num + " < stored endIndex=" + storedMeasurementSession.getEndIndex());
					}
					num++;
				}

				// update stored measurement session summary from received object
				storedMeasurementSession.setFrom(object);

				if (num != object.getEndIndex()) {
					logger.warn("Received MeasurementSession's endIndex (" + object.getEndIndex() + ") doesn't match computed endIndex (" + num + ") - startIndex=" + object.getStartIndex() + ", point count=" + object.getPoints().size());
				}
				storedMeasurementSession.setEndIndex(num);
				updateFirebaseDataSession(object);
				hibernateSession.getTransaction().commit();					
			}
			else if (storedMeasurementSession.getEndIndex() == object.getEndIndex()) {

				if (object.hasAdditionalProperties()) {
					logger.info("Received MeasurementSession with only additional information");
					storedMeasurementSession.setFrom(object);
					updateFirebaseDataSession(object);
					hibernateSession.getTransaction().commit();					
				}
				else {
					logger.info("Received MeasurementSession already stored but endIndex is the same, so probably a duplicate transmission");
				}
			}
			else {
				logger.error("Received MeasurementSession already stored but stored endIndex is greater: stored=" + storedMeasurementSession.getEndIndex() + ", received=" + object.getEndIndex());
			}
		}
	}

	private void processFullMeasurementSession(Session hibernateSession, MeasurementSession object) {
		hibernateSession.beginTransaction();

		MeasurementSession storedMeasurementSession = (MeasurementSession) hibernateSession
				.createQuery("from MeasurementSession where uuid=:uuid")
				.setString("uuid", object.getUuid())
				.uniqueResult();

		if (storedMeasurementSession == null) {
			setMeasurementSessionOnPoints(object.getPoints(), object);
			hibernateSession.save(object);
			updateFirebaseDataSession(object);
			hibernateSession.getTransaction().commit();
		}
		else {
			// we've already got this measurement session
			if (storedMeasurementSession.getPoints().size() < object.getPoints().size()) {
				logger.info("Received MeasurementSession already stored but number new number of points is greater so replacing: stored=" + storedMeasurementSession.getPoints().size() + ", received=" + object.getPoints().size());

				// update stored measurement session summary from received object
				storedMeasurementSession.setFrom(object);

				// replace old points completely
				hibernateSession.createQuery("delete from MeasurementPoint p where p.session=:session")
				.setEntity("session", storedMeasurementSession)
				.executeUpdate();

				setMeasurementSessionOnPoints(object.getPoints(), storedMeasurementSession);

				for (MeasurementPoint point : object.getPoints()) {
					updateFirebaseDataPoint(point);
					hibernateSession.save(point);
				}
				updateFirebaseDataSession(object);
				hibernateSession.getTransaction().commit();					
			}
			else if (storedMeasurementSession.getPoints().size() == object.getPoints().size()) {
				if (object.hasAdditionalProperties()) {
					logger.info("Received MeasurementSession with only additional information");
					storedMeasurementSession.setFrom(object);
					updateFirebaseDataSession(object);
					hibernateSession.getTransaction().commit();					
				}
				else {
					logger.info("Received MeasurementSession already stored but number of points is the same, so probably a duplicate transmission");
				}
			}
			else {
				logger.error("Received MeasurementSession already stored but number of stored points is greater: stored=" + storedMeasurementSession.getPoints().size() + ", received=" + object.getPoints().size());
			}
		}
	}

	private void setMeasurementSessionOnPoints(Collection<MeasurementPoint> points, MeasurementSession measurementSession) {
		for (MeasurementPoint measurementPoint : points) {
			if (measurementPoint.getSession() != null) {
				logger.error("MeasurementPoint is already associated with a MeasurementSession");
			}
			else {
				measurementPoint.setSession(measurementSession);
			}
		}
	}


	private void updateFirebaseDataPoint(MeasurementPoint point) {
		Map<String, Object> data = new HashMap<String, Object>();
		if (firebaseSessionKey.length()>0) {
			data.put("sessionKey", firebaseSessionKey);
			data.put("time", point.getTime().getTime());
			data.put("speed",point.getWindSpeed());
			if (point.getWindDirection() != null) {
				data.put("direction", point.getWindSpeed());
			}
			firebaseWindClient.push().setValue(data);
		}
	}

	private void updateFirebaseDataSession(MeasurementSession session) {

		Map<String, Object> data = new HashMap<String, Object>();
		firebaseSessionKey = session.getUuid();
		//				Log.d(TAG, "FirebaseSessionKey: " + firebaseSessionKey);
		data.put("timeStart", session.getStartTime().getTime());
		data.put("deviceKey", session.getDevice().getUuid());
		data.put("timeStop", session.getEndTime().getTime());
		data.put("timeUploaded", new Date().getTime());
		if (session.getWindSpeedAvg()!=null && session.getWindSpeedMax()!=null) {
			data.put("windMean",session.getWindSpeedAvg());
			data.put("windMax", session.getWindSpeedMax());
		}
		if (session.getWindDirection() != null) {
			data.put("windDirection", session.getWindDirection());
		}
		if (session.getPosition() != null) {
			data.put("locLat", session.getPosition().getLatitude());
			data.put("locLon", session.getPosition().getLongitude());
			geoFireSessionClient.setLocation(firebaseSessionKey, new GeoLocation(session.getPosition().getLatitude(), session.getPosition().getLongitude()));
		}
		if (!firebaseSessionKey.isEmpty()){
			firebaseSessionClient.child(firebaseSessionKey).setValue(data);
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
