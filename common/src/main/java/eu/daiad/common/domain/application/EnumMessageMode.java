package eu.daiad.common.domain.application;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumMessageMode {
	UNDEFINED(0), SWM(1), AMPHIRO(2), BOTH(3);

	private final int value;

	private EnumMessageMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static EnumMessageMode fromString(String value) {
		for (EnumMessageMode item : EnumMessageMode.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumMessageMode.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumMessageMode> {

		@Override
		public EnumMessageMode deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumMessageMode.fromString(parser.getValueAsString());
		}
	}
}
