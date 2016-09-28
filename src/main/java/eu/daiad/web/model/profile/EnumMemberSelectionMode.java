package eu.daiad.web.model.profile;

import java.util.HashMap;
import java.util.Map;

public enum EnumMemberSelectionMode {
    AUTO(0), SYSTEM(1), MANUAL(2);

    private final int value;

    private EnumMemberSelectionMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    private static final Map<Integer, EnumMemberSelectionMode> intToTypeMap = new HashMap<Integer, EnumMemberSelectionMode>();
    static {
        for (EnumMemberSelectionMode type : EnumMemberSelectionMode.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumMemberSelectionMode fromInteger(int value) {
        EnumMemberSelectionMode type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null)
            return EnumMemberSelectionMode.AUTO;
        return type;
    }

    public static EnumMemberSelectionMode fromString(String value) {
        for (EnumMemberSelectionMode item : EnumMemberSelectionMode.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumMemberSelectionMode.AUTO;
    }
}
