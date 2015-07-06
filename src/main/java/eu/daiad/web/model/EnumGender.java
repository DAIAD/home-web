package eu.daiad.web.model;

import java.util.HashMap;
import java.util.Map;

public enum EnumGender {
	UNDEFINED(0),
	MALE(1),
	FEMALE(2);
	
	private final int value;
	
	private EnumGender(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	private static final Map<Integer, EnumGender> intToTypeMap = new HashMap<Integer, EnumGender>();
	static {
	    for (EnumGender type : EnumGender.values()) {
	        intToTypeMap.put(type.value, type);
	    }
	}

	public static EnumGender fromInteger(int value) {
		EnumGender type = intToTypeMap.get(Integer.valueOf(value));
	    if (type == null) 
	        return EnumGender.UNDEFINED;
	    return type;
	}
	
	public static EnumGender fromString(String value) {
		 for (EnumGender item : EnumGender.values()) {
	        if (item.name().equalsIgnoreCase(value)) {
            	return item;
	        }
	    }
        return EnumGender.UNDEFINED;
	}
}
