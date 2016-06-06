package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumSpatialFilterOperation {
	UNDEFINED(0), CONTAINS(1), INTERSECT(2), DISTANCE(3);

	private final int value;

	private EnumSpatialFilterOperation(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumSpatialFilterOperation> intToTypeMap = new HashMap<Integer, EnumSpatialFilterOperation>();
	static {
		for (EnumSpatialFilterOperation type : EnumSpatialFilterOperation.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumSpatialFilterOperation fromInteger(int value) {
		EnumSpatialFilterOperation type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumSpatialFilterOperation.UNDEFINED;
		return type;
	}

	public static EnumSpatialFilterOperation fromString(String value) {
		for (EnumSpatialFilterOperation item : EnumSpatialFilterOperation.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumSpatialFilterOperation.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumSpatialFilterOperation> {
		
	    @Override
	    public EnumSpatialFilterOperation deserialize(JsonParser parser, DeserializationContext context)
	            throws IOException, JsonProcessingException {
	        return EnumSpatialFilterOperation.fromString(parser.getValueAsString());
	    }
	}
}