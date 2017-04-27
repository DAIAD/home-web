package eu.daiad.web.model.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumMessageType
{
    UNDEFINED(0),
    ALERT(1),
    TIP(2),
    RECOMMENDATION(3),
    ANNOUNCEMENT(4);

    private final int value;

    private EnumMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static EnumMessageType fromString(String value) {
        for (EnumMessageType item : EnumMessageType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumMessageType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumMessageType>
    {
        // Todo: remove; only to provide backwards compatibility with API clients
        public static final Map<String, String> aliases = new HashMap<>();
        static {
            aliases.put("RECOMMENDATION_DYNAMIC", "RECOMMENDATION");
            aliases.put("RECOMMENDATION_STATIC", "TIP");
        }

        @Override
        public EnumMessageType deserialize(
            JsonParser parser, DeserializationContext context)
                throws IOException, JsonProcessingException
        {
            String s = parser.getValueAsString();
            if (aliases.containsKey(s))
                s = aliases.get(s);
            return EnumMessageType.fromString(s);
        }
    }
}
