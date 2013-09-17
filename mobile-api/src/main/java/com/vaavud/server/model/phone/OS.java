package com.vaavud.server.model.phone;

public enum OS {
	
	IOS("iPhone OS"),
	ANDROID("Android");
	
	public static OS fromString(String value) {
		for (OS os : OS.values()) {
			if (os.osName.equalsIgnoreCase(value)) {
				return os;
			}
		}
		return null;
	}
	
	public static final boolean isVersionGreatherThanEq(String version1, String version2) {
		int v1 = (version1 == null) ? -1 : computeComparableVersion(version1);
		int v2 = (version2 == null) ? -1 : computeComparableVersion(version2);
		return v1 == -1 || v2 == -1 ? false : v1 >= v2;
	}
	
	public static final boolean isVersionLessThan(String version1, String version2) {
		int v1 = (version1 == null) ? -1 : computeComparableVersion(version1);
		int v2 = (version2 == null) ? -1 : computeComparableVersion(version2);
		return v1 == -1 || v2 == -1 ? false : v1 < v2;
	}
	
	private static final int computeComparableVersion(String version) {
		
		if (version == null || version.length() == 0) {
			return 0;
		}
		
		version = version.trim();
		
		String[] parts = version.split("\\.");
		
		if (parts.length == 0) {
			return 0;
		}

		int major = 0;
		int minor = 0;
		int sub = 0;

		try {
			if (parts.length > 0) {
				major = Integer.parseInt(parts[0]);
			}
			if (parts.length > 1) {
				minor = Integer.parseInt(parts[1]);
			}
			if (parts.length > 2) {
				sub = Integer.parseInt(parts[2]);
			}
			return major * 10000 + minor * 100 + sub;
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}
	
	private final String osName;
	
	private OS(String osName) {
		this.osName = osName;
	}
}