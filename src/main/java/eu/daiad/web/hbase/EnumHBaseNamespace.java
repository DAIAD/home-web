package eu.daiad.web.hbase;

import java.util.HashMap;
import java.util.Map;

import eu.daiad.web.configuration.HBaseInitializer;

/**
 * HBase namespace required by DAIAD. If a namespace does not exist, it is created when
 * the application starts (see {@link HBaseInitializer}).
 *
 */
public enum EnumHBaseNamespace {
    /**
     * Default HBase namespace for DAIAD
     */
    DAIAD("daiad");


    private final String value;

    private static final Map<String, EnumHBaseNamespace> stringToEnumMap = new HashMap<String, EnumHBaseNamespace>();
    static {
        for (EnumHBaseNamespace type : EnumHBaseNamespace.values()) {
            if (stringToEnumMap.containsKey(type.value)) {
                throw new IllegalArgumentException(String.format("Duplicate constant found for namespace [%s] and values [%s], [%s].",
                                                                 type.value,
                                                                 type,
                                                                 stringToEnumMap.get(type.value)));
            } else {
                stringToEnumMap.put(type.value, type);
            }
        }
    }

    private EnumHBaseNamespace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value.toLowerCase();
    }

    public static EnumHBaseNamespace fromString(String value) {
        for (EnumHBaseNamespace item : EnumHBaseNamespace.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException(String.format("HBase namespace [%s] is not registered.", value));
    }

}
