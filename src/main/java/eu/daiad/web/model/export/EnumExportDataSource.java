package eu.daiad.web.model.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumExportDataSource {
	UNDEFINED(0), USER_DATA(1);

	private final int value;

	private EnumExportDataSource(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumExportDataSource> intToTypeMap = new HashMap<Integer, EnumExportDataSource>();
	static {
		for (EnumExportDataSource type : EnumExportDataSource.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumExportDataSource fromInteger(int value) {
		EnumExportDataSource type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumExportDataSource.UNDEFINED;
		return type;
	}

	public static EnumExportDataSource fromString(String value) {
		for (EnumExportDataSource item : EnumExportDataSource.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumExportDataSource.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumExportDataSource> {

		@Override
		public EnumExportDataSource deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumExportDataSource.fromString(parser.getValueAsString());
		}
	}

}
