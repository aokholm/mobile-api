package com.vaavud.server.model;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class Model {

	private static final Model instance = new Model();
	
	public static Model get() {
		return instance;
	}
	
	private static void setPropertyFromSystem(Configuration config, String property) {
		config.setProperty(property, System.getProperty(property, config.getProperty(property)));
	}

	private final SessionFactory sessionFactory;

	private Model() {
		Configuration config = new Configuration().configure();
		setPropertyFromSystem(config, "hibernate.connection.url");
		setPropertyFromSystem(config, "hibernate.connection.username");
		setPropertyFromSystem(config, "hibernate.connection.password");
		setPropertyFromSystem(config, "hibernate.c3p0.max_size");
		setPropertyFromSystem(config, "hibernate.c3p0.min_size");
		sessionFactory = config.buildSessionFactory();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
