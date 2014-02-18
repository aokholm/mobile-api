package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.util.EmailUtil;
import com.vaavud.server.api.util.FacebookUtil;
import com.vaavud.server.api.util.PasswordUtil;
import com.vaavud.server.api.mobile.RegisterUserService.Input;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.Gender;
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

			if (object.getAction() == null || object.getAction().isEmpty()) {
				logger.error("Received register user input without action");
				throw new ProtocolException("Received register user input without action");
			}

			if (object.getEmail() == null || object.getEmail().isEmpty()) {
				logger.error("Received register user input without email");
				throw new ProtocolException("Received register user input without email");
			}
			
			if ((object.getClientPasswordHash() == null || object.getClientPasswordHash().isEmpty())
					&& (object.getFacebookAccessToken() == null || object.getFacebookAccessToken().isEmpty() || object.getFacebookId() == null || object.getFacebookId().isEmpty())) {
				logger.error("Received register user input without client password hash or facebook access token");
				throw new ProtocolException("Received register user input without client password hash or facebook access token");
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
				
				if (object.getClientPasswordHash() != null && object.getClientPasswordHash().length() > 0) {				
					if (existingUser.getPasswordHash() != null && PasswordUtil.validatePassword(object.getClientPasswordHash(), existingUser.getPasswordHash())) {
						logger.info("Password hash is matching given client hash " + object.getClientPasswordHash());
					}	
					else {
						logger.info("Credentials not matching, sending INVALID_CREDENTIALS");
						Map<String,Object> json = new HashMap<String,Object>();
						json.put("status", "INVALID_CREDENTIALS");
						writeJSONResponse(resp, mapper, json);
						return;
					}
				}
				else if (object.getFacebookAccessToken() != null && object.getFacebookAccessToken().length() > 0 && object.getFacebookId() != null && object.getFacebookId().length() > 0) {
					if (verifyFacebookAccount(object.getFacebookAccessToken(), object.getFacebookId())) {
						logger.info("Facebook account verified for Facebook user ID=" + object.getFacebookId());
					}
					else {
						logger.info("Facebook account verification failed for Facebook user ID=" + object.getFacebookId());
						Map<String,Object> json = new HashMap<String,Object>();
						json.put("status", "INVALID_ACCESS_TOKEN");
						writeJSONResponse(resp, mapper, json);
						return;
					}
				}
				else {
					logger.error("Should never end here");
					throw new IllegalStateException();
				}
				
				authenticatedUser = existingUser;
			}
			else if ("LOGIN".equals(object.getAction())) {
		
				// user doesn't exist and login is intended
				
				logger.info("User doesn't exist for login, sending INVALID_CREDENTIALS");
				Map<String,Object> json = new HashMap<String,Object>();
				json.put("status", "INVALID_CREDENTIALS");
				writeJSONResponse(resp, mapper, json);
				return;
			}
			else {
				
				// new user...
				
				if (!EmailUtil.isValid(object.getEmail())) {
					Map<String,Object> json = new HashMap<String,Object>();
					json.put("status", "MALFORMED_EMAIL");
					writeJSONResponse(resp, mapper, json);
					return;
				}
				
				User user = new User();
				user.setEmail(object.getEmail());
				user.setFirstName(object.getFirstName());
				user.setLastName(object.getLastName());
				
				if (object.getClientPasswordHash() != null && object.getClientPasswordHash().length() > 0) {
					logger.info("Creating new user given client hash " + object.getClientPasswordHash());
					user.setPasswordHash(PasswordUtil.createHash(object.getClientPasswordHash()));
				}
				else if (object.getFacebookAccessToken() != null && object.getFacebookAccessToken().length() > 0 && object.getFacebookId() != null && object.getFacebookId().length() > 0) {
					if (verifyFacebookAccount(object.getFacebookAccessToken(), object.getFacebookId())) {
						logger.info("Creating new user given Facebook access token for Facebook user ID=" + object.getFacebookId());
						user.setFacebookId(object.getFacebookId());
					}
					else {
						logger.info("Facebook account verification failed for Facebook user ID=" + object.getFacebookId());
						Map<String,Object> json = new HashMap<String,Object>();
						json.put("status", "INVALID_ACCESS_TOKEN");
						writeJSONResponse(resp, mapper, json);
						return;
					}
				}
				else {
					logger.error("Should never end here");
					throw new IllegalStateException();
				}
		
				hibernateSession.save(user);

				authenticatedUser = user;
			}

			if (authenticatedUser.getGender() == Gender.UNKNOWN && object.getGender() != null && object.getGender() != Gender.UNKNOWN) {
				authenticatedUser.setGender(object.getGender());
			}
			
			if (!authenticatedUser.isVerified() && object.getVerified()) {
				authenticatedUser.setVerified(true);
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
			json.put("email", authenticatedUser.getEmail());
			json.put("firstName", authenticatedUser.getFirstName());
			json.put("lastName", authenticatedUser.getLastName());
			writeJSONResponse(resp, mapper, json);
		}
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
	
	private boolean verifyFacebookAccount(String accessToken, String facebookUserId) {
		try {
			FacebookClient facebookClient = new DefaultFacebookClient(accessToken, FacebookUtil.APP_SECRET);
			com.restfb.types.User user = facebookClient.fetchObject("me", com.restfb.types.User.class);
			return facebookUserId.equals(user.getId());
		}
		catch (RuntimeException e) {
			getLogger().error("Error verifying Facebook account", e);
			return false;
		}
	}
	
	public static class Input {
		
		private String action;
		private String email;
		private String clientPasswordHash;
		private String facebookId;
		private String facebookAccessToken;
		private String firstName;
		private String lastName;
		private Gender gender;
		private Boolean verified;
		
		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

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
		
		public Gender getGender() {
			return gender;
		}

		public void setGender(Gender gender) {
			this.gender = gender;
		}

		public Boolean getVerified() {
			return verified;
		}

		public void setVerified(Boolean verified) {
			this.verified = verified;
		}

		@Override
		public String toString() {
			return "Input [action=" + action + ", email=" + email + ", clientPasswordHash="
					+ clientPasswordHash + ", facebookId=" + facebookId
					+ ", facebookAccessToken=" + facebookAccessToken
					+ ", firstName=" + firstName + ", lastName=" + lastName
					+ ", gender=" + (gender == null ? "null" : gender.name())
					+ ", verified=" + verified
					+ "]";
		}
	}
}
