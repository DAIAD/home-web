package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumTemporalFilterType {
	UNDEFINED(0), ABSOLUTE(1), SLIDING(2);

	private final int value;

	private EnumTemporalFilterType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumTemporalFilterType> intToTypeMap = new HashMap<Integer, EnumTemporalFilterType>();
	static {
		for (EnumTemporalFilterType type : EnumTemporalFilterType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumTemporalFilterType fromInteger(int value) {
		EnumTemporalFilterType type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumTemporalFilterType.UNDEFINED;
		return type;
	}

	public static EnumTemporalFilterType fromString(String value) {
		for (EnumTemporalFilterType item : EnumTemporalFilterType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumTemporalFilterType.UNDEFINED;
	}
	
	public static class Deserializer extends JsonDeserializer<EnumTemporalFilterType> {

		@Override
		public EnumTemporalFilterType deserialize(JsonParser parser, DeserializationContext context)
						throws IOException, JsonProcessingException {
			return EnumTemporalFilterType.fromString(parser.getValueAsString());
		}
	}
}