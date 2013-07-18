package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.util.json.DeviceByUUIDModule;
import com.vaavud.server.model.entity.LatLng;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;

public class MeasureService extends AbstractJSONService<MeasurementSession> {

	private static final Logger logger = Logger.getLogger(MeasureService.class);
			
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
	protected void process(HttpServletRequest req, HttpServletResponse resp, MeasurementSession object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
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
			
			hibernateSession.beginTransaction();

			MeasurementSession storedMeasurementSession = (MeasurementSession) hibernateSession
					.createQuery("from MeasurementSession where uuid=:uuid")
					.setString("uuid", object.getUuid())
					.uniqueResult();

			if (storedMeasurementSession == null) {
				setMeasurementSessionOnPoints(object.getPoints(), object);
				hibernateSession.save(object);
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
						hibernateSession.save(point);
					}

					hibernateSession.getTransaction().commit();					
				}
				else if (storedMeasurementSession.getPoints().size() == object.getPoints().size()) {
					logger.info("Received MeasurementSession already stored but number of points is the same, so probably a duplicate transmission");
				}
				else {
					logger.error("Received MeasurementSession already stored but number of stored points is greater: stored=" + storedMeasurementSession.getPoints().size() + ", received=" + object.getPoints().size());
				}
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
