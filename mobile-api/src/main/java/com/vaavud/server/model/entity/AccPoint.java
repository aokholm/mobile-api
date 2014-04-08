package com.vaavud.server.model.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class AccPoint extends IdEntity {

	private Long id;
	private MagneticSession magneticSession;
	private Integer num;
	private Float time;
	private Float x;
	private Float y;
	private Float z;
	
	public AccPoint() {
	}

	public AccPoint(MagneticSession magneticSession, Integer num, Float[] values) {
		this.magneticSession = magneticSession;
		this.num = num;
		this.time = values[0];
		this.x = values[1];
		this.y = values[2];
		this.z = values[3];
	}

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
	public MagneticSession getMagneticSession() {
		return magneticSession;
	}

	public void setMagneticSession(MagneticSession magneticSession) {
		this.magneticSession = magneticSession;
	}

	@Basic
	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	@Basic
	public Float getTime() {
		return time;
	}

	public void setTime(Float time) {
		this.time = time;
	}

	@Basic
	public Float getX() {
		return x;
	}

	public void setX(Float x) {
		this.x = x;
	}

	@Basic
	public Float getY() {
		return y;
	}

	public void setY(Float y) {
		this.y = y;
	}

	@Basic
	public Float getZ() {
		return z;
	}

	public void setZ(Float z) {
		this.z = z;
	}
	
	@Override
	public String toString() {
		return "AccPoint [id=" + id + ", magneticSession_id=" + magneticSession.getId() + ", num =" +
				num + ", time=" + time + ", x=" + x + ", y=" + y + ", z=" + z +"]";
	}
}
