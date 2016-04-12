package eu.daiad.web.model.device;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumDeviceType {
	UNDEFINED(0), METER(1), AMPHIRO(2);

	private final int value;

	private EnumDeviceType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumDeviceType> intToTypeMap = new HashMap<Integer, EnumDeviceType>();
	static {
		for (EnumDeviceType type : EnumDeviceType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumDeviceType fromInteger(int value) {
		EnumDeviceType type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumDeviceType.UNDEFINED;
		return type;
	}

	public static EnumDeviceType fromString(String value) {
		for (EnumDeviceType item : EnumDeviceType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumDeviceType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumDeviceType> {

		@Override
		public EnumDeviceType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumDeviceType.fromString(parser.getValueAsString());
		}
	}
}
