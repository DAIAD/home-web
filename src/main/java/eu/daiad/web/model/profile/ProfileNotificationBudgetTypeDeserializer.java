package eu.daiad.web.model.profile;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ProfileNotificationBudgetTypeDeserializer extends
		JsonDeserializer<EnumProfileNotificationBudgetType> {

	@Override
	public EnumProfileNotificationBudgetType deserialize(JsonParser parser,
			DeserializationContext context) throws IOException,
			JsonProcessingException {
		return EnumProfileNotificationBudgetType.fromString(parser
				.getValueAsString());
	}
}