package eu.daiad.web.security.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class RoleTypeDeserializer extends JsonDeserializer<EnumRole> {
	
    @Override
    public EnumRole deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        return EnumRole.fromString(parser.getValueAsString());
    }
}