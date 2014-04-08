package com.vaavud.server.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

@Entity
public class MagneticSession extends IdEntity {

	private Long id;
	private String measurementSessionUuid;
    private Date creationTime = new Date();
    private Date startTime;
	private int startIndex;       // magnetic point upload start index
	private int endIndex;         // magnetic point upload end index
	private List<Float[]> points; // magnetic points uploaded (not mapped to DB)
	private List<MagneticPoint> magneticPoints = new ArrayList<MagneticPoint>();
	private List<AccPoint> accPoints = new ArrayList<AccPoint>();
	
	public MagneticSession() {
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
	public String getMeasurementSessionUuid() {
		return measurementSessionUuid;
	}

	public void setMeasurementSessionUuid(String measurementSessionUuid) {
		this.measurementSessionUuid = measurementSessionUuid;
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

	@Type(type = "com.vaavud.server.model.hibernate.IntegerDateType")
	public Date getStartTime() {
		return startTime;
	}

	@SuppressWarnings("unused")
	private void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Magnetic points.
	 */
	@OneToMany(
		cascade = {CascadeType.ALL},
		mappedBy = "magneticSession",
		fetch = FetchType.LAZY
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public List<MagneticPoint> getMagneticPoints() {
		return magneticPoints;
	}

	public void setMagneticPoints(List<MagneticPoint> magneticPoints) {
		this.magneticPoints = magneticPoints;
	}

	/**
	 * Magnetic point start index.
	 */
	@Transient
	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * Magnetic point end index.
	 */
	@Basic
	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	/**
	 * Magnetic points as floats.
	 */
	@Transient
	public List<Float[]> getPoints() {
		return points;
	}

	public void setPoints(List<Float[]> points) {
		this.points = points;
	}
	
	public int appendToMagneticPoints() {
		int num = startIndex;
		for (Float[] point : points) {
			magneticPoints.add(new MagneticPoint(this, num, point));
			num++;
		}
		return num;
	}

	@OneToMany(
		cascade = {CascadeType.ALL},
		mappedBy = "magneticSession",
		fetch = FetchType.LAZY
	)
	@OnDelete(action = OnDeleteAction.CASCADE)
	public List<AccPoint> getAccPoints() {
		return accPoints;
	}

	public void setAccPoints(List<AccPoint> accPoints) {
		this.accPoints = accPoints;
	}

	@Override
	public String toString() {
		return "MagneticSession [measurementSessionUuid="
				+ measurementSessionUuid + ", startIndex=" + startIndex
				+ ", endIndex=" + endIndex + "]";
	}
}
