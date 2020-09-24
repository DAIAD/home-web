package eu.daiad.common.model.query.savings;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumBudgetDistribution {
    UNDEFINED(0), EQUAL(1), FAIR(2);

    private final int value;

    private EnumBudgetDistribution(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumBudgetDistribution fromString(String value) {
        for (EnumBudgetDistribution item : EnumBudgetDistribution.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumBudgetDistribution.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumBudgetDistribution> {

        @Override
        public EnumBudgetDistribution deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumBudgetDistribution.fromString(parser.getValueAsString());
        }
    }
}