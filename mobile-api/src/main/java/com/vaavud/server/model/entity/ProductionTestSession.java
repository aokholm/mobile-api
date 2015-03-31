package com.vaavud.server.model.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

@Entity
public class ProductionTestSession extends IdEntity {

	private Long id;
	private Date creationTime;
	private Float velocityProfileError;
	private Float velocity;
	private Float direction;
	private Integer measurementPoints;
	
	public ProductionTestSession() {
	}

    public ProductionTestSession(Float velocityProfileError, Float velocity, Float direction, Integer measurementPoints) {
        super();
        this.velocityProfileError = velocityProfileError;
        this.velocity = velocity;
        this.direction = direction;
        this.measurementPoints = measurementPoints;
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

    @Column(nullable = false)
    @Type(type = "com.vaavud.server.model.hibernate.IntegerDateType")
    public Date getCreationTime() {
        return creationTime;
    }
    
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
	
	@Basic
	public Float getVelocityProfileError() {
        return velocityProfileError;
    }

    public void setVelocityProfileError(Float velocityProfileError) {
        this.velocityProfileError = velocityProfileError;
    }

    @Basic
    public Float getVelocity() {
        return velocity;
    }

    public void setVelocity(Float velocity) {
        this.velocity = velocity;
    }

    @Basic
    public Float getDirection() {
        return direction;
    }

    public void setDirection(Float direction) {
        this.direction = direction;
    }

    @Basic
    public Integer getMeasurementPoints() {
        return measurementPoints;
    }

    public void setMeasurementPoints(Integer measurementPoints) {
        this.measurementPoints = measurementPoints;
    }

    @Override
    public String toString() {
        return "ProductionTestSession [id=" + id + ", creationTime=" + creationTime + ", velocityProfileError="
                + velocityProfileError + ", velocity=" + velocity + ", direction=" + direction + ", measurementPoints="
                + measurementPoints + "]";
    }
}
