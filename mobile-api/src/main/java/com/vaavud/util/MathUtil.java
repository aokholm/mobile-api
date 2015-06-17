package com.vaavud.util;

public final class MathUtil {
	
	public static String toCardinal(Float windDirectionDegree){
		if (windDirectionDegree == null)
			return "N/A";
		if (windDirectionDegree> 348.75 && windDirectionDegree < 11.25)
            return "N";
        else if (windDirectionDegree < 33.75)
            return "NNE";
        else if (windDirectionDegree < 56.25)
            return "NE";
        else if (windDirectionDegree < 78.75)
            return "ENE";
        else if (windDirectionDegree < 101.25)
            return "E";
        else if (windDirectionDegree < 123.75)
            return "ESE";
        else if (windDirectionDegree < 146.25)
            return "SE";
        else if (windDirectionDegree < 168.75)
            return "SSE";
        else if (windDirectionDegree < 191.25)
            return "S";
        else if (windDirectionDegree < 213.75)
            return "SSW";
        else if (windDirectionDegree < 236.25)
            return "SW";
        else if (windDirectionDegree < 258.75)
        	return "WSW";
    	else if (windDirectionDegree < 281.25)
            return "W";
    	else if (windDirectionDegree < 303.75)
            return "WNW";
    	else if (windDirectionDegree < 326.25)
            return "NW";
        else
            return "NNW"; 
	}
}
