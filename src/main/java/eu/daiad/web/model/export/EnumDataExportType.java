package eu.daiad.web.model.export;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumDataExportType {
    /**
     * Export type is missing or is invalid.
     */
    UNDEFINED(0),
    /**
     * Data export created by a scheduled job.
     */
    DATA_EXPORT(1),
    /**
     * Export of final trial data.
     */
    DATA_EXPORT_TRIAL(2);

    private final int value;

    private EnumDataExportType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumDataExportType fromString(String value) {
        for (EnumDataExportType item : EnumDataExportType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumDataExportType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumDataExportType> {

        @Override
        public EnumDataExportType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumDataExportType.fromString(parser.getValueAsString());
        }
    }

}
