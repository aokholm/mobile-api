package com.vaavud.server.model.migration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.MeasurementPoint;
import com.vaavud.server.model.entity.MeasurementSession;
import com.vaavud.server.model.entity.User;

public class FirebaseMigrator {
	
	private static final Logger logger = Logger.getLogger(FirebaseMigrator.class);
	
	public static final String FIREBASE_BASE_URL = "https://vaavud-migration.firebaseio.com/";
	public static final String FIREBASE_USER = "user/";
	public static final String FIREBASE_DEVICE = "device/";
	public static final String FIREBASE_SESSION = "session/";
	public static final String FIREBASE_USERID = "tomcatIds/";
	public static final String FIREBASE_GEO = "session_geo/";
	public static final String FIREBASE_WIND = "wind/";
	
	
	public static void createUser(final User user, final Device device) {
		final Firebase ref = new Firebase(FIREBASE_BASE_URL);
		String newPassword = user.getEmail() + user.getId().toString();
		
		ref.createUser(user.getEmail(), newPassword, new Firebase.ValueResultHandler<Map<String, Object>>() {
		    @Override
		    public void onSuccess(Map<String, Object> result) {
		    	String userId = (String) result.get("uid");
		    	logger.info("Successfully firebase user account with uid: " + userId);
		    	ref.child("tomcatIds/" + user.getId().toString()).setValue(userId);
		    	insertUser(user, device, userId);
		 
		    }
		    @Override
		    public void onError(FirebaseError firebaseError) {
		        // there was an error
		    	logger.info(FirebaseError.fromCode(firebaseError.getCode()) + " " + firebaseError.getMessage());
		    	ref.child("tomcatIdsFailed/" + user.getId().toString()).setValue(FirebaseError.fromCode(firebaseError.getCode()));
		    }
		});
	}
		
	public static void setUser(final User user, final Device device) {
		
		if ( user.getFacebookId() == null ) { // if is using email + password
			// first check if we already have a firebase UserID
			Firebase ref = new Firebase(FIREBASE_BASE_URL + FIREBASE_USERID + user.getId().toString());
			
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
			    @Override
			    public void onDataChange(DataSnapshot snapshot) {
			    	
			    	if (snapshot.getValue() == null) { // user does not exist in firebase
			    		createUser(user, device);
			    	} else {
				    	String userId = (String) snapshot.getValue();
				    	insertUser(user, device, userId);
			    	}
			    }
			    @Override
			    public void onCancelled(FirebaseError firebaseError) {
			    	
			    }
			});
		} else { // if using facebook
			insertUser(user, device, "facebook:" + user.getFacebookId());
		}	
	}
	
	private static void insertUser(User user, Device device, String firebaseID) {
		Firebase ref = new Firebase(FIREBASE_BASE_URL + FIREBASE_USER);
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("created", user.getCreationTime());
		data.put("email", user.getEmail());
		data.put("firstName", user.getFirstName());
		data.put("lastName", user.getLastName());
		ref.child(firebaseID).setValue(data);
		
		logger.info("Insert user " + firebaseID + " to firebase!");
		
		// update user id on device
		
		String deviceUid = FirebasePushIdGenerator.generatePushId(device.getCreationTime(), device.getId());
		Firebase refDevice = new Firebase(FIREBASE_BASE_URL + FIREBASE_DEVICE + deviceUid + "/user_id");
		refDevice.setValue(firebaseID);
	}
	
	public static void setDevice(Device device) {
		
		Firebase ref = new Firebase(FIREBASE_BASE_URL + FIREBASE_DEVICE);
				
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appVersion", device.getAppVersion());
		data.put("created", device.getCreationTime().getTime());
		data.put("model", device.getModel());
		data.put("osVersion", device.getOsVersion());
		data.put("vendor", device.getVendor());
		
		// data.put("user_id", fireUserID(device.getUser())); user id is set by user function
		
		String deviceUid = FirebasePushIdGenerator.generatePushId(device.getCreationTime(), device.getId());
		
		ref.child(deviceUid).updateChildren(data);
		
		logger.info("Insert device " + deviceUid + " to firebase!");
	}
	
	public static void setSession(MeasurementSession session) {
		
		updateFirebaseDataSession(session); // SEND TO LEGACY SUBSCRIPTION DATABASE
		
		Firebase ref = new Firebase(FIREBASE_BASE_URL + FIREBASE_SESSION);
		GeoFire geoFireSessionClient = new GeoFire(new Firebase(FIREBASE_BASE_URL+FIREBASE_GEO));
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("timeStart", session.getStartTime().getTime());
		data.put("deviceKey", session.getDevice().getUuid());
		data.put("timeStop", session.getEndTime().getTime());
		data.put("timeUploaded", new Date().getTime());
		if (session.getWindSpeedAvg()!=null && session.getWindSpeedMax()!=null) {
			data.put("windMean",session.getWindSpeedAvg());
			data.put("windMax", session.getWindSpeedMax());
		}
		if (session.getWindDirection() != null) {
			data.put("windDirection", session.getWindDirection());
		}
		if (session.getPosition() != null) {
			Map<String, Object> location = new HashMap<String, Object>();
			location.put("lat", session.getPosition().getLatitude());
			location.put("lon", session.getPosition().getLongitude());
			data.put("location", location);
		}
		
		String sessionUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), session.getId());
		
		logger.info("sesion UID" + sessionUid + "session.getId() " + session.getId());
		
		ref.child(sessionUid).setValue(data);
		
		if (session.getPosition() != null) {
			geoFireSessionClient.setLocation(sessionUid, new GeoLocation(session.getPosition().getLatitude(), session.getPosition().getLongitude()));
		}
	}
	
	public static void setPoint(MeasurementPoint point, MeasurementSession session) {

		Firebase ref = new Firebase(FIREBASE_BASE_URL + FIREBASE_WIND);
		String sessionUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), session.getId());
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("sessionKey", sessionUid);
		data.put("time", point.getTime().getTime());
		data.put("speed",point.getWindSpeed());
		if (point.getWindDirection() != null) {
			data.put("direction", point.getWindSpeed());
		}
		
		String pointUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), point.getId()); // NOTE: we use the session creation time
		
		ref.child(pointUid).setValue(data);
	}
	
	// LEGACY FOR SUBSCRIPTIONS IN ANDROID 0.5.3
	public static void updateFirebaseDataSession(MeasurementSession session) {
		
		String FIREBASE_BASE_URL = "https://vaavud-tomcat.firebaseio.com/";
		String FIREBASE_SESSION = "session/";
		String FIREBASE_GEO = "geo/";
		
		Firebase firebaseSessionClient = new Firebase(FIREBASE_BASE_URL+FIREBASE_SESSION);
		GeoFire geoFireSessionClient = new GeoFire(new Firebase(FIREBASE_BASE_URL+FIREBASE_GEO+FIREBASE_SESSION));
		
		
		Map<String, Object> data = new HashMap<String, Object>();
		String firebaseSessionKey = session.getUuid();
		//				Log.d(TAG, "FirebaseSessionKey: " + firebaseSessionKey);
		data.put("timeStart", session.getStartTime().getTime());
		data.put("deviceKey", session.getDevice().getUuid());
		data.put("timeStop", session.getEndTime().getTime());
		data.put("timeUploaded", new Date().getTime());
		if (session.getWindSpeedAvg()!=null && session.getWindSpeedMax()!=null) {
			data.put("windMean",session.getWindSpeedAvg());
			data.put("windMax", session.getWindSpeedMax());
		}
		if (session.getWindDirection() != null) {
			data.put("windDirection", session.getWindDirection());
		}
		if (session.getPosition() != null) {
			data.put("locLat", session.getPosition().getLatitude());
			data.put("locLon", session.getPosition().getLongitude());
			geoFireSessionClient.setLocation(firebaseSessionKey, new GeoLocation(session.getPosition().getLatitude(), session.getPosition().getLongitude()));
		}
		if (!firebaseSessionKey.isEmpty()){
			firebaseSessionClient.child(firebaseSessionKey).setValue(data);
		}
	}
	
	
}
