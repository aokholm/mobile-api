package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.loader.custom.Return;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.mobile.DeleteMeasurementService.RequestParameters;
import com.vaavud.server.api.util.PasswordUtil;
import com.vaavud.server.api.util.ServiceUtil;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.User;
import com.vaavud.server.model.phone.PhoneModel;
import com.vaavud.util.UUIDUtil;

public class PasswordService extends AbstractJSONService<PasswordService.RequestParameters> {

    private static final Logger logger = Logger.getLogger(PasswordService.class);

    public static class RequestParameters implements Serializable {
        private String email;
        private String key;
        private String clientPasswordHash;
        private String action;

        public String getEmail() throws ProtocolException {
            if (email == null || email.isEmpty()) {
                logger.warn("Password request with no email");
                throw new ProtocolException("Password request with no email");
            } else {
                return email;
            }
        }
        
        public void setEmail(String email) {
            this.email = email;
        }

        public String getKey() throws ProtocolException {
            if (key == null || key.isEmpty()) {
                logger.warn("Password request with no key");
                throw new ProtocolException("Password request with no key");
            } else {
                return key;
            }
        }

        public void setKey(String key) {
            this.key = key;
        }
        
        public String getClientPasswordHash() throws ProtocolException {
            if (clientPasswordHash == null || clientPasswordHash.isEmpty()) {
                logger.warn("Password request with no clientPasswordHash");
                throw new ProtocolException("Password request with no clientPasswordHash");
            } else {
                return clientPasswordHash;
            }
        }

        public void setClientPasswordHash(String clientPasswordHash) {
            this.clientPasswordHash = clientPasswordHash;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return "RequestParameters [email=" + email + ", key=" + key + ", clientPasswordHash=" + clientPasswordHash + ", action="
                    + action + "]";
        }

    }

    @Override
    protected Class<PasswordService.RequestParameters> type() {
        return RequestParameters.class;
    }

    @Override
    protected boolean requiresAuthentication() {
        return false;
    }

    @Override
    protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice,
            RequestParameters object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException,
            ProtocolException, IOException {
        if (object == null) {
            logger.info("Process object (null)");
            return;
        }
        if (object.getAction() == null) {
            logger.info("Process password with no action");
            return;
        }
        
        logger.info("Process object " + object);
        
        switch (object.action) {
        case "sendMail":
            writeJSONResponse(resp, mapper, sendMail(object, hibernateSession));
            break;
        case "SetPassword":
            writeJSONResponse(resp, mapper, setPassword(object, hibernateSession));
        default:
            break;
        }
    }

    public Map<String, Object> sendMail(RequestParameters parameters, Session hibernateSession) throws ProtocolException {
        
        // make sure we have the required parameters
        String email = parameters.getEmail();
        
        hibernateSession.beginTransaction();

        final User user = (User) hibernateSession.createQuery("from User where email=:email")
                .setString("email", email).uniqueResult();

        hibernateSession.getTransaction().commit();

        Map<String, Object> json = new HashMap<String, Object>();
        
        if (user != null) {
            json.put("action", "SendEmail hash(hash(pass)): " + generateKey(user.getPasswordHash()));
            json.put("key", generateKey(user.getPasswordHash()));
            

//            String host = "smtp.gmail.com";
//            String username = "user";
//            String password = "passwd";
//            Properties props = new Properties();
//            props.setProperty("mail.smtp.ssl.enable", "true");
//            // set any other needed mail.smtp.* properties here
//            Session session = Session.getInstance(props);
//            MimeMessage msg = new MimeMessage(session);
//            // set the message content here
//            Transport.send(msg, username, password);
            
            
        } else {
            logger.info("User with email does not exist");
            json.put("action", "no Action - user does not exist");
        }
        return json;
    }
    
    public Map<String, Object> setPassword(RequestParameters parameters, Session hibernateSession) throws ProtocolException {
        // make sure we have the required parameters
        String email = parameters.getEmail();
        String key = parameters.getKey();
        String clientPasswordHash = parameters.getClientPasswordHash();
        
        hibernateSession.beginTransaction();

        User user = (User) hibernateSession.createQuery("from User where email=:email")
                .setString("email", email).uniqueResult();

        Map<String, Object> json = new HashMap<String, Object>();
        
        if (checkKey(user.getPasswordHash(), key)) {
            logger.info("Creating setting password given client hash " + clientPasswordHash);
            user.setPasswordHash(PasswordUtil.createHash(clientPasswordHash));
            
            json.put("action", "resets password :)");
        }
        else {
            logger.info("key didn't match");
            json.put("action", "key didn't match");
        }
        
        hibernateSession.getTransaction().commit();
        return json;
    }
    
    private boolean checkKey(String passwordHash, String key) {
        // check that the key was generated within the last hour or the hour before that. 
        for (int i = 0; i < 2; i++) {
            if (MD5(passwordHash + Long.toString(UTCHours()-i) ).equals(key)) {
                return true;
            }
        }
        return false;
    }
    
    private String generateKey(String passwordHash) {
        return MD5(passwordHash + UTCHours().toString());
    }
    
    private Long UTCHours() {
        Date date = new Date();
        return date.getTime() / (1000*60*60);
    }
    
    private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
    
    @Override
    protected Logger getLogger() {
        return logger;
    }
}
