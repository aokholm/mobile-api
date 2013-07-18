package com.vaavud.server.api;

public class ProtocolException extends Exception {

	private final String clientInfo;
	
	public ProtocolException(String message) {
		this(message, null);
	}

	public ProtocolException(String message, String clientInfo) {
		super(message);
		this.clientInfo = clientInfo;
	}

	public String getLogMessage() {
		return getMessage() + (clientInfo == null ? "" : "\n" + clientInfo);
	}
}
