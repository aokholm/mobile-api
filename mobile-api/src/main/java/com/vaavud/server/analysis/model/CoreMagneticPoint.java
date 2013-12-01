package com.vaavud.server.analysis.model;

public class CoreMagneticPoint {
	private double time;
	private double x;
	private double y;
	private double z;
	
	public CoreMagneticPoint(double time, double x, double y, double z) {
		// TODO Auto-generated constructor stub
		this.time = time;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	
}
