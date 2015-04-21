package com.vaavud.server.api.mobile;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.AbstractJSONService;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.UnauthorizedException;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.ProductionQCSession;

public class ProductionQualityControlService extends AbstractJSONService<ProductionQCSession> {

    private static final Logger logger = Logger.getLogger(ProductionQualityControlService.class);
    
    @Override
    protected Class<ProductionQCSession> type() {
        return ProductionQCSession.class;
    }
    
    @Override
    protected boolean requiresAuthentication() {
        return false;
    }
    
    @Override
    protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, ProductionQCSession object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException {
        logger.info("ProductionQC service Call");
        if (object == null) {
            logger.info("Process object (null)");
        }
        else {
            logger.info("Process object " + object);
            
            if (object.getId() != null) {
                logger.warn("Received ProductionQCSession with ID, which should be a server-side only field. Setting it to null.");
                object.setId(null);
            }
            
            if (object.getVelocity() == null || object.getVelocity().isNaN()) {
                logger.error("Received ProductionQCSession with no Velocity");
                throw new ProtocolException("Received ProductionQCSession with no Velocity");
            }
            
            if (object.getVelocityTarget() == null || object.getVelocityTarget().isNaN()) {
                logger.error("Received ProductionQCSession with no VelocityTarget");
                throw new ProtocolException("Received ProductionQCSession with no VelocityTarget");
            }
            
            if (object.getVelocityProfileError() == null || object.getVelocityProfileError().isNaN()) {
                logger.error("Received ProductionQCSession with no VelocityProfileError");
                throw new ProtocolException("Received ProductionQCSession with no VelocityProfileError");
            }
            
            if (object.getDirection() == null || object.getDirection().isNaN()) {
                logger.error("Received ProductionQCSession with no Direction");
                throw new ProtocolException("Received ProductionQCSession with no Direction");
            }
            
            if (object.getTickDetectionErrorCount() == null) {
                logger.error("Received ProductionQCSession with no TickDetectionErrorCount");
                throw new ProtocolException("Received ProductionQCSession with no TickDetectionErrorCount");
            }
            
            if (object.getVelocityProfile() == null) {
                logger.error("Received ProductionQCSession with no velocityProfile");
                throw new ProtocolException("Received ProductionQCSession with no velocityProfile");
            }
            
            if (object.getQcPassed() == null) {
                logger.error("Received ProductionQCSession with no QcPassed");
                throw new ProtocolException("Received ProductionQCSession with no QcPassed");
            }

            String seriveAuthToken = "gvasidyfgaisudyfgoauysgdf";            
            String httpAuthToken = req.getHeader("authToken");
            if (httpAuthToken == null || httpAuthToken.trim().isEmpty()) {
                throw new UnauthorizedException();
            }
            if (!httpAuthToken.equals(seriveAuthToken)) {
                throw new UnauthorizedException();
            }
            
            hibernateSession.beginTransaction();
            hibernateSession.save(object);
            hibernateSession.getTransaction().commit();
        }
    }
    
    @Override
    protected Logger getLogger() {
        return logger;
    }
}
