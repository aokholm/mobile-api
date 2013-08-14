package com.vaavud.server.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaavud.server.api.util.ServiceUtil;

public abstract class AbstractJSONService<E> extends AbstractHibernateService {

	@Override
	protected final void process(HttpServletRequest req, HttpServletResponse resp, Session hibernateSession) throws UnauthorizedException, IOException {

		getLogger().info("Content-Type: " + req.getContentType() + ", Character-Encoding: " + req.getCharacterEncoding());
		
		String body = ServiceUtil.readBody(req);

		E object = null;
		ObjectMapper mapper = createMapper(hibernateSession);
		
		if (body != null && body.trim().length() > 0) {
			try {
				object = mapper.readValue(body, type());
				//getLogger().info("Got JSON:\n" + body);
			}
			catch (RuntimeException e) {
				getLogger().error("Error processing JSON:\n" + body);
				throw e;
			}
			catch (IOException e) {
				getLogger().error("Error processing JSON:\n" + body);
				throw e;
			}
		}

		try {
			process(req, resp, object, mapper, hibernateSession);
		}
		catch (ProtocolException e) {
			getLogger().error("Sending protocol error due to: " + e.getLogMessage());
			ServiceUtil.sendProtocolErrorResponse(resp, e);
		}		
	}
	
	protected void writeJSONResponse(HttpServletResponse resp, ObjectMapper mapper, Map<String,?> json) throws IOException {
		String responseBody = mapper.writeValueAsString(json);
		ServiceUtil.writeResponse(resp, responseBody, ServiceUtil.JSON_MIME_TYPE);
	}
	
	protected ObjectMapper createMapper(Session hibernateSession) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}
	
	protected abstract Class<E> type();

	protected abstract void process(HttpServletRequest req, HttpServletResponse resp, E object, ObjectMapper mapper, Session hibernateSession) throws UnauthorizedException, ProtocolException, IOException;	
}
