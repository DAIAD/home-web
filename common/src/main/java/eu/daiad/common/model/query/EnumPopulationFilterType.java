package eu.daiad.common.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumPopulationFilterType {
	UNDEFINED(0), USER(1), GROUP(2), CLUSTER(3), UTILITY(4);

	private final int value;

	private EnumPopulationFilterType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	private static final Map<Integer, EnumPopulationFilterType> intToTypeMap = new HashMap<Integer, EnumPopulationFilterType>();
	static {
		for (EnumPopulationFilterType type : EnumPopulationFilterType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumPopulationFilterType fromInteger(int value) {
		EnumPopulationFilterType type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumPopulationFilterType.UNDEFINED;
		return type;
	}

	public static EnumPopulationFilterType fromString(String value) {
		for (EnumPopulationFilterType item : EnumPopulationFilterType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumPopulationFilterType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumPopulationFilterType> {

		@Override
		public EnumPopulationFilterType deserialize(JsonParser parser, DeserializationContext context)
						throws IOException, JsonProcessingException {
			return EnumPopulationFilterType.fromString(parser.getValueAsString());
		}
	}
}
