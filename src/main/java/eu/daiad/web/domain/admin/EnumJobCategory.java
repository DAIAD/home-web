package eu.daiad.web.domain.admin;

import java.util.HashMap;
import java.util.Map;

public enum EnumJobCategory {
	UNDEFINED(0), MAINTENANCE(1), ETL(2), ANALYSIS(3), FORECASTING(4);

	private final int value;

	private EnumJobCategory(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumJobCategory> intToTypeMap = new HashMap<Integer, EnumJobCategory>();
	static {
		for (EnumJobCategory type : EnumJobCategory.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumJobCategory fromInteger(int value) {
		EnumJobCategory type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumJobCategory.UNDEFINED;
		return type;
	}

	public static EnumJobCategory fromString(String value) {
		for (EnumJobCategory item : EnumJobCategory.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumJobCategory.UNDEFINED;
	}
}
