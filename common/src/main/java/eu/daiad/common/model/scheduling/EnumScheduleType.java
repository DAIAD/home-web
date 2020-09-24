package eu.daiad.common.model.scheduling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumScheduleType {
    UNDEFINED(0), PERIOD(1), CRON(2);

    private final int value;

    private EnumScheduleType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    private static final Map<Integer, EnumScheduleType> intToTypeMap = new HashMap<Integer, EnumScheduleType>();
    static {
        for (EnumScheduleType type : EnumScheduleType.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumScheduleType fromInteger(int value) {
        EnumScheduleType type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null) {
            return EnumScheduleType.UNDEFINED;
        }
        return type;
    }

    public static EnumScheduleType fromString(String value) {
        if (value != null) {
            for (EnumScheduleType item : EnumScheduleType.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
        }
        return EnumScheduleType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumScheduleType> {

        @Override
        public EnumScheduleType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
                        JsonProcessingException {
            return EnumScheduleType.fromString(parser.getValueAsString());
        }
    }
}
