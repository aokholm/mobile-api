package com.vaavud.server.api.util.json;

import java.io.IOException;

import org.hibernate.Session;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.vaavud.server.model.entity.Device;
import com.vaavud.server.model.entity.LatLng;

public class DirectLatLngModule extends SimpleModule {

	public DirectLatLngModule() {
		super();
	    addDeserializer(LatLng.class, new DirectLatLngDeserializer());
	    addSerializer(LatLng.class, new DirectLatLngSerializer());
	}

	private class DirectLatLngSerializer extends StdScalarSerializer<LatLng> {
	    public DirectLatLngSerializer() {
	    	super(LatLng.class);
	    }

	    @Override
	    public void serialize(LatLng value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
	    	if (value == null) {
	    		jgen.writeNull();
	    	}
	    	else {
	    		jgen.writeObject(new double[] {value.getLatitude(), value.getLongitude()});
	    	}
	    }
	}
	
	private class DirectLatLngDeserializer extends StdScalarDeserializer<LatLng> {
	    
		public DirectLatLngDeserializer() {
	    	super(LatLng.class);
	    }

	    @Override
	    public LatLng deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
	        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
	        	double[] values = jsonParser.readValueAs(double[].class);
	        	if (values.length == 2) {
	        		return new LatLng(values[0], values[1]);
	        	}
	        	return null;
	        }

	        throw deserializationContext.mappingException("Expected JSON array");
	    }
	}
}
