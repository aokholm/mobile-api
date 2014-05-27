package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.IdEntity;
import com.vaavud.server.model.entity.LatLng;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.util.UUIDUtil;

public class HistoryService extends AbstractJSONService<HistoryService.RequestParameters> {

	private static final Logger logger = Logger.getLogger(HistoryService.class);
	
	public static class RequestParameters implements Serializable {
		
		private Date latestEndTime;
		private String hash;
		
		public Date getLatestEndTime() {
			return latestEndTime;
		}
		
		public void setLatestEndTime(Date latestEndTime) {
			this.latestEndTime = latestEndTime;
		}
		
		public String getHash() {
			return hash;
		}
		
		public void setHash(String hash) {
			this.hash = hash;
		}
		
		@Override
		public String toString() {
			return "RequestParameters [latestEndTime=" + latestEndTime
					+ ", hash=" + hash + "]";
		}
	}
	
	public static class ResponseSessionObject implements Serializable {

		private String uuid;
	    private Date startTime;
	    private Date endTime;
	    private Double latitude;
	    private Double longitude;
	    private Float windSpeedAvg;
	    private Float windSpeedMax;
	    private ResponsePointObject[] points;
	    
	    public ResponseSessionObject() {
	    }
	    
	    public ResponseSessionObject(MeasurementSession measurementSession) {
	    	this.uuid = measurementSession.getUuid();
	    	this.startTime = measurementSession.getStartTime();
	    	this.endTime = measurementSession.getEndTime();
	    	if (measurementSession.getPosition() != null &&
	    			measurementSession.getPosition().getLatitude() != null && measurementSession.getPosition().getLongitude() != null &&
	    			measurementSession.getPosition().getLatitude() != 0D && measurementSession.getPosition().getLongitude() != 0D) {
	    		this.latitude = measurementSession.getPosition().getLatitude();
	    		this.longitude = measurementSession.getPosition().getLongitude();
	    	}
	    	this.windSpeedAvg = measurementSession.getWindSpeedAvg();
	    	this.windSpeedMax = measurementSession.getWindSpeedMax();
	    	
	    	List<MeasurementPoint> originalPoints = measurementSession.getPoints();
	    	List<ResponsePointObject> points = new ArrayList<ResponsePointObject>(originalPoints.size());
	    	
	    	if (originalPoints.size() > 1000) {
	    		logger.warn("History service requesting session with more than 1000 points, skipping");
	    	}
	    	else {
		    	for (MeasurementPoint point : originalPoints) {
		    		if (point.getTime() != null && point.getWindSpeed() != null && point.getWindSpeed() >= 0.0) {
		    			points.add(new ResponsePointObject(point));
		    		}
		    	}
	    	}
	    	this.points = points.toArray(new ResponsePointObject[points.size()]);
	    }
	    
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public Date getStartTime() {
			return startTime;
		}
		
		public void setStartTime(Date startTime) {
			this.startTime = startTime;
		}
		
		public Date getEndTime() {
			return endTime;
		}

		public void setEndTime(Date endTime) {
			this.endTime = endTime;
		}

		public Double getLatitude() {
			return latitude;
		}
		
		public void setLatitude(Double latitude) {
			this.latitude = latitude;
		}
		
		public Double getLongitude() {
			return longitude;
		}
		
		public void setLongitude(Double longitude) {
			this.longitude = longitude;
		}
		
		public Float getWindSpeedAvg() {
			return windSpeedAvg;
		}
		
		public void setWindSpeedAvg(Float windSpeedAvg) {
			this.windSpeedAvg = windSpeedAvg;
		}
		
		public Float getWindSpeedMax() {
			return windSpeedMax;
		}
		
		public void setWindSpeedMax(Float windSpeedMax) {
			this.windSpeedMax = windSpeedMax;
		}

		public ResponsePointObject[] getPoints() {
			return points;
		}

		public void setPoints(ResponsePointObject[] points) {
			this.points = points;
		}
	}
			
	public static class ResponsePointObject implements Serializable {
	
		private Date time;
		private Float speed;
		
		public ResponsePointObject(Date time, Float speed) {
			this.time = time;
			this.speed = speed;
		}

		public ResponsePointObject(MeasurementPoint point) {
			this.time = point.getTime();
			this.speed = point.getWindSpeed();
		}

		public Date getTime() {
			return time;
		}
		
		public void setTime(Date time) {
			this.time = time;
		}
		
		public Float getSpeed() {
			return speed;
		}
		
		public void setSpeed(Float speed) {
			this.speed = speed;
		}
	}
	
	@Override
	protected Class<RequestParameters> type() {
		return RequestParameters.class;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, RequestParameters object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		logger.info("Process object " + object);

		if (authenticatedDevice.getUser() == null) {
			logger.error("User must be logged in");
			throw new UnauthorizedException();
		}
		
		logger.info("User " + authenticatedDevice.getUser().getEmail() + " and ID " + authenticatedDevice.getUser().getId());
		
		Date returnMeasurementsFrom = new Date(0L);
		
		if (object != null && object.getLatestEndTime() != null && object.getHash() != null) {
			@SuppressWarnings("unchecked")
			List<Object[]> uuids = (List<Object[]>) hibernateSession.createQuery(
					"select s.uuid, s.endTime from MeasurementSession s where s.deleted=0 and s.device.user.id=:userId order by s.endTime")
					.setLong("userId", authenticatedDevice.getUser().getId()).list();
			
			StringBuilder sb1 = new StringBuilder(uuids.size() * (36 + 10));
			String nextUuid = null;
			for (int i = 0; i < uuids.size(); i++) {
				Object[] values = uuids.get(i);
				Date endTime = (Date) values[1];
				
				if (endTime.getTime() <= object.getLatestEndTime().getTime()) {
					String endTimeSecondsString = Long.toString((long) Math.ceil(((double) endTime.getTime()) / 1000D));
					sb1.append(values[0]);
					sb1.append(endTimeSecondsString);
				}
				else {
					nextUuid = (String) values[0];
					break;
				}
			}
			String serverHashedUUIDs = UUIDUtil.md5Hash(sb1.toString().toUpperCase(Locale.US));
			logger.info("Server hash: " + serverHashedUUIDs + " until " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(object.getLatestEndTime())) + " (" + object.getLatestEndTime().getTime() + "), client hash: " + object.getHash());
			
			if (serverHashedUUIDs.equals(object.getHash())) {
				returnMeasurementsFrom = object.getLatestEndTime();
			}
			else if (nextUuid != null) {
				// mismatch might be due to the latest session still in progress, so test if this is the case
				String endTimeSecondsString = Long.toString((long) Math.ceil(((double) object.getLatestEndTime().getTime()) / 1000D));
				sb1.append(nextUuid);
				sb1.append(endTimeSecondsString);
				serverHashedUUIDs = UUIDUtil.md5Hash(sb1.toString().toUpperCase(Locale.US));
				logger.info("Modified server hash: " + serverHashedUUIDs + " until " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(object.getLatestEndTime())) + " (" + object.getLatestEndTime().getTime() + "), client hash: " + object.getHash());
				if (serverHashedUUIDs.equals(object.getHash())) {
					returnMeasurementsFrom = object.getLatestEndTime();
				}
			}
		}

		@SuppressWarnings("unchecked")
		List<MeasurementSession> measurements = (List<MeasurementSession>) hibernateSession.createQuery(
				"select s from MeasurementSession s where s.deleted=0 and s.device.user.id=:userId and s.endTime>:endTime")
				.setLong("userId", authenticatedDevice.getUser().getId())
				.setLong("endTime", returnMeasurementsFrom.getTime()).list();
		
		logger.info("Found " + measurements.size() + " measurements");
		
		List<ResponseSessionObject> responseList = new ArrayList<ResponseSessionObject>(measurements.size());
		for (MeasurementSession measurementSession : measurements) {
			responseList.add(new ResponseSessionObject(measurementSession));
		}
		
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put("fromEndTime", returnMeasurementsFrom);
		responseMap.put("measurements", responseList);
		
		writeJSONResponse(resp, mapper, responseMap);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
