package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumTimeInterval {
	NONE(0), HOUR(1), DAY(2), WEEK(3), MONTH(4), YEAR(5);

	private final int value;

	private EnumTimeInterval(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumTimeInterval> intToTypeMap = new HashMap<Integer, EnumTimeInterval>();
	static {
		for (EnumTimeInterval type : EnumTimeInterval.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumTimeInterval fromInteger(int value) {
		EnumTimeInterval type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumTimeInterval.NONE;
		return type;
	}

	public static EnumTimeInterval fromString(String value) {
		for (EnumTimeInterval item : EnumTimeInterval.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumTimeInterval.NONE;
	}

	public static class Deserializer extends JsonDeserializer<EnumTimeInterval> {

		@Override
		public EnumTimeInterval deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumTimeInterval.fromString(parser.getValueAsString());
		}
	}

}
