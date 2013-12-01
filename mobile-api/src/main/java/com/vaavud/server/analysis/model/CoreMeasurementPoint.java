package com.vaavud.server.analysis.model;

public class CoreMeasurementPoint {
	private double time;
	private double windspeed;
	private double frequency;
	private double amplitude;
	
	public CoreMeasurementPoint(double frequency, double amplitude) {
		this.frequency = frequency;
		this.amplitude = amplitude;
	}
	
	public CoreMeasurementPoint(double time, double frequency, double amplitude) {
		this.time = time;
		this.frequency = frequency;
		this.amplitude = amplitude;
	}
	
	public CoreMeasurementPoint() {
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getWindspeed() {
		return windspeed;
	}

	public void setWindspeed(double windspeed) {
		this.windspeed = windspeed;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}
	
	
	
}
