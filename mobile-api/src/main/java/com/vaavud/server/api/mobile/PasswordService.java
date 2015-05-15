package com.vaavud.server.api.mobile;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.api.util.PasswordUtil;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.User;

public class PasswordService extends AbstractJSONService<PasswordService.RequestParameters> {
    
    // password reset method inspired by http://stackoverflow.com/questions/2755708/password-reset-by-email-without-a-database-table
    
    private static final String URL = "http://vaavud.com/password";
    
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
            throw new ProtocolException("Process password with no action");
        }
        
        logger.info("Process object " + object);
        
        switch (object.action) {
        case "sendMail":
            writeJSONResponse(resp, mapper, sendMail(object, hibernateSession));
            break;
        case "setPassword":
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
            try {
//                Could not get                
//                Context initCtx = new InitialContext();
//                Context envCtx = (Context) initCtx.lookup("java:comp/env");
//                javax.mail.Session session = (javax.mail.Session) envCtx.lookup("mail/Session");
                
                // Recipient's email ID needs to be mentioned.
                String toAddress = email;//change accordingly

                // Sender's email ID needs to be mentioned
                String fromAddress = "hello@vaavud.com";//change accordingly

                final String username = "admin@vaavud.com";
                final String password = "Eftyoe,;45";
                Properties props = new Properties();
                // Assuming you are sending email through relay.jangosmtp.net
                String host = "smtp.gmail.com";

                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");
                // set any other needed mail.imap.* properties here
                
                javax.mail.Session session = javax.mail.Session.getInstance(props);
                
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromAddress));

                InternetAddress to[] = new InternetAddress[1];
                to[0] = new InternetAddress(toAddress);
                message.setRecipients(Message.RecipientType.TO, to);
           
                message.setSubject("Vaavud reset password");
                StringBuilder sb = new StringBuilder();
                sb.append("Press this link: ");
                sb.append(PasswordService.URL);
                sb.append("/newPassword");
                sb.append("?email=" + email);
                sb.append("&key=" + generateKey(user.getPasswordHash()));
                message.setContent(sb.toString(), "text/plain");
                
                Transport.send(message, username, password);

                
                
                logger.info("Sent email to " + toAddress + " successfully....");
                json.put("status", "EMAIL_SEND_SUCCESS");                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                    
        } else {
            logger.info("User with email does not exist");
            json.put("status", "INVALID_EMAIL");
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
            
            json.put("status", "NEW_PASSWORD_SET");
        }
        else {
            logger.info("key didn't match");
            json.put("status", "INVALID_RESET_KEY");
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
