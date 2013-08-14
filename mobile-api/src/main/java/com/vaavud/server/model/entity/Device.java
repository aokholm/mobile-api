package com.vaavud.server.model.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

@Entity
public class Device extends IdEntity {

	private Long id;
	private String uuid;
	private String authToken;
	private Date creationTime = new Date();
	private String vendor;
	private String model;
	private String os;
	private String osVersion;
	private String app;
	private String appVersion;
	private String country;
	private String language;
	private Long timezoneOffset;
	private WindSpeedUnit windSpeedUnit;
	private String magneticFieldSensor;
	private Boolean uploadMagneticData;

	public void setFrom(Device other) {
		setVendor(other.getVendor());
		setModel(other.getModel());
		setOs(other.getOs());
		setOsVersion(other.getOsVersion());
		setApp(other.getApp());
		setAppVersion(other.getAppVersion());
		setCountry(other.getCountry());
		setLanguage(other.getLanguage());
		setTimezoneOffset(other.getTimezoneOffset());
		setWindSpeedUnit(other.getWindSpeedUnit());
		setMagneticFieldSensor(other.getMagneticFieldSensor());
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

	@Column(unique = true, nullable = false, length = 36)
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Column(unique = true, nullable = false, length = 36)
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
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
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Basic
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Basic
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	@Basic
	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	@Basic
	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	@Basic
	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	@Column(length = 10)
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = (country != null && country.length() > 10) ? country
				.substring(0, 10) : country;
	}

	@Column(length = 10)
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = (language != null && language.length() > 10) ? language
				.substring(0, 10) : language;
	}

	@Basic
	public Long getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(Long timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint unsigned")
	public WindSpeedUnit getWindSpeedUnit() {
		return windSpeedUnit;
	}

	public void setWindSpeedUnit(WindSpeedUnit windSpeedUnit) {
		this.windSpeedUnit = windSpeedUnit;
	}

	@Basic
	public String getMagneticFieldSensor() {
		return magneticFieldSensor;
	}

	public void setMagneticFieldSensor(String magneticFieldSensor) {
		this.magneticFieldSensor = magneticFieldSensor;
	}
	
	@Column(columnDefinition = "bit", length = 1)
	public Boolean getUploadMagneticData() {
		return uploadMagneticData;
	}

	public void setUploadMagneticData(Boolean uploadMagneticData) {
		this.uploadMagneticData = uploadMagneticData;
	}

	public boolean equalValues(Device other) {
		if (this == other) {
			return true;
		}
		if (app == null) {
			if (other.app != null) {
				return false;
			}
		} else if (!app.equals(other.app)) {
			return false;
		}
		if (appVersion == null) {
			if (other.appVersion != null) {
				return false;
			}
		} else if (!appVersion.equals(other.appVersion)) {
			return false;
		}
		if (country == null) {
			if (other.country != null) {
				return false;
			}
		} else if (!country.equals(other.country)) {
			return false;
		}
		if (language == null) {
			if (other.language != null) {
				return false;
			}
		} else if (!language.equals(other.language)) {
			return false;
		}
		if (magneticFieldSensor == null) {
			if (other.magneticFieldSensor != null) {
				return false;
			}
		} else if (!magneticFieldSensor.equals(other.magneticFieldSensor)) {
			return false;
		}
		if (model == null) {
			if (other.model != null) {
				return false;
			}
		} else if (!model.equals(other.model)) {
			return false;
		}
		if (os == null) {
			if (other.os != null) {
				return false;
			}
		} else if (!os.equals(other.os)) {
			return false;
		}
		if (osVersion == null) {
			if (other.osVersion != null) {
				return false;
			}
		} else if (!osVersion.equals(other.osVersion)) {
			return false;
		}
		if (timezoneOffset == null) {
			if (other.timezoneOffset != null) {
				return false;
			}
		} else if (!timezoneOffset.equals(other.timezoneOffset)) {
			return false;
		}
		if (vendor == null) {
			if (other.vendor != null) {
				return false;
			}
		} else if (!vendor.equals(other.vendor)) {
			return false;
		}
		if (windSpeedUnit != other.windSpeedUnit) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", uuid=" + uuid + ", authToken="
				+ authToken + ", creationTime=" + creationTime + ", vendor="
				+ vendor + ", model=" + model + ", os=" + os + ", osVersion="
				+ osVersion + ", app=" + app + ", appVersion=" + appVersion
				+ ", country=" + country + ", language=" + language
				+ ", timezoneOffset=" + timezoneOffset + ", windSpeedUnit="
				+ windSpeedUnit + ", magneticFieldSensor="
				+ magneticFieldSensor + ", uploadMagneticData="
				+ uploadMagneticData + "]";
	}
}
