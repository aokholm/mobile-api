package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.migration.FirebaseMigrator;

public class DeleteMeasurementService extends AbstractJSONService<DeleteMeasurementService.RequestParameters> {

	private static final Logger logger = Logger.getLogger(DeleteMeasurementService.class);
			
	public static class RequestParameters implements Serializable {
		private String uuid;

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		@Override
		public String toString() {
			return "RequestParameters [uuid=" + uuid + "]";
		}
	}

	@Override
	protected Class<DeleteMeasurementService.RequestParameters> type() {
		return RequestParameters.class;
	}
		
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, RequestParameters object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);
			
			if (object.getUuid() == null || object.getUuid().isEmpty()) {
				logger.error("Received no UUID");
				throw new ProtocolException("Received no uuid");
			}

			if (authenticatedDevice.getUser() == null) {
				logger.error("Authenticated device with no user, i.e. the user cannot be logged in");
				throw new UnauthorizedException();
			}

			hibernateSession.beginTransaction();

			MeasurementSession measurementSession = (MeasurementSession) hibernateSession
					.createQuery("from MeasurementSession where uuid=:uuid")
					.setString("uuid", object.getUuid())
					.uniqueResult();

			if (measurementSession == null) {
				logger.error("Measurement session with uuid " + object.getUuid() + " not found");
				return;
			}
			else {
				measurementSession.setDeleted(true);
			}
			hibernateSession.getTransaction().commit();					
			
			FirebaseMigrator.deleteSession(measurementSession);
			
			writeJSONResponse(resp, mapper);
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
