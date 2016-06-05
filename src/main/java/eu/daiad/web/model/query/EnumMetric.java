package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumMetric {
	UNDEFINED(0), COUNT(1), SUM(2), AVERAGE(3), MIN(4), MAX(5);

	private final int value;

	private EnumMetric(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumMetric> intToTypeMap = new HashMap<Integer, EnumMetric>();
	static {
		for (EnumMetric type : EnumMetric.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumMetric fromInteger(int value) {
		EnumMetric type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumMetric.UNDEFINED;
		return type;
	}

	@JsonCreator
	public static EnumMetric fromString(String value) {
		if (value != null) {
			for (EnumMetric item : EnumMetric.values()) {
				if (item.name().equalsIgnoreCase(value)) {
					return item;
				}
			}
		}
		return EnumMetric.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumMetric> {

		@Override
		public EnumMetric deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumMetric.fromString(parser.getValueAsString());
		}
	}
}