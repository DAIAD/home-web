package eu.daiad.web.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumTimeAggregation {
	HOUR(1), DAY(2), WEEK(3), MONTH(4), YEAR(5), ALL(6);

	private final int value;

	private EnumTimeAggregation(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	private static final Map<Integer, EnumTimeAggregation> intToTypeMap = new HashMap<Integer, EnumTimeAggregation>();
	static {
		for (EnumTimeAggregation type : EnumTimeAggregation.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumTimeAggregation fromInteger(int value) {
		EnumTimeAggregation type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumTimeAggregation.HOUR;
		return type;
	}

	public static EnumTimeAggregation fromString(String value) {
		for (EnumTimeAggregation item : EnumTimeAggregation.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumTimeAggregation.HOUR;
	}

	public static class Deserializer extends JsonDeserializer<EnumTimeAggregation> {

		@Override
		public EnumTimeAggregation deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
			return EnumTimeAggregation.fromString(parser.getValueAsString());
		}
	}

}
