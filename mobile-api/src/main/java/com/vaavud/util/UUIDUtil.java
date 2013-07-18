package com.vaavud.util;

import java.util.UUID;

public final class UUIDUtil {

	//-------------------------------------------------------------------------
	// Class methods
	//-------------------------------------------------------------------------

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
		
	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------

	private UUIDUtil() {
	}
}
