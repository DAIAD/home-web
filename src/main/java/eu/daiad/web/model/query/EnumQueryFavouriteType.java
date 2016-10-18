package eu.daiad.web.model.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum EnumQueryFavouriteType {
    UNDEFINED(0), MAP(1), CHART(2);
    
	private final int value;

	private EnumQueryFavouriteType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumQueryFavouriteType> intToTypeMap = new HashMap<Integer, EnumQueryFavouriteType>();
	static {
		for (EnumQueryFavouriteType type : EnumQueryFavouriteType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumQueryFavouriteType fromInteger(int value) {
		EnumQueryFavouriteType type = intToTypeMap.get(value);
		if (type == null)
			return EnumQueryFavouriteType.UNDEFINED;
		return type;
	}

	@JsonCreator
	public static EnumQueryFavouriteType fromString(String value) {
		if (value != null) {
			for (EnumQueryFavouriteType item : EnumQueryFavouriteType.values()) {
				if (item.name().equalsIgnoreCase(value)) {
					return item;
				}
			}
		}
		return EnumQueryFavouriteType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumQueryFavouriteType> {

		@Override
		public EnumQueryFavouriteType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumQueryFavouriteType.fromString(parser.getValueAsString());
		}
	}    
    
}
