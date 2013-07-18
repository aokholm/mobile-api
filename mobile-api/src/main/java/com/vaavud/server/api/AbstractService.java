package com.vaavud.server.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.vaavud.server.api.util.ServiceUtil;
import com.vaavud.server.model.Model;

public abstract class AbstractService extends HttpServlet {

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}
	
	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}
	
	protected abstract void process(HttpServletRequest req, HttpServletResponse resp) throws IOException;
	
	protected abstract Logger getLogger();
}
