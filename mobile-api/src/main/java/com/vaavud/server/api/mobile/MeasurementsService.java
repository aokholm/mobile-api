package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.io.Serializable;
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
import com.vaavud.server.model.entity.Device;

public class MeasurementsService extends AbstractJSONService<MeasurementsService.RequestParameters> {

	private static final Logger logger = Logger.getLogger(MeasurementsService.class);
	
	private static final long TIME_LIMIT_MILLIS = 24L * 3600L * 1000L;
	
	public static class RequestParameters implements Serializable {
		private Date startTime;

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}

		@Override
		public String toString() {
			return "RequestParameters [startTime=" + startTime + "]";
		}
	}
			
	@Override
	protected Class<RequestParameters> type() {
		return RequestParameters.class;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, RequestParameters object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);

			Date startTime = (object == null) ? null : object.getStartTime();

			if (startTime == null || startTime.getTime() < (System.currentTimeMillis() - TIME_LIMIT_MILLIS)) {
				logger.warn("Start time is before history time limit, using 24 hours back instead");
				startTime = new Date(System.currentTimeMillis() - TIME_LIMIT_MILLIS);
			}

			List<Object[]> measurements = hibernateSession.createSQLQuery(
					"select latitude, longitude, startTime, windSpeedAvg, windSpeedMax " + 
					"from MeasurementSession " + 
					"where deleted=0 and startTime>:startTime and " + 
					"latitude is not null and longitude is not null and " +
					"windSpeedAvg is not null")
					.setLong("startTime", startTime.getTime()).list();
			
			writeJSONResponse(resp, mapper, measurements);
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
