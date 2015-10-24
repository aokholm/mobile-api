package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.User;
import com.vaavud.server.model.migration.FirebaseMigrator;
import com.vaavud.server.model.phone.PhoneModel;
import com.vaavud.util.UUIDUtil;

public class RegisterDeviceService extends AbstractJSONService<Device> {

	private static final Logger logger = Logger.getLogger(RegisterDeviceService.class);
	
	private static final float[] HOUR_OPTIONS = new float[] {3F, 6F, 12F, 24F};
	private static final boolean ENABLE_MIXPANEL = true;
	private static final boolean ENABLE_MIXPANEL_PEOPLE = true;
	
	
	@Override
	protected Class<Device> type() {
		return Device.class;
	}

	@Override
	protected boolean requiresAuthentication() {
		return false;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, Device object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);
			
			if (object.getId() != null) {
				logger.warn("Received Device with ID, which should be a server-side only field. Setting it to null.");
				object.setId(null);
			}
			
			if (object.getUuid() == null || object.getUuid().isEmpty()) {
				logger.error("Received Device with no UUID");
				throw new ProtocolException("Received Device with no uuid");
			}
			
			if (object.getCountry() != null && object.getCountry().length() > 2) {
				logger.warn("Got country code that is longer than 2 characters: " + object.getCountry());
			}

			String authToken = null;
			
			hibernateSession.beginTransaction();

			final Device device;
			Device storedDevice = (Device) hibernateSession
					.createQuery("from Device where uuid=:uuid")
					.setString("uuid", object.getUuid())
					.uniqueResult();

			if (storedDevice == null) {
				logger.info("Registering new Device");
				authToken = UUIDUtil.generateUUID();
				object.setAuthToken(authToken);
				object.setUploadMagneticData(null);
				
				hibernateSession.save(object);
				hibernateSession.getTransaction().commit();
				
				device = object;
				
				FirebaseMigrator.setDevice(device); // FIREBASE
			}
			else {
				
				// existing device requires a matching authToken sent as HTTP header
				// note: for backwards compatibility, we only require this if a user is
				// associated with the device, since older apps won't be able to handle
				// an unauthorized response here
				
				if (storedDevice.getUser() != null) {
					String httpAuthToken = req.getHeader("authToken");
					if (httpAuthToken == null || httpAuthToken.trim().isEmpty()) {
						throw new UnauthorizedException();
					}
					if (!httpAuthToken.equals(storedDevice.getAuthToken())) {
						throw new UnauthorizedException();
					}
				}
				
				authToken = storedDevice.getAuthToken();
				
				if (!storedDevice.equalValues(object)) {
					logger.info("Received Device already stored and values have changed");
					storedDevice.setFrom(object);
					hibernateSession.getTransaction().commit();
					FirebaseMigrator.setDevice(storedDevice); // FIREBASE
				}
				else {
					logger.info("Received Device already stored and no changes in values");
				}
				
				device = storedDevice;
			}
			
			if (authToken == null || authToken.trim().isEmpty()) {
				logger.error("AuthToken not supposed to be null or empty here");
			}
			
			boolean validAgricultureSubscription = false;
			if (device.getUser() != null) {
				User deviceUser = device.getUser();
				if (deviceUser.isValidAgricultureSubscription()) {
					validAgricultureSubscription = true;
				}
			}
			
			PhoneModel phoneModel = PhoneModel.getPhoneModel(device.getOs(), device.getModel());
			
			Map<String,Object> json = new HashMap<String,Object>();
			json.put("authToken", authToken);
			json.put("uploadMagneticData", device.getUploadMagneticData() == null ? "true" : (device.getUploadMagneticData() ? "true" : "false"));
			json.put("algorithm", phoneModel.getAlgorithm().name());
			json.put("frequencyStart", Double.toString(phoneModel.getFrequencyStart()));
			json.put("frequencyFactor", Double.toString(phoneModel.getFrequencyFactor()));
			json.put("fftLength", Integer.toString(phoneModel.getFFTLength(device.getOsVersion())));
			json.put("fftDataLength", Integer.toString(phoneModel.getFFTDataLength(device.getOsVersion())));
			json.put("hourOptions", HOUR_OPTIONS);
			json.put("creationTime", device.getCreationTime());
			json.put("validAgricultureSubscription", validAgricultureSubscription);
			json.put("enableShareFeature", true);
			json.put("enableMixpanelPeople", ENABLE_MIXPANEL_PEOPLE);
			
			// note: prior to iOS app version 1.1.4, the code expected this value as a string and
			// will crash if it is a boolean
			if (device.isIOS() && device.isAppVersionLessThan("1.1.4")) {
				json.put("enableMixpanel", ENABLE_MIXPANEL ? "true" : "false");
			}
			else {
				json.put("enableMixpanel", ENABLE_MIXPANEL);
			}
			
			//json.put("maxMapMarkers", MAX_MAP_MARKERS);
			writeJSONResponse(resp, mapper, json);
		}
	}

	
	@Override
	protected Logger getLogger() {
		return logger;
	}
}
