package com.vaavud.server.model.phone;

public enum FFTLength {
	
	IOS() {
		@Override
		public int getFFTLength(String osVersion) {
			return OS.isVersionGreatherThanEq(osVersion, "6.1.4") ? 64 : 128;
		}

		@Override
		public int getFFTDataLength(String osVersion) {
			return OS.isVersionGreatherThanEq(osVersion, "6.1.4") ? 50 : 80;
		}
	},	
	ANDROID() {
		@Override
		public int getFFTLength(String osVersion) {
			return 128;
		}

		@Override
		public int getFFTDataLength(String osVersion) {
			return 100;
		}
	};
	
	public abstract int getFFTLength(String osVersion);
	
	public abstract int getFFTDataLength(String osVersion);
}
