package com.vaavud.server.api.ping;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.vaavud.server.api.AbstractHibernateService;
import com.vaavud.server.model.entity.Device;

public class DatabasePingService extends AbstractHibernateService {

	private static final Logger logger = Logger.getLogger(DatabasePingService.class);

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp, Device authenticatedDevice, Session hibernateSession) throws IOException {
		hibernateSession.beginTransaction();
		hibernateSession.createQuery("select max(id) from MeasurementSession").uniqueResult();
	}

	@Override
	protected boolean requiresAuthentication() {
		return false;
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
}
