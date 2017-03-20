package eu.daiad.web.model.profile;

import java.util.HashMap;
import java.util.Map;

public enum EnumRealTimeMode {
    AUTO(0), MANUAL(1);

    private final int value;

    private EnumRealTimeMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    private static final Map<Integer, EnumRealTimeMode> intToTypeMap = new HashMap<Integer, EnumRealTimeMode>();
    static {
        for (EnumRealTimeMode type : EnumRealTimeMode.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumRealTimeMode fromInteger(int value) {
        EnumRealTimeMode type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null) {
            return EnumRealTimeMode.AUTO;
        }
        return type;
    }

    public static EnumRealTimeMode fromString(String value) {
        for (EnumRealTimeMode item : EnumRealTimeMode.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumRealTimeMode.AUTO;
    }
}
