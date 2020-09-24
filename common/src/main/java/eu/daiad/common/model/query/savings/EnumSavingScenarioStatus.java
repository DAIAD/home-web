package eu.daiad.common.model.query.savings;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumSavingScenarioStatus {
    UNDEFINED(0), PENDING(1), RUNNING(2), COMPLETED(3), FAILED(4), ABANDONED(5);

    private final int value;

    private EnumSavingScenarioStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumSavingScenarioStatus fromString(String value) {
        for (EnumSavingScenarioStatus item : EnumSavingScenarioStatus.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumSavingScenarioStatus.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumSavingScenarioStatus> {

        @Override
        public EnumSavingScenarioStatus deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumSavingScenarioStatus.fromString(parser.getValueAsString());
        }
    }
}