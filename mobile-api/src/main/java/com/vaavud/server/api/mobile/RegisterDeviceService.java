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
import com.vaavud.server.api.util.ServiceUtil;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.phone.PhoneModel;
import com.vaavud.util.UUIDUtil;

public class RegisterDeviceService extends AbstractJSONService<Device> {

	private static final Logger logger = Logger.getLogger(RegisterDeviceService.class);
	private static final float[] HOUR_OPTIONS = new float[] {3F, 6F, 12F, 24F};
	private static final int MAX_MAP_MARKERS = 500;
			
	@Override
	protected Class<Device> type() {
		return Device.class;
	}

	@Override
	protected boolean requiresAuthentication() {
		return false;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
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
			}
			else {
				authToken = storedDevice.getAuthToken();

				if (!storedDevice.equalValues(object)) {
					logger.info("Received Device already stored and values have changed");
					storedDevice.setFrom(object);
					hibernateSession.getTransaction().commit();					
				}
				else {
					logger.info("Received Device already stored and no changes in values");
				}
				
				device = storedDevice;
			}
			
			if (authToken == null || authToken.trim().isEmpty()) {
				logger.error("AuthToken not supposed to be null or empty here");
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
			json.put("maxMapMarkers", MAX_MAP_MARKERS);
			writeJSONResponse(resp, mapper, json);
		}
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
}
