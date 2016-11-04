package eu.daiad.web.model.security;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.daiad.web.model.EnumValueDescription;

public enum EnumRole {
    @EnumValueDescription("Allows access to DAIAD@Utility. User can access all registered utilities and perform generic system administrative tasks.")
    ROLE_SYSTEM_ADMIN,

    @EnumValueDescription("Allows access to DAIAD@Utility. User may access specific utilities only and perform utility specific administrative tasks.")
    ROLE_UTILITY_ADMIN,

	@EnumValueDescription("Allows access to DAIAD@Home for the current user")
	ROLE_USER;

	public static EnumRole fromString(String value) {
		for (EnumRole item : EnumRole.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}

		return null;
	}

	public static class Deserializer extends JsonDeserializer<EnumRole> {

		@Override
		public EnumRole deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumRole.fromString(parser.getValueAsString());
		}
	}

}
