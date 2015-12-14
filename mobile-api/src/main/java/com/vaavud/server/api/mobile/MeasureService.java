package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.util.json.DeviceByUUIDModule;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.entity.WindMeter;
import com.vaavud.server.model.migration.FirebaseMigrator;
import net.jodah.expiringmap.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MeasureService extends AbstractJSONService<MeasurementSession> {

	private static final Logger logger = Logger.getLogger(MeasureService.class);
	// Set up a cache with expiration last access + 5 minutes. We'll use this as a heuristic
	// to determine when a session is done; if no updates for 5 minutes, we assume that its
	// done...
	private static final Map<String, MeasurementSession> activeSessions = ExpiringMap.builder()
	                   .expiration(30, TimeUnit.SECONDS)
	                   .expirationPolicy(ExpirationPolicy.ACCESSED)
                       .expirationListener(new ExpirationListener<String, MeasurementSession>() {
	                       public void expired(String key, MeasurementSession obj)
	                       {
	                    	   logger.warn("Expiration of " + key);
	                    	   FirebaseMigrator.markSessionComplete(key);
	                       }
	                   })
	                   .build();

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
			activeSessions.putIfAbsent(object.getUuid(), object);
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
			
			hibernateSession.getTransaction().commit();
			FirebaseMigrator.setSession(object);
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
				hibernateSession.getTransaction().commit();	
				FirebaseMigrator.setSession(storedMeasurementSession);
				for (MeasurementPoint point : object.getPoints()) {
					FirebaseMigrator.setPoint(point, storedMeasurementSession);
				}
			}
			else if (storedMeasurementSession.getEndIndex() == object.getEndIndex()) {

				if (object.hasAdditionalProperties()) {
					logger.info("Received MeasurementSession with only additional information");
					storedMeasurementSession.setFrom(object);
					
					hibernateSession.getTransaction().commit();
					FirebaseMigrator.setSession(storedMeasurementSession);
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
			hibernateSession.getTransaction().commit();
			FirebaseMigrator.setSession(object);
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
					hibernateSession.save(point);
				}
				
				hibernateSession.getTransaction().commit();
				FirebaseMigrator.setSession(storedMeasurementSession);
				for (MeasurementPoint point : object.getPoints()) {
					FirebaseMigrator.setPoint(point, storedMeasurementSession);
				}
				
			}
			else if (storedMeasurementSession.getPoints().size() == object.getPoints().size()) {
				if (object.hasAdditionalProperties()) {
					logger.info("Received MeasurementSession with only additional information");
					storedMeasurementSession.setFrom(object);
					hibernateSession.getTransaction().commit();
					FirebaseMigrator.setSession(storedMeasurementSession);
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

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
