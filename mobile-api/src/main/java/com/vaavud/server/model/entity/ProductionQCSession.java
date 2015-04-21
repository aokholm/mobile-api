package com.vaavud.server.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

@Entity
public class ProductionQCSession extends IdEntity {

    private Long id;
	private Date creationTime = new Date();
	private Float velocityProfileError;
	private Float velocity;
	private Float velocityTarget;
    private Float direction;
	private Integer tickDetectionErrorCount;
    private List<Float> velocityProfile = new ArrayList<Float>();
	private Boolean qcPassed;
	
	public ProductionQCSession() {
	}

    public ProductionQCSession(Float velocityProfileError, Float velocity,
            Float velocityTarget, Float direction, Integer tickDetectionErrorCount, List<Float> velocityProfile,
            Boolean qcPassed) {
        super();
        this.velocityProfileError = velocityProfileError;
        this.velocity = velocity;
        this.velocityTarget = velocityTarget;
        this.direction = direction;
        this.tickDetectionErrorCount = tickDetectionErrorCount;
        this.velocityProfile = velocityProfile;
        this.qcPassed = qcPassed;
    }

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
	@Override
	public Long getId() {
		return id;
	}

	@SuppressWarnings("unused")
    public void setId(Long id) {
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
    public Float getVelocityTarget() {
        return velocityTarget;
    }

    public void setVelocityTarget(Float velocityTarget) {
        this.velocityTarget = velocityTarget;
    }
    
    @Basic
    public Float getDirection() {
        return direction;
    }

    public void setDirection(Float direction) {
        this.direction = direction;
    }

    @Basic
    public Integer getTickDetectionErrorCount() {
        return tickDetectionErrorCount;
    }

    public void setTickDetectionErrorCount(Integer tickDetectionErrorCount) {
        this.tickDetectionErrorCount = tickDetectionErrorCount;
    }

    @ElementCollection
    @CollectionTable(name ="QCVelocityProfile")
    public List<Float> getVelocityProfile() {
        return velocityProfile;
    }

    public void setVelocityProfile(List<Float> velocityProfile) {
        this.velocityProfile = velocityProfile;
    }

    @Column(columnDefinition = "bit", length = 1)
    public Boolean getQcPassed() {
        return qcPassed;
    }

    public void setQcPassed(Boolean qcPassed) {
        this.qcPassed = qcPassed;
    }

    @Override
    public String toString() {
        return "ProductionQCSession [id=" + id + ", creationTime=" + creationTime + ", velocityProfileError="
                + velocityProfileError + ", velocity=" + velocity + ", velocityTarget=" + velocityTarget
                + ", direction=" + direction + ", tickDetectionErrorCount=" + tickDetectionErrorCount
                + ", velocityProfile=" + velocityProfile + ", qcPassed=" + qcPassed + "]";
    }
}
