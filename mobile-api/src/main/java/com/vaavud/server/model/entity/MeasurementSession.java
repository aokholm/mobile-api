package com.vaavud.server.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.vaavud.server.model.Model;

@Entity
public class MeasurementSession extends IdEntity {

	private Long id;
	private String uuid;
	private Device device;
	private WindMeter windMeter = WindMeter.MJOLNIR;
    private Date creationTime = new Date();
	private String source;
    private boolean measuring = false;
    private boolean uploaded = false;
    private boolean deleted = false;
    private int startIndex = 0;
    private int endIndex = 0;
    private Long timezoneOffset;
    private Date startTime;
    private Date endTime;
    private LatLng position;
    private Float windSpeedAvg;
    private Float windSpeedMax;
    private Float windDirection;
    private Float temperature;
    private ReduceEquipment reduceEquipment;
    private Float dose;
    private Integer boomHeight;
    private SprayQuality sprayQuality;
    private Integer generalConsideration;
    private Integer specialConsideration;
    private Privacy privacy = Privacy.PUBLIC;
    private Boolean testMode;
    private List<MeasurementPoint> points = new ArrayList<MeasurementPoint>();
	
    public void setFrom(MeasurementSession other) {
    	setWindMeter(other.getWindMeter());
    	setSource(other.getSource());
    	setMeasuring(other.isMeasuring());
    	setUploaded(other.isUploaded());
    	setEndIndex(other.getEndIndex());
    	setTimezoneOffset(other.getTimezoneOffset());
    	setStartTime(other.getStartTime());
    	setEndTime(other.getEndTime());
    	setPosition(other.getPosition());
    	setWindSpeedAvg(other.getWindSpeedAvg());
    	setWindSpeedMax(other.getWindSpeedMax());
    	setWindDirection(other.getWindDirection());
    	setPrivacy(other.getPrivacy());
    	setTemperature(other.getTemperature());
    	setReduceEquipment(other.getReduceEquipment());
    	setDose(other.getDose());
    	setBoomHeight(other.getBoomHeight());
    	setSprayQuality(other.getSprayQuality());
    	setGeneralConsideration(other.getGeneralConsideration());
    	setSpecialConsideration(other.getSpecialConsideration());
    }
    
    public boolean hasAdditionalProperties() {
    	return (dose != null && dose > 0.0F) || (boomHeight != null && boomHeight > 0) || (sprayQuality != null && sprayQuality != SprayQuality.NOT_APPLICABLE);
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

	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint unsigned", nullable = false)
	public WindMeter getWindMeter() {
		return windMeter;
	}

	public void setWindMeter(WindMeter windMeter) {
		this.windMeter = windMeter;
	}

	public String getSource() {
		return source;
	}

	@Basic
	public void setSource(String source) {
		this.source = source;
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

	@Index(name = "deletedIndex")
	@Column(columnDefinition = "bit", length = 1, nullable = false)
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Column(nullable = false)
	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	@Column(nullable = false)
	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
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
	
	@Basic
	public Float getTemperature() {
		return temperature;
	}

	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}
	
	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint unsigned")
	public ReduceEquipment getReduceEquipment() {
		return reduceEquipment;
	}

	public void setReduceEquipment(ReduceEquipment reduceEquipment) {
		this.reduceEquipment = reduceEquipment;
	}

	@Basic
	public Float getDose() {
		return dose;
	}

	public void setDose(Float dose) {
		this.dose = dose;
	}

	@Basic
	public Integer getBoomHeight() {
		return boomHeight;
	}

	public void setBoomHeight(Integer boomHeight) {
		this.boomHeight = boomHeight;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint unsigned")
	public SprayQuality getSprayQuality() {
		return sprayQuality;
	}

	public void setSprayQuality(SprayQuality sprayQuality) {
		this.sprayQuality = sprayQuality;
	}

	@Basic
	public Integer getGeneralConsideration() {
		return generalConsideration;
	}

	public void setGeneralConsideration(Integer generalConsideration) {
		this.generalConsideration = generalConsideration;
	}

	@Basic
	public Integer getSpecialConsideration() {
		return specialConsideration;
	}

	public void setSpecialConsideration(Integer specialConsideration) {
		this.specialConsideration = specialConsideration;
	}
	
	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint unsigned")
	public Privacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(Privacy privacy) {
		this.privacy = privacy;
	}

	@Column(columnDefinition = "bit", length = 1)
	public Boolean getTestMode() {
		return testMode;
	}

	public void setTestMode(Boolean testMode) {
		this.testMode = testMode;
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
				+ ", windMeter=" + windMeter + ", creationTime=" + creationTime
				+ ", source=" + source + ", measuring=" + measuring
				+ ", uploaded=" + uploaded + ", deleted=" + deleted
				+ ", startIndex=" + startIndex + ", endIndex=" + endIndex
				+ ", timezoneOffset=" + timezoneOffset + ", startTime="
				+ startTime + ", endTime=" + endTime + ", position=" + position
				+ ", windSpeedAvg=" + windSpeedAvg + ", windSpeedMax="
				+ windSpeedMax + ", windDirection=" + windDirection
				+ ", temperature=" + temperature + ", reduceEquipment="
				+ reduceEquipment + ", dose=" + dose + ", boomHeight="
				+ boomHeight + ", sprayQuality=" + sprayQuality
				+ ", generalConsideration=" + generalConsideration
				+ ", specialConsideration=" + specialConsideration + ", testMode=" + testMode + "]";
	}
}
