package eu.daiad.web.model.profile;

import java.util.HashMap;
import java.util.Map;

public enum EnumMobileMode {
	UNDEFINED(0),
	ACTIVE(1),
	INACTIVE(2),
	LEARNING(3),
	BLOCK(4);
	
	private final int value;
	
	private EnumMobileMode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	private static final Map<Integer, EnumMobileMode> intToTypeMap = new HashMap<Integer, EnumMobileMode>();
	static {
	    for (EnumMobileMode type : EnumMobileMode.values()) {
	        intToTypeMap.put(type.value, type);
	    }
	}

	public static EnumMobileMode fromInteger(int value) {
		EnumMobileMode type = intToTypeMap.get(Integer.valueOf(value));
	    if (type == null) 
	        return EnumMobileMode.UNDEFINED;
	    return type;
	}
	
	public static EnumMobileMode fromString(String value) {
		 for (EnumMobileMode item : EnumMobileMode.values()) {
	        if (item.name().equalsIgnoreCase(value)) {
            	return item;
	        }
	    }
        return EnumMobileMode.UNDEFINED;
	}
}
