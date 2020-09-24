package eu.daiad.common.model.query;

import java.util.HashMap;
import java.util.Map;

public enum EnumMetric {
	UNDEFINED(0), COUNT(1), SUM(2), AVERAGE(3), MIN(4), MAX(5);

	private final int value;

	private EnumMetric(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumMetric> intToTypeMap = new HashMap<Integer, EnumMetric>();
	static {
		for (EnumMetric type : EnumMetric.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumMetric fromInteger(int value) {
		EnumMetric type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumMetric.UNDEFINED;
		return type;
	}

}