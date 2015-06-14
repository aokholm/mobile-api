package com.vaavud.server.api.external;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.entity.WindMeter;

public class MeasurementsExternalService extends AbstractJSONService<MeasurementsExternalService.RequestParameters> {

	private static final Logger logger = Logger.getLogger(MeasurementsExternalService.class);
    private static final int MAX_NUMBER_OF_RETURNED_POINTS = 1000;
	   
	   
	private static final HashMap<String,String> AUTH_TOKENS;
	static
    {
	    AUTH_TOKENS = new HashMap<String, String>();
	    AUTH_TOKENS.put("abcd", "vaavud.com");
	    AUTH_TOKENS.put("123", "gmail.com");
    }
	
	public static class RequestParameters implements Serializable {
		private Date startTime;
		private Date endTime;
		private String userEmail;

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

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        @Override
        public String toString() {
            return "RequestParameters [startTime=" + startTime + ", endTime=" + endTime + ", userEmail=" + userEmail
                    + "]";
        }
	}
	
	public static class ResponseSessionObject implements Serializable {

        private String uuid;
        private Integer windMeter;
        private Date startTime;
        private Date endTime;
        private Double latitude;
        private Double longitude;
        private Float windSpeedAvg;
        private Float windSpeedMax;
        private Float windDirection;
        private Float temperature;
        private Float humidity;
        private Integer pressure;
        private Float gustiness;
        private Float windChill;        
        
        private Float sourcedTemperature;
        private Float sourcedHumidity;
        private Integer sourcedPressureGroundLevel;
        private Float sourcedWindSpeedAvg;
        private Float sourcedWindSpeedMax;
        private Float sourcedWindDirection;

        private String geoLocationNameLocalized;        
        
        private ResponsePointObject[] points;
        
        public ResponseSessionObject() {
        }
        
        @SuppressWarnings("unchecked")
        public ResponseSessionObject(MeasurementSession measurementSession, Session hibernateSession) {
            this.uuid = measurementSession.getUuid();
//            this.deviceUuid = measurementSession.getDevice().getUuid();
            this.windMeter = (measurementSession.getWindMeter() != null) ? measurementSession.getWindMeter().ordinal() : 1;
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
            this.windDirection = (measurementSession.getWindMeter() == WindMeter.MJOLNIR) ? null : measurementSession.getWindDirection();
            this.temperature = (measurementSession.getTemperature() == null || measurementSession.getTemperature() == 0) ? null : measurementSession.getTemperature();
            this.humidity = measurementSession.getHumidity();
            this.pressure = measurementSession.getPressure();
            this.gustiness = measurementSession.getGustiness();
            this.windChill = measurementSession.getWindChill();
            
            this.geoLocationNameLocalized = measurementSession.getGeoLocationNameLocalized();
            
            this.sourcedTemperature = measurementSession.getSourcedTemperature();
            this.sourcedHumidity = measurementSession.getSourcedHumidity();
            this.sourcedPressureGroundLevel = measurementSession.getSourcedPressureGroundLevel();
            this.sourcedWindSpeedAvg = measurementSession.getSourcedWindSpeedAvg();
            this.sourcedWindSpeedMax = measurementSession.getSourcedWindSpeedMax();
            this.sourcedWindDirection = measurementSession.getSourcedWindDirection();
            
//            this.reduceEquipment = (measurementSession.getReduceEquipment() == null || measurementSession.getReduceEquipment() == ReduceEquipment.NOT_APPLICABLE) ? null : measurementSession.getReduceEquipment().ordinal();
//            this.dose = (measurementSession.getDose() == null || measurementSession.getDose() == 0) ? null : measurementSession.getDose();
//            this.boomHeight = (measurementSession.getBoomHeight() == null || measurementSession.getBoomHeight() == 0) ? null : measurementSession.getBoomHeight();
//            this.sprayQuality = (measurementSession.getSprayQuality() == null || measurementSession.getSprayQuality() == SprayQuality.NOT_APPLICABLE) ? null : measurementSession.getSprayQuality().ordinal();
//            this.generalConsideration = (measurementSession.getGeneralConsideration() == null || measurementSession.getGeneralConsideration() == 0) ? null : measurementSession.getGeneralConsideration();
//            this.specialConsideration = (measurementSession.getSpecialConsideration() == null || measurementSession.getSpecialConsideration() == 0) ? null : measurementSession.getSpecialConsideration();
//            this.testMode = measurementSession.getTestMode() == null ? null: measurementSession.getTestMode();
            
            List<MeasurementPoint> originalPoints;
            Number numberOfPoints = (Number) hibernateSession.createSQLQuery("select count(*) from MeasurementPoint p where p.session_id=:sessionId").setLong("sessionId", measurementSession.getId()).uniqueResult();
            if (numberOfPoints.intValue() > MAX_NUMBER_OF_RETURNED_POINTS) {
                SQLQuery query = hibernateSession.createSQLQuery("select * from MeasurementPoint where session_id=:sessionId and id mod :modulo = 0 order by id");
                query.setLong("sessionId", measurementSession.getId());
                query.setLong("modulo", (long) Math.ceil(numberOfPoints.doubleValue() / (double) MAX_NUMBER_OF_RETURNED_POINTS));
                query.addEntity(MeasurementPoint.class);
                originalPoints = query.list();
                logger.warn("History service requesting session with more than " + MAX_NUMBER_OF_RETURNED_POINTS + " points (" + numberOfPoints + "), returning " + originalPoints.size() + " instead");
            }
            else {
                originalPoints = measurementSession.getPoints();
            }

            List<ResponsePointObject> points = new ArrayList<ResponsePointObject>(originalPoints.size());
                
            for (MeasurementPoint point : originalPoints) {
                if (point.getTime() != null && point.getWindSpeed() != null && point.getWindSpeed() >= 0.0) {
                    points.add(new ResponsePointObject(point));
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
        
//        public String getDeviceUuid() {
//            return deviceUuid;
//        }
//
//        public void setDeviceUuid(String deviceUuid) {
//            this.deviceUuid = deviceUuid;
//        }

        public Integer getWindMeter() {
            return windMeter;
        }

        public void setWindMeter(Integer windMeter) {
            this.windMeter = windMeter;
        }

        public Float getTemperature() {
            return temperature;
        }

        public void setTemperature(Float temperature) {
            this.temperature = temperature;
        }
        
        public Float getHumidity() {
            return humidity;
        }

        public void setHumidity(Float humidity) {
            this.humidity = humidity;
        }

        public Integer getPressure() {
            return pressure;
        }

        public void setPressure(Integer pressure) {
            this.pressure = pressure;
        }

        public Float getGustiness() {
            return gustiness;
        }

        public void setGustiness(Float gustiness) {
            this.gustiness = gustiness;
        }

        public Float getWindChill() {
            return windChill;
        }

        public void setWindChill(Float windChill) {
            this.windChill = windChill;
        }

        public Float getSourcedTemperature() {
            return sourcedTemperature;
        }

        public void setSourcedTemperature(Float sourcedTemperature) {
            this.sourcedTemperature = sourcedTemperature;
        }

        public Float getSourcedHumidity() {
            return sourcedHumidity;
        }

        public void setSourcedHumidity(Float sourcedHumidity) {
            this.sourcedHumidity = sourcedHumidity;
        }

        public Integer getSourcedPressureGroundLevel() {
            return sourcedPressureGroundLevel;
        }

        public void setSourcedPressureGroundLevel(Integer sourcedPressureGroundLevel) {
            this.sourcedPressureGroundLevel = sourcedPressureGroundLevel;
        }

        public Float getSourcedWindSpeedAvg() {
            return sourcedWindSpeedAvg;
        }

        public void setSourcedWindSpeedAvg(Float sourcedWindSpeedAvg) {
            this.sourcedWindSpeedAvg = sourcedWindSpeedAvg;
        }

        public Float getSourcedWindSpeedMax() {
            return sourcedWindSpeedMax;
        }

        public void setSourcedWindSpeedMax(Float sourcedWindSpeedMax) {
            this.sourcedWindSpeedMax = sourcedWindSpeedMax;
        }

        public Float getSourcedWindDirection() {
            return sourcedWindDirection;
        }

        public void setSourcedWindDirection(Float sourcedWindDirection) {
            this.sourcedWindDirection = sourcedWindDirection;
        }

        public String getGeoLocationNameLocalized() {
            return geoLocationNameLocalized;
        }

        public void setGeoLocationNameLocalized(String geoLocationNameLocalized) {
            this.geoLocationNameLocalized = geoLocationNameLocalized;
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
        
        public Float getWindDirection() {
            return windDirection;
        }

        public void setWindDirection(Float windDirection) {
            this.windDirection = windDirection;
        }
        
//        public Integer getReduceEquipment() {
//            return reduceEquipment;
//        }
//
//        public void setReduceEquipment(Integer reduceEquipment) {
//            this.reduceEquipment = reduceEquipment;
//        }
//
//        public Float getDose() {
//            return dose;
//        }
//
//        public void setDose(Float dose) {
//            this.dose = dose;
//        }
//
//        public Integer getBoomHeight() {
//            return boomHeight;
//        }
//
//        public void setBoomHeight(Integer boomHeight) {
//            this.boomHeight = boomHeight;
//        }
//
//        public Integer getSprayQuality() {
//            return sprayQuality;
//        }
//
//        public void setSprayQuality(Integer sprayQuality) {
//            this.sprayQuality = sprayQuality;
//        }
//
//        public Integer getGeneralConsideration() {
//            return generalConsideration;
//        }
//
//        public void setGeneralConsideration(Integer generalConsideration) {
//            this.generalConsideration = generalConsideration;
//        }
//
//        public Integer getSpecialConsideration() {
//            return specialConsideration;
//        }
//
//        public void setSpecialConsideration(Integer specialConsideration) {
//            this.specialConsideration = specialConsideration;
//        }

        public ResponsePointObject[] getPoints() {
            return points;
        }

        public void setPoints(ResponsePointObject[] points) {
            this.points = points;
        }

//        public Boolean getTestMode() {
//            return testMode;
//        }
//
//        public void setTestMode(Boolean testMode) {
//            this.testMode = testMode;
//        }
    }
            
    public static class ResponsePointObject implements Serializable {
    
        private Date time;
        private Float speed;
        private Float windDirection;
        
        public ResponsePointObject(Date time, Float speed, Float windDirection) {
            this.time = time;
            this.speed = speed;
            this.windDirection = windDirection;
        }

        public ResponsePointObject(MeasurementPoint point) {
            this.time = point.getTime();
            this.speed = point.getWindSpeed();
            this.windDirection = point.getWindDirection();
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

        public Float getWindDirection() {
            return windDirection;
        }

        public void setWindDirection(Float windDirection) {
            this.windDirection = windDirection;
        }   
    }
	
	
	@Override
	protected Class<MeasurementsExternalService.RequestParameters> type() {
		return MeasurementsExternalService.RequestParameters.class;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, RequestParameters object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		
	    if (object == null) {
            throw new ProtocolException("Missing request body");
        }
	    
	   
	    
	    if (object.getUserEmail() == null || "".equals(object.getUserEmail())) {
            logger.error("Received Measurements request with no userEmail");
            throw new ProtocolException("Received Measurements request with no userEmail");
        }
	    
	    if (object.getStartTime() == null) {
	        object.setStartTime(new Date(0));
        }
	    
	    if (object.getEndTime() == null) {
            object.setEndTime(new Date());
        }
	    
	    checkAuth(req.getHeader("authToken"), object.getUserEmail());
	    
        if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);

//			Date startTime = (object == null) ? null : object.getStartTime();
//			Date endTime = (object == null) ? null : object.getStartTime();

//			if (startTime == null || startTime.getTime() < (System.currentTimeMillis() - TIME_LIMIT_MILLIS)) {
//				logger.warn("Start time is before history time limit, using 24 hours back instead");
//				startTime = new Date(System.currentTimeMillis() - TIME_LIMIT_MILLIS);
//			}

//			List<Object[]> measurements = hibernateSession.createQuery("")
	        
			@SuppressWarnings("unchecked")
            List<MeasurementSession> measurements = (List<MeasurementSession>) hibernateSession.createQuery(
	                "select s from MeasurementSession s " +
                    "where s.deleted=0 and s.device.user.email=:userEmail "+
                    "and s.endTime>:startTime and s.startTime<:endTime " +
                    "order by s.id desc") //and s.endTime>:endTime
	                .setString("userEmail", object.getUserEmail())
	                .setLong("startTime", object.getStartTime().getTime())
	                .setLong("endTime", object.getEndTime().getTime())
	                .setMaxResults(50)
	                .list();
//	                .setLong("endTime", returnMeasurementsFrom.getTime()).list();
			
			logger.info("Found " + measurements.size() + " measurements");
	        
	        List<ResponseSessionObject> responseList = new ArrayList<ResponseSessionObject>(measurements.size());
	        for (MeasurementSession measurementSession : measurements) {
	            responseList.add(new ResponseSessionObject(measurementSession, hibernateSession));
	        }
	        
	        Map<String,Object> responseMap = new HashMap<String,Object>();
//	        responseMap.put("fromEndTime", returnMeasurementsFrom);
	        responseMap.put("measurements", responseList);        
			        
//			List<Object[]> measurements = hibernateSession.createSQLQuery(
//					"select latitude, longitude, startTime, windSpeedAvg, windSpeedMax, if(windMeter=1,null,windDirection) " + 
//					"from MeasurementSession " + 
//					"where deleted=0 and privacy=1 and startTime>:startTime and " + 
//					"latitude is not null and longitude is not null and " +
//					"windSpeedAvg is not null")
//					.setLong("startTime", startTime.getTime()).list();
			
			writeJSONResponse(resp, mapper, responseMap);
		}
	}
	
    @Override
    protected boolean requiresAuthentication() {
        return false;
    }

	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private boolean checkAuth(String authToken, String email) throws UnauthorizedException, ProtocolException {
	    if (AUTH_TOKENS.containsKey(authToken)) {
            // check domain
	        String emailDomain = email.substring(email.indexOf("@")+1);
	        if (AUTH_TOKENS.get(authToken).equals(emailDomain)) {
                return true;
            } else {
                throw new ProtocolException("Unauthorized access to this email domain!");
            }
	    } else {
	        throw new UnauthorizedException();
        }
        
    }
}
