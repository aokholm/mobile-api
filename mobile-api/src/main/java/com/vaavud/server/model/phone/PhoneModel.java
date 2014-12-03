package com.vaavud.server.model.phone;

import java.util.Locale;

import org.apache.log4j.Logger;

public enum PhoneModel {

	/* iPhone Algorithm Configurations */
	
	IOS_STANDARD(OS.IOS, FFTLength.IOS, Algorithm.STANDARD, 0.238D, 1.07D, new String[] {}),
	
	// using both legacy (app version 1.2.1) naming (ie. iPhone4GSM) and new (ie. iPhone4,) 
	IPHONE4(OS.IOS, FFTLength.IOS, Algorithm.IPHONE4, 0.238D, 1.16D, new String[] {"iPhone4GSM", "iPhone4GSMRevA", "iPhone4GSM+CDMA", "iPhone4S", "iPhone3,", "iPhone4,"}),
	
	IPHONE5(OS.IOS, FFTLength.IOS, Algorithm.STANDARD, 0.238D, 1.04D, new String[] {"iPhone5GSM", "iPhone5GSM+CDMA", "iPhone5,", "iPhone6,"}),
	
	IPHONE6(OS.IOS, FFTLength.IOS, Algorithm.STANDARD, 0.238D, 1.04D, new String[] {"iPhone7,"}),
	
	/* Android Algorithm Configurations */
	
	ANDROID_STANDARD(OS.ANDROID, FFTLength.ANDROID, Algorithm.STANDARD, 0.238D, 1.07D, new String[] {}),
	
	GS4(OS.ANDROID, FFTLength.ANDROID, Algorithm.STANDARD, 0.238D, 1.09D,
		new String[] {"GT-I9500", "SHV-E300", "GT-I9505", "SGH-I337", "SGH-M919", "SCH-I545",
			          "SPH-L720", "SCH-R970", "GT-I9508", "SCH-I959", "GT-I9502", "SGH-N045"}),
			
	GS4MINI(OS.ANDROID, FFTLength.ANDROID, Algorithm.STANDARD, 0.238D, 1.09D,
		new String[] {"GT-I919"}),
			
	GS3(OS.ANDROID, FFTLength.ANDROID, Algorithm.STANDARD, 0.238D, 1.09D,
		new String[] {"GT-I9300", "GT-I9305", "SHV-E210", "SGH-T999", "SGH-I747", "SGH-N064",
			          "SGH-N035", "SCH-J021", "SCH-R530", "SCH-I535", "SPH-L710", "GT-I9308", "SCH-I939"}),
			
	GS2(OS.ANDROID, FFTLength.ANDROID, Algorithm.STANDARD, 0.238D, 1.05D,
		new String[] {"GT-I9100", "GT-I9210", "GT-I9210", "SGH-I757", "SGH-I727", "SGH-I927",
			          "SGH-T989", "GT-I9108", "ISW11", "MODEL SC-02", "SHW-M250", "SGH-I777",
			          "SGH-I927", "SPH-D710", "SGH-T989", "SCH-R760", "GT-I9105"});
	
	public static PhoneModel getPhoneModel(String osName, String model) {
		OS os = OS.fromString(osName);
		if (os == null) {
			return IOS_STANDARD;
		}
		for (PhoneModel phoneModel : PhoneModel.values()) {
			if (phoneModel.matches(os, model)) {
			    logger.info("Phone model upload: " + model + "Phone model server: " + phoneModel.toString());
				return phoneModel;
			}
		}
		return (os == OS.ANDROID) ? ANDROID_STANDARD : IOS_STANDARD;
	}

	private final OS os;
	private final Algorithm algorithm;
	private final FFTLength fftLength;
	private final double frequencyStart;
	private final double frequencyFactor;
	private final String[] models;
	
	private static final Logger logger = Logger.getLogger(PhoneModel.class);
	
	private PhoneModel(OS os, FFTLength fftLength, Algorithm algorithm, double frequencyStart, double frequencyFactor, String[] models) {
		this.os = os;
		this.fftLength = fftLength;
		this.algorithm = algorithm;
		this.frequencyStart = frequencyStart;
		this.frequencyFactor = frequencyFactor;
		this.models = models;
	}
	
	private boolean matches(OS os, String model) {
		if (this.os != os) {
			return false;
		}
		for (String canonicalModel : models) {
			if (model.toUpperCase(Locale.US).startsWith(canonicalModel.toUpperCase(Locale.US))) {
//			    logger.info("Compare model upload: " + model.toUpperCase(Locale.US) + "Phone model server: " + canonicalModel.toUpperCase(Locale.US) );
				return true;
			}
		}
		return false;
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
	public double getFrequencyStart() {
		return frequencyStart;
	}
	
	public double getFrequencyFactor() {
		return frequencyFactor;
	}
	
	public int getFFTLength(String osVersion) {
		return fftLength.getFFTLength(osVersion);
	}
	
	public int getFFTDataLength(String osVersion) {
		return fftLength.getFFTDataLength(osVersion);
	}
}