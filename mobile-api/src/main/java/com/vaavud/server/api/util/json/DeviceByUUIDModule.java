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

public class DeviceByUUIDModule extends SimpleModule {

	private Session hibernateSession;
	
	public DeviceByUUIDModule(Session hibernateSession) {
		super();
		this.hibernateSession = hibernateSession;
	    addDeserializer(Device.class, new DeviceByUUIDDeserializer());
	    addSerializer(Device.class, new DeviceByUUIDSerializer());
	}

	private class DeviceByUUIDSerializer extends StdScalarSerializer<Device> {
	    public DeviceByUUIDSerializer() {
	    	super(Device.class);
	    }

	    @Override
	    public void serialize(Device value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
	        jgen.writeString(value.getUuid());
	    }
	}
	
	private class DeviceByUUIDDeserializer extends StdScalarDeserializer<Device> {
	    
		public DeviceByUUIDDeserializer() {
	    	super(Device.class);
	    }

	    @Override
	    public Device deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
	        if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
	        	String uuid = jsonParser.getValueAsString();
	        	Device device = (Device) hibernateSession.createQuery("from Device where uuid=:uuid").setString("uuid", uuid).uniqueResult();
	            return device;
	        }

	        throw deserializationContext.mappingException("Expected JSON String");
	    }
	}
}
