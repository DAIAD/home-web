package eu.daiad.common.domain.admin;

import java.util.HashMap;
import java.util.Map;

public enum EnumExecutionContainer {
	UNDEFINED(0), RUNTIME(1), HADOOP(2), FLINK(3);

	private final int value;

	private EnumExecutionContainer(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumExecutionContainer> intToTypeMap = new HashMap<Integer, EnumExecutionContainer>();
	static {
		for (EnumExecutionContainer type : EnumExecutionContainer.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumExecutionContainer fromInteger(int value) {
		EnumExecutionContainer type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumExecutionContainer.UNDEFINED;
		return type;
	}

	public static EnumExecutionContainer fromString(String value) {
		for (EnumExecutionContainer item : EnumExecutionContainer.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumExecutionContainer.UNDEFINED;
	}
}
