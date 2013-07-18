package com.vaavud.server.api.ping;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.vaavud.server.api.AbstractHibernateService;
import com.vaavud.server.api.AbstractService;

public class JVMPingService extends AbstractService {

	private static final Logger logger = Logger.getLogger(JVMPingService.class);

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
