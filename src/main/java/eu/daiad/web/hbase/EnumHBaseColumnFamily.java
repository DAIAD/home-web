package eu.daiad.web.hbase;

import java.util.HashMap;
import java.util.Map;

import eu.daiad.web.configuration.HBaseInitializer;

/**
 * HBase column families used by DAIAD. The default column family
 * {@link EnumHBaseColumnFamily#DEFAULT} is used by {@link HBaseInitializer}
 * whenever a new table is created.
 *
 */
public enum EnumHBaseColumnFamily {
    /**
     * Default HBase column family for DAIAD
     */
    DEFAULT("cf");


    private final String value;

    private static final Map<String, EnumHBaseColumnFamily> stringToEnumMap = new HashMap<String, EnumHBaseColumnFamily>();
    static {
        for (EnumHBaseColumnFamily type : EnumHBaseColumnFamily.values()) {
            if (stringToEnumMap.containsKey(type.value)) {
                throw new IllegalArgumentException(String.format("Duplicate constant found for column family [%s] and values [%s], [%s].",
                                                                 type.value,
                                                                 type,
                                                                 stringToEnumMap.get(type.value)));
            } else {
                stringToEnumMap.put(type.value, type);
            }
        }
    }

    private EnumHBaseColumnFamily(String value) {
        this.value = value;
    }

    public String getValue() {
        return value.toLowerCase();
    }

    public static EnumHBaseColumnFamily fromString(String value) {
        for (EnumHBaseColumnFamily item : EnumHBaseColumnFamily.values()) {
            if (item.getValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException(String.format("HBase column family [%s] is not registered.", value));
    }

}
