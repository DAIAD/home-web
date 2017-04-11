package eu.daiad.web.model.query;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumDataField {
    UNDEFINED(0), VOLUME(1), ENERGY(2), DURATION(3), TEMPERATURE(4), FLOW(5);

    private final int value;

    private EnumDataField(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumDataField fromString(String value) {
        for (EnumDataField item : EnumDataField.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumDataField.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumDataField> {

        @Override
        public EnumDataField deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumDataField.fromString(parser.getValueAsString());
        }
    }
}

