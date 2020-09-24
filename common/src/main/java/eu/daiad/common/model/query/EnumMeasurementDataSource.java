package eu.daiad.common.model.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.daiad.common.model.device.EnumDeviceType;

public enum EnumMeasurementDataSource {
    NONE(0), BOTH(1), AMPHIRO(2), METER(3), DEVICE(4);

    private final int value;

    private EnumMeasurementDataSource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final Map<Integer, EnumMeasurementDataSource> intToTypeMap = new HashMap<Integer, EnumMeasurementDataSource>();
    static {
        for (EnumMeasurementDataSource type : EnumMeasurementDataSource.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumMeasurementDataSource fromInteger(int value) {
        EnumMeasurementDataSource type = intToTypeMap.get(Integer.valueOf(value));
        // Temporary fix - start
        if (type == DEVICE) {
            return AMPHIRO;
        }
        // Temporary fix - end

        if (type == null) {
            return EnumMeasurementDataSource.NONE;
        }

        return type;
    }

    public static EnumMeasurementDataSource fromDeviceType(EnumDeviceType t)
    {
        EnumMeasurementDataSource s = null;
        switch (t)
        {
        case AMPHIRO:
            s = EnumMeasurementDataSource.AMPHIRO;
            break;
        case METER:
            s = EnumMeasurementDataSource.METER;
            break;
        default:
            s = EnumMeasurementDataSource.BOTH;
            break;
        }
        return s;
    }

    public static EnumMeasurementDataSource fromString(String value) {
        for (EnumMeasurementDataSource item : EnumMeasurementDataSource.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                // Temporary fix - start
                if (item.name().equalsIgnoreCase("DEVICE")) {
                    return AMPHIRO;
                }
                // Temporary fix - end
                return item;
            }
        }
        return EnumMeasurementDataSource.NONE;
    }

    public static class Deserializer extends JsonDeserializer<EnumMeasurementDataSource> {

        @Override
        public EnumMeasurementDataSource deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumMeasurementDataSource.fromString(parser.getValueAsString());
        }
    }
}
