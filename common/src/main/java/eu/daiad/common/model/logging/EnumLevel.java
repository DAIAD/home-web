package eu.daiad.common.model.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumLevel {
	UNDEFINED(0), FATAL(1), ERROR(2), WARN(3), INFO(4), DEBUG(5), TRACE(6);

	private final int value;

	private EnumLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumLevel> intToTypeMap = new HashMap<Integer, EnumLevel>();
	static {
		for (EnumLevel type : EnumLevel.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumLevel fromInteger(int value) {
		EnumLevel type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumLevel.UNDEFINED;
		return type;
	}

	public static EnumLevel fromString(String value) {
		if (value != null) {
			for (EnumLevel item : EnumLevel.values()) {
				if (item.name().equalsIgnoreCase(value)) {
					return item;
				}
			}
		}
		return EnumLevel.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumLevel> {

		@Override
		public EnumLevel deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumLevel.fromString(parser.getValueAsString());
		}
	}
}
