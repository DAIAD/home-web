package eu.daiad.web.model.export;

import java.util.HashMap;
import java.util.Map;

public enum EnumExportDataSource {
	UNDEFINED(0), USER(1), DEVICE(2), SESSION(3), MEASUREMENT(4), METER(5);

	private final int value;

	private EnumExportDataSource(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	private static final Map<Integer, EnumExportDataSource> intToTypeMap = new HashMap<Integer, EnumExportDataSource>();
	static {
		for (EnumExportDataSource type : EnumExportDataSource.values()) {
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumExportDataSource fromInteger(int value) {
		EnumExportDataSource type = intToTypeMap.get(Integer.valueOf(value));
		if (type == null)
			return EnumExportDataSource.UNDEFINED;
		return type;
	}

	public static EnumExportDataSource fromString(String value) {
		for (EnumExportDataSource item : EnumExportDataSource.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumExportDataSource.UNDEFINED;
	}
}
