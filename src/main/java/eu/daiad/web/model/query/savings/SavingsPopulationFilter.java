package eu.daiad.web.model.query.savings;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class SavingsPopulationFilter {

    @JsonDeserialize(using = EnumType.Deserializer.class)
    private EnumType type = EnumType.UNDEFINED;

    private UUID key;

    public SavingsPopulationFilter() {

    }

    public EnumType getType() {
        return type;
    }

    public void setType(EnumType type) {
        this.type = type;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public static enum EnumType {
        UNDEFINED(0), GROUP(1), UTILITY(2);

        private final int value;

        private EnumType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static EnumType fromString(String value) {
            for (EnumType item : EnumType.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EnumType.UNDEFINED;
        }

        public static class Deserializer extends JsonDeserializer<EnumType> {

            @Override
            public EnumType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
                return EnumType.fromString(parser.getValueAsString());
            }
        }
    }

}
