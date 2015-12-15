package com.vaavud.server.model.migration;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.AuthData;
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
import com.vaavud.server.model.entity.WindMeter;

public class FirebaseMigrator {
	
	private static final Logger logger = Logger.getLogger(FirebaseMigrator.class);
	
	public static final String FIREBASE_BASE_URL = "https://vaavud-core.firebaseio.com/";
	public static final String FIREBASE_USER = "user/";
	public static final String FIREBASE_DEVICE = "device/";
	public static final String FIREBASE_SESSION = "session/";
	public static final String FIREBASE_SESSION_DELETED = "sessionDeleted/";
	public static final String FIREBASE_USERID = "tomcat/userId/success/";
	public static final String FIREBASE_USERID_FAILED = "tomcat/userId/fail/";
	public static final String FIREBASE_GEO = "sessionGeo/";;
	public static final String FIREBASE_WIND = "wind/";
	public static final String FIRE_NO_USER = "anonymous";
	public static final String FIRE_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NjQxNTM3NjcuNzM5LCJ2IjowLCJkIjp7InVpZCI6Im1pZ3JhdG9yIn0sImlhdCI6MTQ0ODUzNDU2N30.asIEJ08ju3gWADh6gAfXL467EM3N8aSs_TC_TBUoRII"; // uid: migrator, expieces in 2025
//	public static final String FIRE_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NjQxNTc0NTIuMDg1LCJkZWJ1ZyI6dHJ1ZSwidiI6MCwiZCI6eyJ1aWQiOiJtaWdyYXRvciJ9LCJpYXQiOjE0NDg1MzgyNTJ9.OVGkmQNcl1NK4WmGu0h2w-ZyUkh-c-0jO3xCoycubdk"; // uid: migrator, expieces in 2025 debug = true
	public static Firebase authRef;
	
	public static Firebase getFirebase() {
		if (authRef  == null) {
			final Firebase ref = new Firebase(FIREBASE_BASE_URL);
			ref.authWithCustomToken(FIRE_TOKEN, new Firebase.AuthResultHandler() {
			    @Override
			    public void onAuthenticated(AuthData authData) {
			    	authRef = ref;
			    }
			    @Override
			    public void onAuthenticationError(FirebaseError firebaseError) {
			    	logger.info(FirebaseError.fromCode(firebaseError.getCode()) + " " + firebaseError.getMessage());
			    }
			});
			return null;
		}
		return authRef;
	}
	
	public static void markSessionComplete(String key) {
		Map<String, String> items = new HashMap<>();
        items.put("sessionKey", key);
        Firebase ref = getFirebase().child("sessionComplete/queue/tasks");
        ref.push().setValue(items);
	}
	
	public static void createUser(final User user, final Device device) {
		
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		final String newPassword = user.getEmail() + user.getId().toString();
		
		ref.createUser(user.getEmail(), newPassword, new Firebase.ValueResultHandler<Map<String, Object>>() {
		    @Override
		    public void onSuccess(Map<String, Object> result) {
		    	String uid = (String) result.get("uid");
		    	logger.info("Successfully firebase user account with uid: " + uid);
		    	insertUser(user, device, uid);
		    	FirebaseMigrator.setDevice(device, uid);
		 
		    }
		    @Override
		    public void onError(FirebaseError firebaseError) {
		        // there was an error
		    	if (firebaseError.getCode() == FirebaseError.EMAIL_TAKEN) {
		    		String uid = FIRE_NO_USER;
			    	logger.info("(email taken) Successfully firebase user account with uid: " + uid);
			    	insertUser(user, device, uid);
			    	FirebaseMigrator.setDevice(device, uid);
		    	} else {
		    		logger.info(FirebaseError.fromCode(firebaseError.getCode()) + " " + firebaseError.getMessage());
			    	ref.child(FIREBASE_USERID_FAILED + user.getId().toString()).setValue(firebaseError);
		    	}	
		    }
		});
	}
	
		
	interface CallbackFireUserUID{
		void result(String uid);
		void doesNotExist();
	}
		
	
	public static void getFirebaseUserID(User user, final CallbackFireUserUID callback) {
		
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		if (user == null) {
			callback.doesNotExist();
			return;
		}
		
		if ( user.getFacebookId() == null ) {
			Firebase refUser = ref.child(FIREBASE_USERID + user.getId().toString());
			
			refUser.addListenerForSingleValueEvent(new ValueEventListener() {
			    @Override
			    public void onDataChange(DataSnapshot snapshot) {
			    	
			    	if (snapshot.getValue() == null) { // user does not exist in firebase
			    		callback.doesNotExist();
			    	} else {
				    	String userId = (String) snapshot.getValue();
				    	callback.result(userId);
			    	}
			    }
			    @Override
			    public void onCancelled(FirebaseError firebaseError) {
			    	
			    }
			});
		} else {
			callback.result("facebook:" + user.getFacebookId());
		}		
	}
	
	
	
	
	public static void setUser(final User user, final Device device) {
		
		getFirebaseUserID(user, new CallbackFireUserUID() {

			@Override
			public void result(String uid) {
				insertUser(user, device, uid);
				FirebaseMigrator.setDevice(device, uid);
			}

			@Override
			public void doesNotExist() {
				createUser(user, device);
			}
			
		});	
	}
	
	private static void insertUser(User user, Device device, final String uid) {
		
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		if (!uid.equals(FIRE_NO_USER)) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("created", user.getCreationTime());
			data.put("email", user.getEmail());
			data.put("firstName", user.getFirstName());
			data.put("lastName", user.getLastName());
			data.put("language", device.getLanguage());
			data.put("country", device.getCountry());
			
			final Map dataFinal = data;
			ref.child(FIREBASE_USER + uid).setValue(dataFinal, new Firebase.CompletionListener() {
			    
				@Override
			    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
			        if (firebaseError != null) {
			        	logger.info("User " + uid + " could not be saved. " + firebaseError.getMessage());
			            ObjectMapper om = new ObjectMapper();
						try {
							logger.info(om.writeValueAsString(dataFinal));
						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        } else {
			        	logger.info("User " + uid + " saved successfully.");
			        }
				}
			});
		}
				
		// set user
		ref.child(FIREBASE_USERID + user.getId().toString()).setValue(uid);
		logger.info("Insert user " + uid + " to firebase!");	
	}
	
	public static void setDevice(final Device device) {
		
		getFirebaseUserID(device.getUser(), new CallbackFireUserUID() {

			@Override
			public void result(String userUid) {
				FirebaseMigrator.setDevice(device, userUid);
			}

			@Override
			public void doesNotExist() {
				FirebaseMigrator.setDevice(device, FIRE_NO_USER);
			}
		});	
	}
	
	public static void setDevice(final Device device, final String userUid) {
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		Firebase refDevice = ref.child(FIREBASE_DEVICE);
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("appVersion", device.getAppVersion());
		data.put("created", device.getCreationTime().getTime());
		data.put("model", convertModelName(device.getModel()));
		data.put("osVersion", device.getOsVersion());
		data.put("vendor", device.getVendor());
		data.put("uid", userUid);
		
		final Map<String, Object> dataFinal = data;
		
		
		// update device
		final String deviceUid = FirebasePushIdGenerator.generatePushId(device.getCreationTime(), device.getId());		
		refDevice.child(deviceUid).setValue(dataFinal, new Firebase.CompletionListener() {
		    
			@Override
		    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
		        if (firebaseError != null) {
		        	logger.info("Data " + deviceUid + " could not be saved. " + firebaseError.getMessage());
		            ObjectMapper om = new ObjectMapper();
					try {
						logger.info(om.writeValueAsString(dataFinal));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        } else {
		        	logger.info("Device " + deviceUid + " data saved successfully.");
		        	ObjectMapper om = new ObjectMapper();
					try {
						logger.info(om.writeValueAsString(dataFinal));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
			}
		});
		
		
//		logger.info("set device " + deviceUid + " to firebase!");
//		Iterator it = data.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry pair = (Map.Entry)it.next();
//			logger.info(pair.getKey() + " = " + pair.getValue());
//			it.remove(); // avoids a ConcurrentModificationException
//		}
	}
	
	
	public static void setSession(final MeasurementSession session) {
		
		updateFirebaseDataSession(session); // SEND TO LEGACY SUBSCRIPTION DATABASE
		
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		getFirebaseUserID(session.getDevice().getUser(), new CallbackFireUserUID() {

			@Override
			public void result(String userUid) {
				set(userUid);
			}

			@Override
			public void doesNotExist() {
				set(FIRE_NO_USER);
			}
			
			public void set(String userUid) {
				Firebase refSession = ref.child(FIREBASE_SESSION);
				GeoFire geoFireSessionClient = new GeoFire(ref.child(FIREBASE_GEO));
				
				final Map<String, Object> dataFinal = sessionToDict(session, userUid);
				
				final String sessionUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), session.getId());
				
				logger.info("sesion UID " + sessionUid + " session.getId() " + session.getId());
				
				refSession.child(sessionUid).setValue(dataFinal, new Firebase.CompletionListener() {
				    
					@Override
				    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
				        if (firebaseError != null) {
				        	logger.info("Data " + sessionUid + " could not be saved. " + firebaseError.getMessage());
				            ObjectMapper om = new ObjectMapper();
							try {
								logger.info(om.writeValueAsString(dataFinal));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        } else {
				        	logger.info("Session " + sessionUid + " saved successfully.");
				        }
					}
				});
				
				if (session.getPosition() != null) {
					geoFireSessionClient.setLocation(sessionUid, new GeoLocation(session.getPosition().getLatitude(), session.getPosition().getLongitude()));
				}
			}
		});	
	}
	
	public static Map<String,Object> sessionToDict(MeasurementSession session, String userUid) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("deviceKey", FirebasePushIdGenerator.generatePushId(session.getDevice().getCreationTime(), session.getDevice().getId()));
		data.put("timeEnd", session.getEndTime().getTime());
		data.put("timeStart", session.getStartTime().getTime());
		data.put("uid", userUid);
		if (session.getWindSpeedMax() != null && session.getWindSpeedMax() < 200) data.put("windMax", session.getWindSpeedMax());
		if (session.getWindSpeedAvg() != null && session.getWindSpeedAvg() < 200) data.put("windMean",session.getWindSpeedAvg());
		if (session.getWindMeter() == WindMeter.SLEIPNIR) data.put("windDirection", session.getWindDirection());
		if (session.getGustiness() != null && session.getGustiness() < 100) data.put("turbulence", session.getGustiness());
		
		String windmeter = "";
		switch (session.getWindMeter()) {
            case MJOLNIR:  
            	windmeter = "mjolnir";
                break;
            case SLEIPNIR:
            	windmeter = "sleipnir";
            	break;
            case UNKNOWN: // DOES NOT EXIST in the db
            	windmeter = "Unknown";  // WILL FAIL
            	break;
		}
		
		data.put("windMeter", windmeter);
		
		if (session.getPosition() != null) {
			Map<String, Object> location = new HashMap<String, Object>();
			location.put("lat", session.getPosition().getLatitude());
			location.put("lon", session.getPosition().getLongitude());
			if (session.getGeoLocationNameLocalized() != null && !session.getGeoLocationNameLocalized().equals("") && session.getPosition().getLatitude() != null) location.put("name",session.getGeoLocationNameLocalized());
			data.put("location", location);
		}
		
		Map<String, Object> sourced = new HashMap<String, Object>();
		sourced.put("humidity",session.getSourcedHumidity());
		sourced.put("humidity",session.getHumidity());
		if (session.getSourcedPressureGroundLevel() != null && session.getSourcedPressureGroundLevel() > 800 && session.getSourcedPressureGroundLevel() < 1200) sourced.put("pressure",session.getSourcedPressureGroundLevel()*100);
		if (session.getSourcedTemperature() != null && session.getSourcedTemperature() > 200) sourced.put("temperature",session.getSourcedTemperature());
		if (session.getTemperature() != null && session.getTemperature() > 200) sourced.put("temperature",session.getTemperature());
		
		sourced.put("windDirection",session.getSourcedWindDirection());
		sourced.put("windMean",session.getSourcedWindSpeedAvg());
		data.put("sourced", sourced);
		
		return data;
	}
	
		
	public static void deleteSession(final MeasurementSession session) {
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		getFirebaseUserID(session.getDevice().getUser(), new CallbackFireUserUID() {

			@Override
			public void result(String userUid) {
				set(userUid);
			}

			@Override
			public void doesNotExist() {
				set(FIRE_NO_USER);
			}
			
			public void set(String userUid) {
				Map<String, Object> data = sessionToDict(session, userUid);
				Firebase ref_session = ref.child(FIREBASE_SESSION);
				Firebase ref_session_deleted = ref.child(FIREBASE_SESSION_DELETED);
				Firebase ref_geo = ref.child(FIREBASE_GEO);
				
				String sessionUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), session.getId());
				
				ref_session.child(sessionUid).setValue(null);
				ref_geo.child(sessionUid).setValue(null);
				ref_session_deleted.child(sessionUid).setValue(data);
			}
		});	
	}
	
	
	public static void setPoint(MeasurementPoint point, MeasurementSession session) {
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		String sessionUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), session.getId());
		
		Map<String, Object> data = new HashMap<String, Object>();
		if (point.getWindDirection() != null && session.getWindMeter() == WindMeter.SLEIPNIR) { // old apps could upload direction on mjolnir
			data.put("direction", point.getWindSpeed());
		}
		data.put("sessionKey", sessionUid);
		data.put("speed",point.getWindSpeed());
		data.put("time", point.getTime().getTime());
		final Map<String, Object> dataFinal = data;
		
		final String pointUid = FirebasePushIdGenerator.generatePushId(point.getTime(), point.getId());
		
		ref.child(FIREBASE_WIND + pointUid).setValue(dataFinal, new Firebase.CompletionListener() {
		    
			@Override
		    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
		        if (firebaseError != null) {
		        	logger.info("Data " + pointUid + " could not be saved. " + firebaseError.getMessage());
		            ObjectMapper om = new ObjectMapper();
					try {
						logger.info(om.writeValueAsString(dataFinal));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        } else {
		        	logger.info("Point " + pointUid + " saved successfully.");
		        }
			}
		});
	}
	
	// not tested and used yet
	public static void deletePoints(MeasurementSession session) {
		final Firebase ref = getFirebase();
		if (getFirebase() == null) {return;}
		
		final String sessionUid = FirebasePushIdGenerator.generatePushId(session.getCreationTime(), session.getId());
		
		ref.child(FIREBASE_WIND).orderByChild("sessionKey").equalTo(sessionUid).addListenerForSingleValueEvent(new ValueEventListener() {
		    @Override
		    public void onDataChange(DataSnapshot snapshot) {
		    	Map<String, Object> data = new HashMap<String, Object>();
			
				for (DataSnapshot windSnapshot: snapshot.getChildren()) {
		            String key = windSnapshot.getKey();
		            System.out.println(" wind Key to be deleted " + key);
		            data.put(key, null);
		        }
				
				final Map<String, Object> dataFinal = data;
				
				ref.child(FIREBASE_WIND).updateChildren(dataFinal, new Firebase.CompletionListener() {
				    @Override
				    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
				        if (firebaseError != null) {
				        	logger.info("Winds for session " + sessionUid + " could not be deleted. " + firebaseError.getMessage());
				            ObjectMapper om = new ObjectMapper();
							try {
								logger.info(om.writeValueAsString(dataFinal));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        } else {
				        	logger.info("Winds for session " + sessionUid + " successfully deleted.");
				        }
					}
				});
		    }
		    @Override
		    public void onCancelled(FirebaseError firebaseError) {
		    }
		});
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
	
	public static String convertModelName(String model) {
		if (model.equals("Simulator")) {return "i386";}
		if (model.equals("iPhone2G")) {return "iPhone1,1";}
		if (model.equals("iPhone3G")) {return "iPhone1,2";}
		if (model.equals("iPhone3GS")) {return "iPhone2,1";}
		if (model.equals("iPhone4GSM")) {return "iPhone3,1";}
		if (model.equals("iPhone4GSMRevA")) {return "iPhone3,2";}
		if (model.equals("iPhone4GSM+CDMA")) {return "iPhone3,3";}
		if (model.equals("iPhone4S")) {return "iPhone4,1";}
		if (model.equals("iPhone5GSM")) {return "iPhone5,1";}
		if (model.equals("iPhone5GSM+CDMA")) {return "iPhone5,2";}
		if (model.equals("iPod1stGen")) {return "iPod1,1";}
		if (model.equals("iPod2ndGen")) {return "iPod2,1";}
		if (model.equals("iPod3rdGen")) {return "iPod3,1";}
		if (model.equals("iPod4thGen")) {return "iPod4,1";}
		if (model.equals("iPod5thGen")) {return "iPod5,1";}
		if (model.equals("iPadWiFi")) {return "iPad1,1";}
		if (model.equals("iPad3G")) {return "iPad1,2";}
		if (model.equals("iPad2WiFi")) {return "iPad2,1";}
		if (model.equals("iPad2GSM")) {return "iPad2,2";}
		if (model.equals("iPad2CDMA")) {return "iPad2,3";}
		if (model.equals("iPad2WiFiRevA")) {return "iPad2,4";}
		if (model.equals("ipad3WiFi")) {return "iPad3,1";}
		if (model.equals("ipad3GSM")) {return "iPad3,2";}
		if (model.equals("ipad3CDMA")) {return "iPad3,3";}
		if (model.equals("iPad4WiFi")) {return "iPad3,4";}
		if (model.equals("iPad4GSM")) {return "iPad3,5";}
		if (model.equals("iPad4GSM+CDMA")) {return "iPad3,6";}
		if (model.equals("iPadMini1GWiFi")) {return "iPad2,5";}
		if (model.equals("iPadMini1GGSM")) {return "iPad2,6";}
		if (model.equals("iPadMini1GGSM+CDMA")) {return "iPad2,7";}
		
		return model;
	}
	
	
}
