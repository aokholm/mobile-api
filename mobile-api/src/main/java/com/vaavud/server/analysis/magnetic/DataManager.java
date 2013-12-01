package com.vaavud.server.analysis.magnetic;

import java.util.ArrayList;
import java.util.List;

import com.vaavud.server.analysis.model.CoreMagneticPoint;

public class DataManager {
	
	private List<CoreMagneticPoint> magneticfieldMeasurements;
	
	public DataManager() {
		magneticfieldMeasurements = new ArrayList<CoreMagneticPoint>();		
	}

	public void addMagneticFieldReadings(List<CoreMagneticPoint> magneticfieldMeasurements) {
		this.magneticfieldMeasurements = magneticfieldMeasurements;
	}
	
	public void addMagneticFieldReading(CoreMagneticPoint magReading) {
		magneticfieldMeasurements.add(magReading);
	}
	
	public List<CoreMagneticPoint> getMagneticfieldMeasurementsAtIndex(Integer index, Integer numberOfMeasurements) {
		int listSize = magneticfieldMeasurements.size();
		List<CoreMagneticPoint> magneticfieldMeasurementsList;
		
		if ((listSize- index) > numberOfMeasurements) {
			magneticfieldMeasurementsList = magneticfieldMeasurements.subList(index , index + numberOfMeasurements);
		}
		else {
			magneticfieldMeasurementsList = magneticfieldMeasurements.subList(index, listSize);	
		}
		
		return magneticfieldMeasurementsList;
	}
	
	
	public List<CoreMagneticPoint> getMagneticfieldMeasurements() {
		return magneticfieldMeasurements;
	}
	
	public void clearData() {
		magneticfieldMeasurements = new ArrayList<CoreMagneticPoint>();
	}
	
	public Double getTimeSinceStart() {
		if (magneticfieldMeasurements.size() >= 1) {
			return magneticfieldMeasurements.get(magneticfieldMeasurements.size() -1).getTime();
		}
		else {
			return 0d;
		}
	}
	
	public Integer getNumberOfMeassurements() {
		return magneticfieldMeasurements.size();
	}
	
	public String getNumberOfMeasurementsString() {
		return Integer.toString(magneticfieldMeasurements.size());
	}
	
	public Double getLastMagX() {
		if (magneticfieldMeasurements.size() >= 1) {
			return magneticfieldMeasurements.get(magneticfieldMeasurements.size() -1).getX();
		}
		else {
			return null;
		}
	}
	

}
