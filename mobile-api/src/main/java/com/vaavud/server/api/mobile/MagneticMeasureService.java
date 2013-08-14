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
import com.vaavud.server.model.entity.MagneticPoint;
import com.vaavud.server.model.entity.MagneticSession;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;

public class MagneticMeasureService extends AbstractJSONService<MagneticSession> {

	private static final Logger logger = Logger.getLogger(MagneticMeasureService.class);
			
	@Override
	protected Class<MagneticSession> type() {
		return MagneticSession.class;
	}
	
	@Override
	protected ObjectMapper createMapper(Session hibernateSession) {
		ObjectMapper mapper = super.createMapper(hibernateSession);
		mapper.registerModule(new DeviceByUUIDModule(hibernateSession));
		return mapper;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, MagneticSession object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);

			if (object.getId() != null) {
				logger.warn("Received MagneticSession with ID, which should be a server-side only field. Setting it to null.");
				object.setId(null);
			}
			
			if (object.getMeasurementSessionUuid() == null || object.getMeasurementSessionUuid().isEmpty()) {
				logger.error("Received MagneticSession with no MeasurementSession UUID");
				throw new ProtocolException("Received MagneticSession with no uuid");
			}
			
			hibernateSession.beginTransaction();

			MagneticSession storedMagneticSession = (MagneticSession) hibernateSession
					.createQuery("from MagneticSession where measurementSessionUuid=:uuid")
					.setString("uuid", object.getMeasurementSessionUuid())
					.uniqueResult();

			if (storedMagneticSession == null) {
				int num = object.appendToMagneticPoints();
				
				if (num != object.getEndIndex()) {
					logger.warn("Received MagneticSession's endIndex (" + object.getEndIndex() + ") doesn't match computed endIndex (" + num + ") - startIndex=" + object.getStartIndex() + ", point count=" + object.getPoints().size());
				}
				object.setEndIndex(num);

				hibernateSession.save(object);
				hibernateSession.getTransaction().commit();
			}
			else {
				// we've already got this magnetic session
				if (storedMagneticSession.getEndIndex() < object.getEndIndex()) {
					logger.info("Received MagneticSession already stored but new end index is greater so replacing: stored=" + storedMagneticSession.getEndIndex() + ", received=" + object.getEndIndex());
					
					int num = object.getStartIndex();
					for (Float[] point : object.getPoints()) {
						if (num >= storedMagneticSession.getEndIndex()) {
							MagneticPoint magneticPoint = new MagneticPoint(storedMagneticSession, num, point);
							hibernateSession.save(magneticPoint);
						}
						num++;
					}
					
					if (num != object.getEndIndex()) {
						logger.warn("Received MagneticSession's endIndex (" + object.getEndIndex() + ") doesn't match computed endIndex (" + num + ") - startIndex=" + object.getStartIndex() + ", point count=" + object.getPoints().size());
					}
					storedMagneticSession.setEndIndex(num);

					hibernateSession.getTransaction().commit();					
				}
				else if (storedMagneticSession.getEndIndex() == object.getEndIndex()) {
					logger.info("Received MagneticSession already stored but endIndex is the same, so probably a duplicate transmission");
				}
				else {
					logger.error("Received MagneticSession already stored but stored endIndex is greater: stored=" + storedMagneticSession.getEndIndex() + ", received=" + object.getEndIndex());
				}
			}

		}
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
}
