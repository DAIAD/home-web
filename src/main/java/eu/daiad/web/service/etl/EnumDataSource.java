package eu.daiad.web.service.etl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Enumeration of available data sources
 */
public enum EnumDataSource {
    /**
     * No data is exported.
     */
    NONE(0),
    /**
     * Data for amphiro b1 devices is exported.
     */
    AMPHIRO(1),
    /**
     * Data for smart water meter is exported.
     */
    METER(2);

    private final int value;

    private EnumDataSource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final Map<Integer, EnumDataSource> intToTypeMap = new HashMap<Integer, EnumDataSource>();
    static {
        for (EnumDataSource type : EnumDataSource.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumDataSource fromInteger(int value) {
        EnumDataSource type = intToTypeMap.get(Integer.valueOf(value));

        if (type == null) {
            return EnumDataSource.NONE;
        }

        return type;
    }

    public static EnumDataSource fromString(String value) {
        for (EnumDataSource item : EnumDataSource.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumDataSource.NONE;
    }

    public static class Deserializer extends JsonDeserializer<EnumDataSource> {

        @Override
        public EnumDataSource deserialize(JsonParser parser, DeserializationContext context) throws IOException,
                        JsonProcessingException {
            return EnumDataSource.fromString(parser.getValueAsString());
        }
    }
}
