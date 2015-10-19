package com.vaavud.server.migration;

import java.util.Map;

import org.apache.log4j.Logger;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.User;

interface DeviceMigratorDelegate {
	void completed(long deviceID);
}

public class DeviceMigrator {
	private static final Logger logger = Logger.getLogger(EchoServer.class);
	
	Firebase ref = new Firebase("https://vaavud-migration.firebaseio.com/");
	
	DeviceMigratorDelegate delegate;
	
	DeviceMigrator(DeviceMigratorDelegate delegate) {
		this.delegate = delegate;
	}
	
	public void processDevice(long deviceID) {
		logger.info("opened hibernated");
		org.hibernate.Session hibernateSession = Model.get().getSessionFactory().openSession();
		try {
			Device device = (Device) hibernateSession.createQuery("from Device where id=:id").setLong("id", deviceID).uniqueResult();
			if (device == null) { delegate.completed(deviceID); return; }
			
			User user = device.getUser();
			if (user == null) { delegate.completed(deviceID); return; }
			
			String userPass = user.getEmail() + String.valueOf(user.getId());
			
			ref.createUser(user.getEmail(), userPass, new Firebase.ValueResultHandler<Map<String, Object>>() {
			    private long deviceID;
				
				@Override
			    public void onSuccess(Map<String, Object> result) {
			        logger.info("Successfully created user account with uid: " + result.get("uid"));
			        delegate.completed(deviceID);
			    }
			    @Override
			    public void onError(FirebaseError firebaseError) {
			        // there was an error
			    	logger.info("Failded deviceID: " + String.valueOf(deviceID) + " " + firebaseError.getMessage());
			    	delegate.completed(deviceID);
			    }
			    
			    private Firebase.ValueResultHandler<Map<String, Object>> init(long deviceID){
			    	this.deviceID = deviceID;
			        return this;
			    }
			    
			}.init(deviceID) );
			
		} catch (Exception e) {
			logger.error("Error processing service " + getClass().getName(), e);
			
		} finally {
			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
				hibernateSession.getTransaction().rollback();
			}
			hibernateSession.close();
			logger.info("closed hibernated");
		}
	}
	
	
	
	
//	
//	public String user2json(User user) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("created", user.getCreationTime().getTime() );
//		map.put("email", user.getEmail());
//		map.put("deleted", user.isDeleted());
//				
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//	    return json.toString();
//	}
//	
//	public String device2json(Device device) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("created", device.getCreationTime().getTime() );
//		map.put("vendor", device.getVendor());
//		map.put("model", converModelNames(device.getModel()));
//		map.put("version", device.getAppVersion());
//		
//		if (device.getUser() != null) {
//			map.put("userKey", device.getUser().getUserKey());
//		}
//	
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//	    return json.toString();
//	}
//	
//	public String measurement2json(MeasurementSession measurement) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("timeStart", measurement.getStartTime().getTime() );
//		map.put("windMean", measurement.getWindSpeedAvg() );
//		map.put("deviceKey", measurement.getDevice().getDeviceKey());
//		
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//	    return json.toString();
//	}
//	
//	public String point2json(MeasurementPoint point) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("time", point.getTime().getTime() );
//		map.put("speed", point.getWindSpeed() );
//		map.put("direction", point.getWindDirection() );
//		map.put("sessionKey", point.getSession().getSessionKey());
//		
//		JSONObject json = new JSONObject();
//	    json.putAll( map );
//		
//	    return json.toString();
//	}
//
//	public String converModelNames(String model) {
//		if (model.equals("Simulator")) {return "i386";}
//		if (model.equals("iPhone2G")) {return "iPhone1,1";}
//		if (model.equals("iPhone3G")) {return "iPhone1,2";}
//		if (model.equals("iPhone3GS")) {return "iPhone2,1";}
//		if (model.equals("iPhone4GSM")) {return "iPhone3,1";}
//		if (model.equals("iPhone4GSMRevA")) {return "iPhone3,2";}
//		if (model.equals("iPhone4GSM+CDMA")) {return "iPhone3,3";}
//		if (model.equals("iPhone4S")) {return "iPhone4,1";}
//		if (model.equals("iPhone5GSM")) {return "iPhone5,1";}
//		if (model.equals("iPhone5GSM+CDMA")) {return "iPhone5,2";}
//		if (model.equals("iPod1stGen")) {return "iPod1,1";}
//		if (model.equals("iPod2ndGen")) {return "iPod2,1";}
//		if (model.equals("iPod3rdGen")) {return "iPod3,1";}
//		if (model.equals("iPod4thGen")) {return "iPod4,1";}
//		if (model.equals("iPod5thGen")) {return "iPod5,1";}
//		if (model.equals("iPadWiFi")) {return "iPad1,1";}
//		if (model.equals("iPad3G")) {return "iPad1,2";}
//		if (model.equals("iPad2WiFi")) {return "iPad2,1";}
//		if (model.equals("iPad2GSM")) {return "iPad2,2";}
//		if (model.equals("iPad2CDMA")) {return "iPad2,3";}
//		if (model.equals("iPad2WiFiRevA")) {return "iPad2,4";}
//		if (model.equals("ipad3WiFi")) {return "iPad3,1";}
//		if (model.equals("ipad3GSM")) {return "iPad3,2";}
//		if (model.equals("ipad3CDMA")) {return "iPad3,3";}
//		if (model.equals("iPad4WiFi")) {return "iPad3,4";}
//		if (model.equals("iPad4GSM")) {return "iPad3,5";}
//		if (model.equals("iPad4GSM+CDMA")) {return "iPad3,6";}
//		if (model.equals("iPadMini1GWiFi")) {return "iPad2,5";}
//		if (model.equals("iPadMini1GGSM")) {return "iPad2,6";}
//		if (model.equals("iPadMini1GGSM+CDMA")) {return "iPad2,7";}
//		
//		return model;
//	}
}
