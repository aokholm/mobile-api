package com.vaavud.server.model.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

@Entity
public class MeasurementPoint extends IdEntity {

	private Long id;
	private MeasurementSession session;
	private Date time;
	private Float windSpeed;
	private Float windDirection;

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Override
	public Long getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(
		optional = false,
		fetch = FetchType.LAZY
	)
	public MeasurementSession getSession() {
		return session;
	}

	public void setSession(MeasurementSession session) {
		this.session = session;
	}

	@Column(nullable = false)
	@Type(type = "com.vaavud.server.model.hibernate.IntegerDateType")
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	@Basic
	public Float getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(Float windSpeed) {
		this.windSpeed = windSpeed;
	}

	@Basic
	public Float getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(Float windDirection) {
		this.windDirection = windDirection;
	}
}
