package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.util.json.DeviceByUUIDModule;
import com.vaavud.server.api.util.json.DirectLatLngModule;
import com.vaavud.server.model.entity.LatLng;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;

public class MeasurementsService extends AbstractJSONService<MeasurementsService.RequestParameters> {

	private static final Logger logger = Logger.getLogger(MeasurementsService.class);
	
	private static final long TIME_LIMIT_MILLIS = 72L * 3600L * 1000L;
	
	public static class RequestParameters implements Serializable {
		private Date startTime;

		public Date getStartTime() {
			return startTime;
		}

		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}
	}
			
	@Override
	protected Class<RequestParameters> type() {
		return RequestParameters.class;
	}
	
	// TODO: enable
	/*
	@Override
	protected boolean requiresAuthentication() {
		return false;
	}
	*/
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, RequestParameters object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		// TODO: enable
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);

			Date startTime = (object == null) ? null : object.getStartTime();

			if (startTime == null) {
				logger.warn("Received request parameters without startTime");
				// TODO: temporary!
				startTime = new Date(System.currentTimeMillis() - 365L * 24L * 3600L * 1000L);
				//throw new ProtocolException("No startTime in request parameters");
			}
			
			// TODO: enable
			
			if (startTime.getTime() < (System.currentTimeMillis() - TIME_LIMIT_MILLIS)) {
				logger.warn("Start time is before history time limit, using 72 hours back instead");
				startTime = new Date(System.currentTimeMillis() - TIME_LIMIT_MILLIS);
			}
			

			List<Object[]> measurements = hibernateSession.createSQLQuery("select latitude, longitude, startTime, windSpeedAvg, windSpeedMax from MeasurementSession where startTime>:startTime and latitude is not null and longitude is not null and windSpeedAvg is not null").setLong("startTime", startTime.getTime()).list();
			
			writeJSONResponse(resp, mapper, measurements);
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
