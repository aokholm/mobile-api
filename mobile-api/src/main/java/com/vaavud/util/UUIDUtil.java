package com.vaavud.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Logger;

public final class UUIDUtil {

	private static final Logger logger = Logger.getLogger(UUIDUtil.class);

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static String md5Hash(String text) {
		String digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(text.getBytes("UTF-8")); 
			StringBuilder sb = new StringBuilder(2*hash.length);
			for(byte b : hash){
				sb.append(String.format("%02x", b&0xff)); 
			}
			digest = sb.toString().toUpperCase(Locale.US);
		}
		catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		catch (NoSuchAlgorithmException e) {
			logger.error(e);
		}
		return digest;
	}
		
	private UUIDUtil() {
	}
}
