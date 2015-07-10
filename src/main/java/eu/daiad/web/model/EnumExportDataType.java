package eu.daiad.web.model;

import java.util.HashMap;
import java.util.Map;

public enum EnumExportDataType {
		UNDEFINED(0),
		SESSION(1);
		
		private final int value;
		
		private EnumExportDataType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return this.value;
		}
		
		private static final Map<Integer, EnumExportDataType> intToTypeMap = new HashMap<Integer, EnumExportDataType>();
		static {
		    for (EnumExportDataType type : EnumExportDataType.values()) {
		        intToTypeMap.put(type.value, type);
		    }
		}

		public static EnumExportDataType fromInteger(int value) {
			EnumExportDataType type = intToTypeMap.get(Integer.valueOf(value));
		    if (type == null) 
		        return EnumExportDataType.UNDEFINED;
		    return type;
		}
		
		public static EnumExportDataType fromString(String value) {
			 for (EnumExportDataType item : EnumExportDataType.values()) {
		        if (item.name().equalsIgnoreCase(value)) {
	            	return item;
		        }
		    }
	        return EnumExportDataType.UNDEFINED;
		}
	}
