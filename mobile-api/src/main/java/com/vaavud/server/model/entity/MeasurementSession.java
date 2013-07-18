package com.vaavud.server.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.vaavud.server.model.Model;

@Entity
public class MeasurementSession extends IdEntity {

	private Long id;
	private String uuid;
	private Device device;
    private Date creationTime = new Date();
    private boolean measuring = false;
    private boolean uploaded = false;
    private Long timezoneOffset;
    private Date startTime;
    private Date endTime;
    private LatLng position;
    private Float windSpeedAvg;
    private Float windSpeedMax;
    private Float windDirection;
    private List<MeasurementPoint> points = new ArrayList<MeasurementPoint>();
	
    public void setFrom(MeasurementSession other) {
    	setMeasuring(other.isMeasuring());
    	setUploaded(other.isUploaded());
    	setTimezoneOffset(other.getTimezoneOffset());
    	setStartTime(other.getStartTime());
    	setEndTime(other.getEndTime());
    	setPosition(other.getPosition());
    	setWindSpeedAvg(other.getWindSpeedAvg());
    	setWindSpeedMax(other.getWindSpeedMax());
    	setWindDirection(other.getWindDirection());
    }
    
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(unique = true, nullable = false, length = 36)
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@ManyToOne(
		optional = false,
		fetch = FetchType.EAGER
	)
	@Fetch(FetchMode.JOIN)
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Column(columnDefinition = "bit", length = 1)
	public boolean isMeasuring() {
		return measuring;
	}

	public void setMeasuring(boolean measuring) {
		this.measuring = measuring;
	}

	@Column(columnDefinition = "bit", length = 1)
	public boolean isUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}

	@Column(nullable = false)
	@Type(type = "com.vaavud.server.model.hibernate.IntegerDateType")
	public Date getCreationTime() {
		return creationTime;
	}

	@SuppressWarnings("unused")
	private void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	
	@Basic
	public Long getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(Long timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	@Type(type = "com.vaavud.server.model.hibernate.IntegerDateType")
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Type(type = "com.vaavud.server.model.hibernate.IntegerDateType")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Embedded
	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}

	@Basic
	public Float getWindSpeedAvg() {
		return windSpeedAvg;
	}

	public void setWindSpeedAvg(Float windSpeedAvg) {
		this.windSpeedAvg = windSpeedAvg;
	}

	@Basic
	public Float getWindSpeedMax() {
		return windSpeedMax;
	}

	public void setWindSpeedMax(Float windSpeedMax) {
		this.windSpeedMax = windSpeedMax;
	}

	@Basic
	public Float getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(Float windDirection) {
		this.windDirection = windDirection;
	}
	
	@OneToMany(
		cascade = {CascadeType.ALL},
		mappedBy = "session",
		fetch = FetchType.LAZY
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public List<MeasurementPoint> getPoints() {
		return points;
	}
	
	@SuppressWarnings("unused")
	private void setPoints(List<MeasurementPoint> points) {
		this.points = points;
	}
	
	@Override
	public String toString() {
		return "MeasurementSession [id=" + id + ", uuid=" + uuid
				+ ", measuring=" + measuring + ", uploaded=" + uploaded
				+ ", creationTime=" + creationTime + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", position=" + position
				+ ", windSpeedAvg=" + windSpeedAvg + ", windSpeedMax="
				+ windSpeedMax + ", windDirection=" + windDirection + "]";
	}
}
