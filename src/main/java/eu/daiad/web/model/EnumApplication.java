package eu.daiad.web.model;

import java.util.HashMap;
import java.util.Map;

public enum EnumApplication {
	UNDEFINED(0), HOME(1), UTILITY(2);

	private final int value;

	private EnumApplication(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumApplication> intToTypeMap = new HashMap<Integer, EnumApplication>();
	static {
		for (EnumApplication type : EnumApplication.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumApplication fromInteger(int value) {
		EnumApplication type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumApplication.UNDEFINED;
		return type;
	}

	public static EnumApplication fromString(String value) {
		if (value != null) {
			for (EnumApplication item : EnumApplication.values()) {
				if (item.name().equalsIgnoreCase(value)) {
					return item;
				}
			}
		}

		return EnumApplication.UNDEFINED;
	}
}
