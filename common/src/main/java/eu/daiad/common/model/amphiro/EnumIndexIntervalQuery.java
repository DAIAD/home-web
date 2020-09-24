package eu.daiad.common.model.amphiro;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumIndexIntervalQuery {
	UNDEFINED(0), ABSOLUTE(1), SLIDING(2);

	private final int value;

	private EnumIndexIntervalQuery(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static EnumIndexIntervalQuery fromString(String value) {
		for (EnumIndexIntervalQuery item : EnumIndexIntervalQuery.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumIndexIntervalQuery.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumIndexIntervalQuery> {

		@Override
		public EnumIndexIntervalQuery deserialize(JsonParser parser, DeserializationContext context)
						throws IOException, JsonProcessingException {
			return EnumIndexIntervalQuery.fromString(parser.getValueAsString());
		}
	}
}