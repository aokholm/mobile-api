package com.vaavud.server.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.vaavud.server.api.util.ServiceUtil;
import com.vaavud.server.model.Model;
import com.vaavud.server.model.entity.Device;

public abstract class AbstractHibernateService extends AbstractService {

	@Override
	protected final void process(HttpServletRequest req, HttpServletResponse resp) throws IOException { 
		Session hibernateSession = Model.get().getSessionFactory().openSession();
		try {
			Device device = null;

			if (requiresAuthentication()) {
				String authToken = req.getHeader("authToken");
				getLogger().info("Got authToken:" + authToken);
				if (authToken == null || authToken.trim().isEmpty()) {
					throw new UnauthorizedException();
				}
				device = (Device) hibernateSession.createQuery("from Device where authToken=:authToken").setString("authToken", authToken.trim()).uniqueResult();
				if (device == null) {
					throw new UnauthorizedException();
				}
			}
			
			process(req, resp, device, hibernateSession);
		}
		catch (RuntimeException e) {
			getLogger().error("Error processing service " + getClass().getName(), e);
			ServiceUtil.sendInternalServerErrorResponse(resp);
		}
		catch (UnauthorizedException e) {
			getLogger().error("Error processing service " + getClass().getName(), e);
			ServiceUtil.sendUnauthorizedErrorResponse(resp);
		}
		catch (IOException e) {
			getLogger().error("Error processing service " + getClass().getName(), e);
			ServiceUtil.sendInternalServerErrorResponse(resp);
		}
		finally {
			if (hibernateSession.getTransaction() != null && hibernateSession.getTransaction().isActive()) {
				hibernateSession.getTransaction().rollback();
			}
			hibernateSession.close();
		}
	}
	
	protected boolean requiresAuthentication() {
		return true;
	}
	
	protected abstract void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, Session hibernateSession) throws UnauthorizedException, IOException;
}
