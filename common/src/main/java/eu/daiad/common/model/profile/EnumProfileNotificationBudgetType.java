package eu.daiad.common.model.profile;

import java.util.HashMap;
import java.util.Map;

public enum EnumProfileNotificationBudgetType {
	UNDEFINED(0), DAILY(1);

	private final int value;

	private EnumProfileNotificationBudgetType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumProfileNotificationBudgetType> intToTypeMap = new HashMap<Integer, EnumProfileNotificationBudgetType>();
	static {
		for (EnumProfileNotificationBudgetType type : EnumProfileNotificationBudgetType
				.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumProfileNotificationBudgetType fromInteger(int value) {
		EnumProfileNotificationBudgetType type = intToTypeMap.get(Integer
				.valueOf(value));
		if (type == null)
			return EnumProfileNotificationBudgetType.UNDEFINED;
		return type;
	}

	public static EnumProfileNotificationBudgetType fromString(String value) {
		for (EnumProfileNotificationBudgetType item : EnumProfileNotificationBudgetType
				.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumProfileNotificationBudgetType.UNDEFINED;
	}
}
