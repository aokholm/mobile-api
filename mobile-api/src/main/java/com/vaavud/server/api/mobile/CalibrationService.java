package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.util.Date;
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
import com.vaavud.server.api.util.json.DeviceByUUIDModule;
import com.vaavud.server.model.entity.CalibrationFile;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.User;
import com.vaavud.server.model.phone.PhoneModel;
import com.vaavud.util.UUIDUtil;

public class CalibrationService extends AbstractJSONService<CalibrationFile> {

	private static final Logger logger = Logger.getLogger(CalibrationService.class);
	
	@Override
	protected Class<CalibrationFile> type() {
		return CalibrationFile.class;
	}
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, CalibrationFile object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		logger.info("Calibration service Call");
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);
			
			if (object.getId() != null) {
				logger.warn("Received Device with ID, which should be a server-side only field. Setting it to null.");
				object.setId(null);
			}
			
			if (object.getDeviceUuid() == null || object.getDeviceUuid().isEmpty()) {
				logger.error("Received Device with no UUID");
				throw new ProtocolException("Received Device with no uuid");
			}
			
			if (object.getS3FileName() == null && object.isValidFileName()) {
				logger.error("File name is not valid. : Manufacturer_DeviceName_DeviceUUID_HumanReadableTimeStamp[DDMMYY_HHMMSS]) " + object.getS3FileName());
				throw new ProtocolException("Received Device with no uuid");
			}
			
			if (object.getCalibrationCoefficients()==null){
				logger.error("Received Device with no UUID");
				throw new ProtocolException("Received Device with no uuid");
			}

			String authToken = null;
			
			hibernateSession.beginTransaction();

			Device storedDevice = (Device) hibernateSession
					.createQuery("from Device where uuid=:uuid")
					.setString("uuid", object.getDeviceUuid())
					.uniqueResult();

			if (storedDevice == null) {
				logger.error("Received Device with no UUID");
				throw new ProtocolException("Received Device not registered");
			}
			else {
				
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
			}
			
			if (authToken == null || authToken.trim().isEmpty()) {
				logger.error("AuthToken not supposed to be null or empty here");
			}
			
			hibernateSession.save(object);
			hibernateSession.getTransaction().commit();
			
//			PhoneModel phoneModel = PhoneModel.getPhoneModel(device.getOs(), device.getModel());
//			
//			Map<String,Object> json = new HashMap<String,Object>();
//			json.put("authToken", authToken);
//			json.put("uploadMagneticData", device.getUploadMagneticData() == null ? "true" : (device.getUploadMagneticData() ? "true" : "false"));
//			json.put("algorithm", phoneModel.getAlgorithm().name());
//			json.put("frequencyStart", Double.toString(phoneModel.getFrequencyStart()));
//			json.put("frequencyFactor", Double.toString(phoneModel.getFrequencyFactor()));
//			json.put("fftLength", Integer.toString(phoneModel.getFFTLength(device.getOsVersion())));
//			json.put("fftDataLength", Integer.toString(phoneModel.getFFTDataLength(device.getOsVersion())));
//			json.put("hourOptions", HOUR_OPTIONS);
//			json.put("creationTime", device.getCreationTime());
//			json.put("validAgricultureSubscription", validAgricultureSubscription);
//			json.put("enableShareFeature", true);
//			json.put("enableMixpanelPeople", ENABLE_MIXPANEL_PEOPLE);
//			
//			// note: prior to iOS app version 1.1.4, the code expected this value as a string and
//			// will crash if it is a boolean
//			if (device.isIOS() && device.isAppVersionLessThan("1.1.4")) {
//				json.put("enableMixpanel", ENABLE_MIXPANEL ? "true" : "false");
//			}
//			else {
//				json.put("enableMixpanel", ENABLE_MIXPANEL);
//			}
//			
//			//json.put("maxMapMarkers", MAX_MAP_MARKERS);
//			writeJSONResponse(resp, mapper, json);
		}
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
}
