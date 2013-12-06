package com.vaavud.server.analysis.magnetic;

import java.util.ArrayList;
import java.util.List;

import com.vaavud.server.analysis.model.CoreMagneticPoint;
import com.vaavud.server.analysis.model.CoreMeasurementPoint;

public class FFTManager {

	private FFTHandler normalFFT;
	private DataManager myDataManager;
		
	public FFTManager(DataManager dataManager) {
		myDataManager = dataManager;
		normalFFT = new FFTHandler(70, 128, FFTHandler.WELCH_WINDOW, FFTHandler.QUADRATIC_INTERPOLATION);
	}
	
	
	public List<CoreMeasurementPoint> getMeasurementPoints() {
		
		List<CoreMeasurementPoint> measurementPoints = new ArrayList<CoreMeasurementPoint>(myDataManager.getNumberOfMeassurements());
		
		for(int i = 0; i < myDataManager.getNumberOfMeassurements(); i+=10) {
			
			CoreMeasurementPoint myCoreMeasurementPoint = getFreqAmp3DAtIndex(i);
			
			if (myCoreMeasurementPoint != null) {
				measurementPoints.add(myCoreMeasurementPoint);
			}
		}
		
		return measurementPoints;
	}
	
	public CoreMeasurementPoint getFreqAmp3DAtIndex(int index) {
		
		List<CoreMagneticPoint> magneticPoints = myDataManager.getMagneticfieldMeasurementsAtIndex(index, 70);
		
		if (magneticPoints.size() >= normalFFT.getDataLength() ) {
			CoreMeasurementPoint coreMeasurementPoint = normalFFT.getFreqAndAmp3DFFT(magneticPoints, getSampleFrequency(magneticPoints));
			
			if (coreMeasurementPoint != null) {
//				System.out.println(Double.toString(coreMeasurementPoint.getFrequency()));
//				System.out.println(magneticPoints.size()-1);
//				System.out.println(magneticPoints.get(magneticPoints.size()-1).getTime());
				coreMeasurementPoint.setTime(magneticPoints.get(magneticPoints.size()-1).getTime());
				return coreMeasurementPoint;
			}
			else {
				return null;
			}
			
		} else {
			return null;
		}
	}
	
	
	public Double getSampleFrequency(List<CoreMagneticPoint> magneticPoints) {
		double timeDiff = magneticPoints.get(magneticPoints.size()-1).getTime() - magneticPoints.get(0).getTime();
		double sampleFrequency = (normalFFT.getDataLength() -1) / timeDiff;
		
		return sampleFrequency;
		
	}


}
