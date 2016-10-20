package eu.daiad.web.model.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Enumeration of supported data export request types. 
 */
public enum EnumDataExportRequestType {
    /**
     * Export type is missing or is invalid.
     */
	UNDEFINED(0),
	/**
	 * Export all data for a single user.
	 */
	USER(1);

	private final int value;

	private EnumDataExportRequestType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumDataExportRequestType> intToTypeMap = new HashMap<Integer, EnumDataExportRequestType>();
	static {
		for (EnumDataExportRequestType type : EnumDataExportRequestType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumDataExportRequestType fromInteger(int value) {
		EnumDataExportRequestType type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumDataExportRequestType.UNDEFINED;
		return type;
	}

	public static EnumDataExportRequestType fromString(String value) {
		for (EnumDataExportRequestType item : EnumDataExportRequestType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumDataExportRequestType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumDataExportRequestType> {

		@Override
		public EnumDataExportRequestType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
			return EnumDataExportRequestType.fromString(parser.getValueAsString());
		}
	}

}
