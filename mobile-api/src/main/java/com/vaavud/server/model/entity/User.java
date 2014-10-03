package com.vaavud.server.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

@Entity
public class User extends IdEntity {

	private Long id;
	private Date creationTime = new Date();
	private String email;
	private String passwordHash;
	private String facebookId;
	private String facebookAccessToken;
	private String activationCode;
	private String firstName;
	private String lastName;
	private Gender gender = Gender.UNKNOWN;
	private boolean verified = false;
	private boolean newsletter = true;
	private boolean deleted = false;
	private boolean validAgricultureSubscription = false;
    private List<Device> devices = new ArrayList<Device>();

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

	@Column(unique = true, nullable = true)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Basic
	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	@Basic
	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	@Basic
	public String getFacebookAccessToken() {
		return facebookAccessToken;
	}

	public void setFacebookAccessToken(String facebookAccessToken) {
		this.facebookAccessToken = facebookAccessToken;
	}

	@Basic
	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	@Basic
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Basic
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint unsigned", nullable = false)
	public Gender getGender() {
		return gender;
	}

    public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Column(columnDefinition = "bit", length = 1, nullable = false)
	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	@Column(columnDefinition = "bit", length = 1, nullable = false)
	public boolean isNewsletter() {
		return newsletter;
	}

	public void setNewsletter(boolean newsletter) {
		this.newsletter = newsletter;
	}
	
	@Column(columnDefinition = "bit", length = 1, nullable = false)
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	@Column(columnDefinition = "bit", length = 1, nullable = false)
	public boolean isValidAgricultureSubscription() {
		return validAgricultureSubscription;
	}

	public void setValidAgricultureSubscription(boolean validAgricultureSubscription) {
		this.validAgricultureSubscription = validAgricultureSubscription;
	}

	@OneToMany(
		mappedBy = "user",
		fetch = FetchType.LAZY
	)
	public List<Device> getDevices() {
		return devices;
	}
		
	@SuppressWarnings("unused")
	private void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", creationTime=" + creationTime + ", email="
				+ email + ", passwordHash=" + passwordHash + ", facebookId="
				+ facebookId + ", facebookAccessToken=" + facebookAccessToken
				+ ", activationCode=" + activationCode + ", firstName="
				+ firstName + ", lastName=" + lastName + ", newsletter="
				+ newsletter + ", deleted=" + deleted + "]";
	}
}
