package eu.daiad.web.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumSpatialFilterType {
    UNDEFINED(0), CUSTOM(1), AREA(2), GROUP(3), CONSTRAINT(4);

    private final int value;

    private EnumSpatialFilterType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    private static final Map<Integer, EnumSpatialFilterType> intToTypeMap = new HashMap<Integer, EnumSpatialFilterType>();
    static {
        for (EnumSpatialFilterType type : EnumSpatialFilterType.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumSpatialFilterType fromInteger(int value) {
        EnumSpatialFilterType type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null)
            return EnumSpatialFilterType.UNDEFINED;
        return type;
    }

    public static EnumSpatialFilterType fromString(String value) {
        for (EnumSpatialFilterType item : EnumSpatialFilterType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumSpatialFilterType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumSpatialFilterType> {

        @Override
        public EnumSpatialFilterType deserialize(JsonParser parser, DeserializationContext context)
                        throws IOException, JsonProcessingException {
            return EnumSpatialFilterType.fromString(parser.getValueAsString());
        }
    }
}