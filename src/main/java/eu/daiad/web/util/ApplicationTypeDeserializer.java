package eu.daiad.web.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.daiad.web.model.EnumApplication;

public class ApplicationTypeDeserializer extends JsonDeserializer<EnumApplication> {

	@Override
	public EnumApplication deserialize(JsonParser parser, DeserializationContext context) throws IOException,
					JsonProcessingException {
		return EnumApplication.fromString(parser.getValueAsString());
	}
}