package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

public enum EnumQueryFavouriteType {
    UNDEFINED(0), MAP(1), CHART(2), FORECAST(3);
    
	private final int value;

	private EnumQueryFavouriteType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumQueryFavouriteType> intToTypeMap = new HashMap<Integer, EnumQueryFavouriteType>();
	static {
		for (EnumQueryFavouriteType type : EnumQueryFavouriteType.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumQueryFavouriteType fromInteger(int value) {
		EnumQueryFavouriteType type = intToTypeMap.get(value);
		if (type == null)
			return EnumQueryFavouriteType.UNDEFINED;
		return type;
	}
    
}
