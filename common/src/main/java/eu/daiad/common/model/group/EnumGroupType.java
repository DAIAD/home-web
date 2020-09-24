package eu.daiad.common.model.group;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumGroupType {
    UNDEFINED(0), UTILITY(1), CLUSTER(2), SEGMENT(3), SET(4), COMMONS(4);

    private final int value;

    private EnumGroupType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final Map<Integer, EnumGroupType> intToTypeMap = new HashMap<Integer, EnumGroupType>();
    static {
        for (EnumGroupType type : EnumGroupType.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumGroupType fromInteger(int value) {
        EnumGroupType type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null)
            return EnumGroupType.UNDEFINED;
        return type;
    }

    public static EnumGroupType fromString(String value) {
        for (EnumGroupType item : EnumGroupType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumGroupType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumGroupType> {

        @Override
        public EnumGroupType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumGroupType.fromString(parser.getValueAsString());
        }
    }
}
