package eu.daiad.common.model.query;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumRankingType {
	UNDEFINED(0), TOP(1), BOTTOM(2);

	private final int value;

	private EnumRankingType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static EnumRankingType fromString(String value) {
		for (EnumRankingType item : EnumRankingType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumRankingType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumRankingType> {

		@Override
		public EnumRankingType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumRankingType.fromString(parser.getValueAsString());
		}
	}
}