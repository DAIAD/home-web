package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumMeasurementDataSource {
	ALL(0), AMPHIRO(1), METER(2);

	private final int value;

	private EnumMeasurementDataSource(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumMeasurementDataSource> intToTypeMap = new HashMap<Integer, EnumMeasurementDataSource>();
	static {
		for (EnumMeasurementDataSource type : EnumMeasurementDataSource.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumMeasurementDataSource fromInteger(int value) {
		EnumMeasurementDataSource type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumMeasurementDataSource.ALL;
		return type;
	}

	public static EnumMeasurementDataSource fromString(String value) {
		for (EnumMeasurementDataSource item : EnumMeasurementDataSource.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumMeasurementDataSource.ALL;
	}

	public static class Deserializer extends JsonDeserializer<EnumMeasurementDataSource> {

		@Override
		public EnumMeasurementDataSource deserialize(JsonParser parser, DeserializationContext context)
						throws IOException, JsonProcessingException {
			return EnumMeasurementDataSource.fromString(parser.getValueAsString());
		}
	}
}
