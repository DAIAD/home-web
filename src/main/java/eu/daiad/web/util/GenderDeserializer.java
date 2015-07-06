package eu.daiad.web.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.daiad.web.model.EnumGender;


public class GenderDeserializer extends JsonDeserializer<EnumGender> {
	
    @Override
    public EnumGender deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        return EnumGender.fromString(parser.getValueAsString());
    }
}