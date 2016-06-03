package eu.daiad.web.model.favourite;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumFavouriteType {
	UNDEFINED(0), ACCOUNT(1), GROUP(2);

	private final int value;

	private EnumFavouriteType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumFavouriteType> intToTypeMap = new HashMap<Integer, EnumFavouriteType>();
	static {
		for (EnumFavouriteType type : EnumFavouriteType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumFavouriteType fromInteger(int value) {
		EnumFavouriteType type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumFavouriteType.UNDEFINED;
		return type;
	}

	public static EnumFavouriteType fromString(String value) {
		for (EnumFavouriteType item : EnumFavouriteType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumFavouriteType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumFavouriteType> {

		@Override
		public EnumFavouriteType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumFavouriteType.fromString(parser.getValueAsString());
		}
	}
}
