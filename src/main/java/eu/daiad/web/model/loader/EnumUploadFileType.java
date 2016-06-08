package eu.daiad.web.model.loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumUploadFileType {
	UNDEFINED(0), METER(1), METER_DATA(2), AMPHIRO_DATA(3), METER_DATA_FORECAST(4);

	private final int value;

	private EnumUploadFileType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumUploadFileType> intToTypeMap = new HashMap<Integer, EnumUploadFileType>();
	static {
		for (EnumUploadFileType type : EnumUploadFileType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumUploadFileType fromInteger(int value) {
		EnumUploadFileType type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumUploadFileType.UNDEFINED;
		return type;
	}

	public static EnumUploadFileType fromString(String value) {
		for (EnumUploadFileType item : EnumUploadFileType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumUploadFileType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumUploadFileType> {

		@Override
		public EnumUploadFileType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumUploadFileType.fromString(parser.getValueAsString());
		}
	}
}