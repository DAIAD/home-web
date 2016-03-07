package eu.daiad.web.model.profile;

import java.util.HashMap;
import java.util.Map;

public enum EnumUtilityMode {
	UNDEFINED(0),
	ACTIVE(1),
	INACTIVE(2);
	
	private final int value;
	
	private EnumUtilityMode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	private static final Map<Integer, EnumUtilityMode> intToTypeMap = new HashMap<Integer, EnumUtilityMode>();
	static {
	    for (EnumUtilityMode type : EnumUtilityMode.values()) {
	        intToTypeMap.put(type.value, type);
	    }
	}

	public static EnumUtilityMode fromInteger(int value) {
		EnumUtilityMode type = intToTypeMap.get(Integer.valueOf(value));
	    if (type == null) 
	        return EnumUtilityMode.UNDEFINED;
	    return type;
	}
	
	public static EnumUtilityMode fromString(String value) {
		 for (EnumUtilityMode item : EnumUtilityMode.values()) {
	        if (item.name().equalsIgnoreCase(value)) {
            	return item;
	        }
	    }
        return EnumUtilityMode.UNDEFINED;
	}
}
