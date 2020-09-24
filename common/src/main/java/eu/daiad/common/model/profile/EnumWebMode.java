package eu.daiad.common.model.profile;

import java.util.HashMap;
import java.util.Map;

public enum EnumWebMode {
	UNDEFINED(0),
	ACTIVE(1),
	INACTIVE(2);
	
	private final int value;
	
	private EnumWebMode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	private static final Map<Integer, EnumWebMode> intToTypeMap = new HashMap<Integer, EnumWebMode>();
	static {
	    for (EnumWebMode type : EnumWebMode.values()) {
	        intToTypeMap.put(type.value, type);
	    }
	}

	public static EnumWebMode fromInteger(int value) {
		EnumWebMode type = intToTypeMap.get(Integer.valueOf(value));
	    if (type == null) 
	        return EnumWebMode.UNDEFINED;
	    return type;
	}
	
	public static EnumWebMode fromString(String value) {
		 for (EnumWebMode item : EnumWebMode.values()) {
	        if (item.name().equalsIgnoreCase(value)) {
            	return item;
	        }
	    }
        return EnumWebMode.UNDEFINED;
	}
}
