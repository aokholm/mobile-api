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
import com.vaavud.server.api.util.PasswordUtil;
import com.vaavud.server.api.mobile.RegisterUserService.Input;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.User;

public class RegisterUserService extends AbstractJSONService<Input> {

	private static final Logger logger = Logger.getLogger(RegisterUserService.class);
			
	@Override
	protected Class<Input> type() {
		return Input.class;
	}

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, Input object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
		if (object == null) {
			logger.info("Process object (null)");
		}
		else {
			logger.info("Process object " + object);
			
			if (object.getEmail() == null || object.getEmail().isEmpty()) {
				logger.error("Received register user input without email");
				throw new ProtocolException("Received register user input without email");
			}
			
			if ((object.getClientPasswordHash() == null || object.getClientPasswordHash().isEmpty()) && (object.getFacebookId() == null || object.getFacebookId().isEmpty())) {
				logger.error("Received register user input without client password hash or facebook hash");
				throw new ProtocolException("Received register user input without client password hash or facebook hash");
			}

			hibernateSession.beginTransaction();

			String authToken;

			final User authenticatedUser;
			User existingUser = (User) hibernateSession
					.createQuery("from User where email=:email")
					.setString("email", object.getEmail())
					.uniqueResult();

			if (authenticatedDevice == null) {
				logger.error("Received register user input with no authenticated device");
				throw new ProtocolException("Received register user input with no authenticated device");
			}

			if (existingUser != null) {

				// existing user...
				
				boolean isAuthenticated = false;

				if (object.getClientPasswordHash() != null && object.getClientPasswordHash().length() > 0) {				
					if (existingUser.getPasswordHash() != null && PasswordUtil.validatePassword(object.getClientPasswordHash(), existingUser.getPasswordHash())) {
						logger.info("Password hash is matching given client hash " + object.getClientPasswordHash());
						isAuthenticated = true;
					}	
				}
				else {
					// TODO: Facebook validation
				}
				
				if (isAuthenticated) {
					authenticatedUser = existingUser;
				}
				else {
					logger.info("Credentials not matching, sending INVALID");
					Map<String,Object> json = new HashMap<String,Object>();
					json.put("status", "INVALID");
					writeJSONResponse(resp, mapper, json);
					return;
				}
			}
			else {
				
				// new user...
				
				User user = new User();
				user.setEmail(object.getEmail());
				user.setFirstName(object.getFirstName());
				user.setLastName(object.getLastName());
				
				if (object.getClientPasswordHash() != null && object.getClientPasswordHash().length() > 0) {
					logger.info("Creating new user given client hash " + object.getClientPasswordHash());
					user.setPasswordHash(PasswordUtil.createHash(object.getClientPasswordHash()));
				}
				else {
					user.setFacebookId(object.getFacebookId());
					
					// TODO: register facebook hash
				}
				
				hibernateSession.save(user);

				authenticatedUser = user;
			}

			// verify match between device and user...
			
			if (authenticatedDevice.getUser() == null) {				

				// no user owns this device yet, so make the authenticated user own it
				authenticatedDevice.setUser(authenticatedUser);
			}
			else if (authenticatedDevice.getUser().getId() != authenticatedUser.getId()) {
				
				// this device already belongs to another user, which shouldn't happen
				logger.error("Device (" + authenticatedDevice.getId() + ") already belongs to another user (" + authenticatedDevice.getUser().getId() + ") and not authenticated user (" + authenticatedUser.getId() + ")");
				throw new UnauthorizedException();
			}
			
			// use authToken from device			
			authToken = authenticatedDevice.getAuthToken();			

			// commit
			hibernateSession.getTransaction().commit();
			hibernateSession.beginTransaction();

			// validate authToken
			if (authToken == null || authToken.trim().isEmpty()) {
				logger.error("AuthToken not supposed to be null or empty here");
				throw new IllegalStateException();
			}
			
			Map<String,Object> json = new HashMap<String,Object>();
			json.put("authToken", authToken);
			json.put("status", (existingUser == null) ? "CREATED" : "PAIRED");
			json.put("userId", authenticatedUser.getId());
			writeJSONResponse(resp, mapper, json);
		}
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	public static class Input {
		
		private String email;
		private String clientPasswordHash;
		private String facebookId;
		private String facebookAccessToken;
		private String firstName;
		private String lastName;
		
		public String getEmail() {
			return email;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
		public String getClientPasswordHash() {
			return clientPasswordHash;
		}
		
		public void setClientPasswordHash(String clientPasswordHash) {
			this.clientPasswordHash = clientPasswordHash;
		}
		
		public String getFacebookId() {
			return facebookId;
		}
		
		public void setFacebookId(String facebookId) {
			this.facebookId = facebookId;
		}
		
		public String getFacebookAccessToken() {
			return facebookAccessToken;
		}

		public void setFacebookAccessToken(String facebookAccessToken) {
			this.facebookAccessToken = facebookAccessToken;
		}

		public String getFirstName() {
			return firstName;
		}
		
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@Override
		public String toString() {
			return "Input [email=" + email + ", clientPasswordHash="
					+ clientPasswordHash + ", facebookId=" + facebookId
					+ ", facebookAccessToken=" + facebookAccessToken
					+ ", firstName=" + firstName + ", lastName=" + lastName
					+ "]";
		}
	}
}
