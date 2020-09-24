package eu.daiad.common.model.scheduling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumExecutionExitCode {
    UNDEFINED(0), UNKNOWN(1), EXECUTING(2), COMPLETED(3), NOOP(4), FAILED(5), STOPPED(6);

    private final int value;

    private EnumExecutionExitCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    private static final Map<Integer, EnumExecutionExitCode> intToTypeMap = new HashMap<Integer, EnumExecutionExitCode>();
    static {
        for (EnumExecutionExitCode type : EnumExecutionExitCode.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumExecutionExitCode fromInteger(int value) {
        EnumExecutionExitCode type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null) {
            return EnumExecutionExitCode.UNDEFINED;
        }
        return type;
    }

    public static EnumExecutionExitCode fromString(String value) {
        if (value != null) {
            for (EnumExecutionExitCode item : EnumExecutionExitCode.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
        }
        return EnumExecutionExitCode.UNDEFINED;
    }

    public static EnumExecutionExitCode fromExistStatus(ExitStatus value) {
        if (value != null) {
            for (EnumExecutionExitCode item : EnumExecutionExitCode.values()) {
                if (item.name().equalsIgnoreCase(value.getExitCode())) {
                    return item;
                }
            }
        }
        return EnumExecutionExitCode.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumExecutionExitCode> {

        @Override
        public EnumExecutionExitCode deserialize(JsonParser parser, DeserializationContext context) throws IOException,
                        JsonProcessingException {
            return EnumExecutionExitCode.fromString(parser.getValueAsString());
        }
    }
}
