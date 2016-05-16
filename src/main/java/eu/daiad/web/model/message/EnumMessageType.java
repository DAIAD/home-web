package eu.daiad.web.model.message;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumMessageType {
	UNDEFINED(0), ALERT(1), RECOMMENDATION_STATIC(2), RECOMMENDATION_DYNAMIC(3), ANNOUNCEMENT(4);

	private final int value;

	private EnumMessageType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static EnumMessageType fromString(String value) {
		for (EnumMessageType item : EnumMessageType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumMessageType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumMessageType> {

		@Override
		public EnumMessageType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumMessageType.fromString(parser.getValueAsString());
		}
	}
}