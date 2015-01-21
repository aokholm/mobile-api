package com.vaavud.server.model.entity;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Basic;

import org.hibernate.annotations.Type;

@Entity
public class CalibrationFile extends IdEntity {
	
	private static final boolean isAlgorithmVersionGreatherThanEq(String appVersion1, String appVersion2) {
		int v1 = (appVersion1 == null) ? -1 : computeComparableVersion(appVersion1);
		int v2 = (appVersion2 == null) ? -1 : computeComparableVersion(appVersion2);
		return v1 == -1 || v2 == -1 ? false : v1 >= v2;
	}
	
	private static final boolean isAlgorithmVersionLessThan(String appVersion1, String appVersion2) {
		int v1 = (appVersion1 == null) ? -1 : computeComparableVersion(appVersion1);
		int v2 = (appVersion2 == null) ? -1 : computeComparableVersion(appVersion2);
		return v1 == -1 || v2 == -1 ? false : v1 < v2;
	}
	
	private static final int computeComparableVersion(String appVersion) {
		Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+).(\\d+)").matcher(appVersion);
		if (matcher.find()) {
			try {
				int major = Integer.parseInt(matcher.group(1));
				int minor = Integer.parseInt(matcher.group(2));
				int sub = Integer.parseInt(matcher.group(3));
				return major * 10000 + minor * 100 + sub;
			}
			catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}
	
	private Long id;
	private String s3FileName;
	private String deviceUuid;
	private Date creationTime = new Date();
	private float[] calibrationCoefficients;
	private String algorithmVersion;
	private Boolean verified = false;
	
	public CalibrationFile(){
		
	}
	
	public void setFrom(CalibrationFile other) {
		
		setAlgorithmVersion(other.getAlgorithmVersion());
		setS3FileName(other.getS3FileName());
		setDeviceUuid(other.getDeviceUuid());
		setCalibrationCoefficients(other.getCalibrationCoefficients());
	}

	public void setCalibrationCoefficients(float[] calibrationCoefficients) {
		this.calibrationCoefficients=calibrationCoefficients;
		
	}
	@Basic
	public float[] getCalibrationCoefficients() {
		return calibrationCoefficients;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

	@Column(nullable = false)
	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setS3FileName(String s3FileName) {
		this.s3FileName=s3FileName;
		
	}
	@Column(unique = true, nullable = false)
	public String getS3FileName() {
		return s3FileName;
	}

	public void setAlgorithmVersion(String algorithmVersion) {
		this.algorithmVersion = algorithmVersion;
		
	}

	@Basic
	private String getAlgorithmVersion() {
		return algorithmVersion;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Override
	public Long getId() {
		return id;
	}
	
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
	
	@Column(nullable = false)
	public Boolean getVerified() {
		return verified;
	}

	
	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	public boolean isValidFileName() {
		
		return true;
	}

	
}
