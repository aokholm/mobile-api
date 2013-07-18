package com.vaavud.server.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaavud.server.api.ProtocolException;

public final class ServiceUtil {

	private static final String PARAMETER_GZIP = "gzip";
	public static final String TEXT_MIME_TYPE = "text/plain;charset=UTF-8";
	public static final String BINARY_MIME_TYPE = "application/octet-stream";
	public static final String JSON_MIME_TYPE = "application/json;charset=UTF-8";

	public static String readBody(HttpServletRequest req) throws IOException {
		
		// TODO: enforce maximum size and throw exception if reached to guard against attacks. -tsa
		
		String querystring = req.getQueryString();
		
		if (!"POST".equalsIgnoreCase(req.getMethod())) {
			return null;
		}
		else if (querystring != null && querystring.toLowerCase().contains(PARAMETER_GZIP + "=true")) {
			try {
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				GZIPInputStream in = new GZIPInputStream(req.getInputStream());
				int length;
				while ((length = in.read(buffer)) != -1) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.flush();
				out.close();
				return new String(out.toByteArray(), "UTF-8");
			}
			catch (IOException e) {
				throw new IOException("Error decoding gzipped body: " + e.getMessage());
			}
		}
		else {
			try {
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				InputStream in = req.getInputStream();
				int length;
				while ((length = in.read(buffer)) != -1) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.flush();
				out.close();
				return new String(out.toByteArray(), "UTF-8");
			}
			catch (IOException e) {
				throw new IOException("Error reading body: " + e.getMessage(), e);
			}		
		}
	}
	
	public static void sendInternalServerErrorResponse(HttpServletResponse response) throws IOException {
		sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
	}

	public static void sendProtocolErrorResponse(HttpServletResponse response, ProtocolException e) throws IOException {
		sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	}

	public static void sendUnauthorizedErrorResponse(HttpServletResponse response) throws IOException {
		sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, null);
	}

	private static void sendErrorResponse(HttpServletResponse response, int status, String content) throws IOException {
		response.setStatus(status);
		if (content != null && content.length() > 0) {
			writeResponse(response, content, TEXT_MIME_TYPE);
		}
	}

	public static void writeResponse(HttpServletResponse response, String responseBody, String contentType) throws IOException {
		writeResponse(response, responseBody, contentType, true);
	}
	
	private static void writeResponse(HttpServletResponse response, String responseBody, String contentType, boolean gzip) throws IOException {
		byte[] bytes = toUTF8Bytes(responseBody);
		response.setContentType(contentType == null ? TEXT_MIME_TYPE : contentType);
		
		if (!gzip) {
			response.setContentLength(bytes.length);
			OutputStream out = response.getOutputStream();
			out.write(bytes);
			out.close();
		}
		else {
			response.setHeader("Content-Encoding", "gzip");
			
			final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
            gzipOut.write(bytes);
            gzipOut.close();

            bytes = byteOut.toByteArray();
            
			response.setContentLength(bytes.length);
			OutputStream out = response.getOutputStream();
			out.write(bytes);
			out.close();
		}		
	}
	
	public static byte[] toUTF8Bytes(String s) {
		try {
			return (s == null) ? new byte[0] : s.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
