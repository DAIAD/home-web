package eu.daiad.web.model.profile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumUnit {
    UNDEFINED(0), METRIC(1), IMPERIAL(2);

    private final int value;

    private EnumUnit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final Map<Integer, EnumUnit> intToTypeMap = new HashMap<Integer, EnumUnit>();
    static {
        for (EnumUnit type : EnumUnit.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumUnit fromInteger(int value) {
        EnumUnit type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null)
            return EnumUnit.UNDEFINED;
        return type;
    }

    public static EnumUnit fromString(String value) {
        for (EnumUnit item : EnumUnit.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumUnit.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumUnit> {

        @Override
        public EnumUnit deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumUnit.fromString(parser.getValueAsString());
        }
    }
}
