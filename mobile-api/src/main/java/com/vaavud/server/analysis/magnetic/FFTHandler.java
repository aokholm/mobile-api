package com.vaavud.server.analysis.magnetic;

import java.util.ArrayList;
import java.util.List;

import com.vaavud.server.analysis.model.CoreMagneticPoint;
import com.vaavud.server.analysis.model.CoreMeasurementPoint;


public class FFTHandler {

	public class FreqAmp {

		public double frequency;
		public double amplitude;
		
		FreqAmp(double _frequency, double _amplitude) {
			frequency = _frequency;
			amplitude = _amplitude;
		}
	}
	
	private Integer dataLength;
	private Integer FFTLength;
	private Integer windowType;
	private Integer interpolationType;
	private FFTAlgorithm myFFTAlgorithm;
	
	private double[] windowValues;
	
	// windowTypes
	public static final int RETANGULAR_WINDOW = 0;
	public static final int WELCH_WINDOW = 1;
	
	// interpolationTypes
	public static final int NO_INTERPOLATION = 0;
	public static final int QUADRATIC_INTERPOLATION = 1;
	public static final int LOGARITHMIC_INTERPOLATION = 2;
	
	
	public FFTHandler(Integer _dataLength, Integer _FFTLength, Integer _windowType, Integer _interpolationType) {
		this.dataLength = _dataLength;
		this.FFTLength = _FFTLength;
		this.windowType = _windowType;
		this.interpolationType = _interpolationType;
		
		generatePrecalculatedWindowValues();
		
		myFFTAlgorithm = new FFTAlgorithm(FFTLength);
	}
	
	
	public FreqAmp getFreqAndAmpOneAxisFFT(List<Double> oneAxisData, Double sampleFrequency) {
		
		List<Double> fftresult;
        
		oneAxisData = applyZeroMean(oneAxisData);
		oneAxisData = windowData(oneAxisData);
		
		int i = 0;
		while (oneAxisData.get(i).doubleValue() == 0f) {
			i++;
			if (i == oneAxisData.size())
				return null;
		}
		
		fftresult = myFFTAlgorithm.doFFT(oneAxisData, dataLength);
		
		FreqAmp mySpeedAndAmp = speedAndAmpFromFFTResult(fftresult, sampleFrequency.doubleValue());
		
		return mySpeedAndAmp;
	}
	
	public CoreMeasurementPoint getFreqAndAmp3DFFT(List<CoreMagneticPoint> magneticPoints, Double sampleFrequency) {
		
		double frequencyMean;
		double frequencyRMSD;
		double amplitudeMean;
		
		List<Double> xAxis = new ArrayList<Double>(dataLength);
		List<Double> yAxis = new ArrayList<Double>(dataLength);
		List<Double> zAxis = new ArrayList<Double>(dataLength);
		
		for (int i = 0; i < magneticPoints.size(); i++) {
			xAxis.add(magneticPoints.get(i).getX());
			yAxis.add(magneticPoints.get(i).getY());
			zAxis.add(magneticPoints.get(i).getZ());
		}
		
		FreqAmp myFAx = getFreqAndAmpOneAxisFFT(xAxis, sampleFrequency);
		if (myFAx == null) {
			return null;
		}
		
		FreqAmp myFAy = getFreqAndAmpOneAxisFFT(yAxis, sampleFrequency);
		if (myFAy == null) {
			return null;
		}
		
		FreqAmp myFAz = getFreqAndAmpOneAxisFFT(zAxis, sampleFrequency);
		if (myFAz == null) {
			return null;
		}
		
		// calculate frequency RMS
		
		frequencyMean = (myFAx.frequency + myFAy.frequency + myFAz.frequency) /3;
		frequencyRMSD = Math.sqrt( 0.3333333333D * Math.pow(myFAx.frequency - frequencyMean, 2) +  Math.pow(myFAy.frequency - frequencyMean, 2) + Math.pow(myFAz.frequency - frequencyMean, 2));
		amplitudeMean = (myFAx.amplitude + myFAy.amplitude + myFAz.amplitude) /3;
				
						
//		Log.v(MainActivity.TAG, String.format("wind: %f, %f, %f amp:  %f, %f, %f, fRMSD: %f",
//					myFAx.frequency, myFAy.frequency, myFAz.frequency,  myFAx.amplitude, myFAy.amplitude, myFAz.amplitude, frequencyRMSD));
		
		if (frequencyRMSD < 0.2 && frequencyMean > 1 && amplitudeMean > 0.3) {
			CoreMeasurementPoint meanCoreMeasurementPoint= new CoreMeasurementPoint(frequencyMean, amplitudeMean);				
			return meanCoreMeasurementPoint;
		}
		
		return null;
	}
	
	private static List<Double> applyZeroMean(List<Double> data) {
		double sum = 0d;		
		double mean; 
		
		for (int i = 0; i < data.size(); i++) {
			sum = sum + data.get(i);
		}
		
		mean = sum / data.size();
		
		for (int i = 0; i < data.size(); i++) {
			data.set(i, (double) (data.get(i)-mean));
		}
		
		return data;
	}
	
	private List<Double> windowData(List<Double> data) {
		for (int i = 0; i < data.size(); i++) {
			data.set(i, (double) windowValues[i] * data.get(i));
		}
		return data;
	}
	
	
	private void generatePrecalculatedWindowValues() {
		windowValues = new double[dataLength];
		
		if (windowType == RETANGULAR_WINDOW) {
			for (int i = 0; i < windowValues.length; i++) {
				windowValues[i] = 1;
			}	
		}
		else if (windowType == WELCH_WINDOW) {
			
			// MODIFY DATA (NOT IMPLEMENTED YET)
			for (int i = 0; i < windowValues.length; i++) {
				windowValues[i] = 1 - Math.pow((i -  (double) (dataLength-1)/2 ) / (  (double) (dataLength+1)/2), 2);
			}
		}
		else {
			
			//Log.e(MainActivity.TAG, "Unsuported WindowType");
			windowType = RETANGULAR_WINDOW;
			generatePrecalculatedWindowValues();
		}
	}
	
	private FreqAmp speedAndAmpFromFFTResult(List<Double> fftResult, Double sampleFrequency) {
		
        int maxBin = 1;
        double maxPeak = 0;
        double peakAmplitude;
        double peakFrequency;
        
        // find the highest peak (bin)
        for (int i=1; i < (FFTLength/2); i++) {
        	
            if (fftResult.get(i) > maxPeak) {
            	maxBin = i;
                maxPeak = fftResult.get(i).doubleValue();
            }
		}
        
        switch (interpolationType) {
		case NO_INTERPOLATION:
        	peakFrequency = (maxBin)*sampleFrequency/FFTLength;
            peakAmplitude = maxPeak;
			break;

		case QUADRATIC_INTERPOLATION:
			double alph = fftResult.get(maxBin-1);
			double beta = fftResult.get(maxBin);
			double gamma = fftResult.get(maxBin+1);

            double p = 0.5d * (alph - gamma) / (alph - 2 * beta + gamma);

            peakFrequency = (double) ((maxBin+p)*sampleFrequency/FFTLength);
            peakAmplitude = (double) (beta - 1/4 * (alph - gamma) * p);
			break;			
			
		case LOGARITHMIC_INTERPOLATION:
			// not implemented yet
			interpolationType = NO_INTERPOLATION;
			return speedAndAmpFromFFTResult(fftResult, sampleFrequency);
				
		default:
			//Log.e(MainActivity.TAG, "Wrong Interpolation type!");
			interpolationType = NO_INTERPOLATION;
			return speedAndAmpFromFFTResult(fftResult, sampleFrequency);
			
		}
		return new FreqAmp(peakFrequency, peakAmplitude);
	}
		
	public Integer getDataLength() {
		return dataLength;
	}
}
