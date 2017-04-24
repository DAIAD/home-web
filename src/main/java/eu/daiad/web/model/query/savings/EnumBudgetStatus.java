package eu.daiad.web.model.query.savings;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumBudgetStatus {
    UNDEFINED(0), PENDING(1), RUNNING(2), COMPLETED(3), FAILED(4);

    private final int value;

    private EnumBudgetStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumBudgetStatus fromString(String value) {
        for (EnumBudgetStatus item : EnumBudgetStatus.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumBudgetStatus.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumBudgetStatus> {

        @Override
        public EnumBudgetStatus deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumBudgetStatus.fromString(parser.getValueAsString());
        }
    }
}