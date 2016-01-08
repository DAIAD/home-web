package eu.daiad.web.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.daiad.web.model.device.EnumDeviceType;


public class DeviceTypeDeserializer extends JsonDeserializer<EnumDeviceType> {
	
    @Override
    public EnumDeviceType deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        return EnumDeviceType.fromString(parser.getValueAsString());
    }
}