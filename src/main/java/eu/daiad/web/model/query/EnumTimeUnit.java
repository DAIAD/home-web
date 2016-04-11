package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumTimeUnit {
	HOUR(1), DAY(2), WEEK(3), MONTH(4), YEAR(5);

	private final int value;

	private EnumTimeUnit(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumTimeUnit> intToTypeMap = new HashMap<Integer, EnumTimeUnit>();
	static {
		for (EnumTimeUnit type : EnumTimeUnit.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumTimeUnit fromInteger(int value) {
		EnumTimeUnit type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumTimeUnit.HOUR;
		return type;
	}

	public static EnumTimeUnit fromString(String value) {
		for (EnumTimeUnit item : EnumTimeUnit.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumTimeUnit.HOUR;
	}

	public static class Deserializer extends JsonDeserializer<EnumTimeUnit> {

		@Override
		public EnumTimeUnit deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumTimeUnit.fromString(parser.getValueAsString());
		}
	}

}
