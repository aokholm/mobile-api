package com.vaavud.server.web.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vaavud.server.model.entity.LatLng;
import com.vaavud.server.model.entity.MeasurementSession;

public class MapMeasurement implements Serializable {

	public static List<MapMeasurement> fromMeasurementSessions(List<MeasurementSession> measurementSessions) {
		List<MapMeasurement> mapMeasurements = new ArrayList<MapMeasurement>(measurementSessions.size());
		for (MeasurementSession measurementSession : measurementSessions) {
			mapMeasurements.add(fromMeasurementSession(measurementSession));
		}
		return mapMeasurements;
	}
	
	public static MapMeasurement fromMeasurementSession(MeasurementSession measurementSession) {
		MapMeasurement mapMeasurement = new MapMeasurement();
		mapMeasurement.id = measurementSession.getId();
		mapMeasurement.startTime = measurementSession.getStartTime();
		mapMeasurement.endTime = measurementSession.getEndTime();
		mapMeasurement.position = measurementSession.getPosition();
		mapMeasurement.windSpeedAvg = measurementSession.getWindSpeedAvg();
		mapMeasurement.windSpeedMax = measurementSession.getWindSpeedMax();
		mapMeasurement.windDirection = measurementSession.getWindDirection();
		mapMeasurement.iconNum = "Android".equalsIgnoreCase(measurementSession.getDevice().getOs()) ? 1 : 0;
		return mapMeasurement;
	}
	
	private Long id;
    private Date startTime;
    private Date endTime;
    private LatLng position;
    private Float windSpeedAvg;
    private Float windSpeedMax;
    private Float windDirection;
    private int iconNum;
	
    public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
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

	public LatLng getPosition() {
		return position;
	}
	
	public void setPosition(LatLng position) {
		this.position = position;
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

	public int getIconNum() {
		return iconNum;
	}

	public void setIconNum(int iconNum) {
		this.iconNum = iconNum;
	}
}
