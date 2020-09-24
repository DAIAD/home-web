package eu.daiad.common.model.query;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public enum EnumClusterType {
	UNDEFINED(0, null),
	AGE(1, "Age"),
	INCOME(2, "Income"),
	HOUSEHOLD_SIZE(3, "Household Members"),
	APARTMENT_SIZE(4, "Apartment Size");

	private final int value;

	private final String name;

	private EnumClusterType(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int getValue() {
		return this.value;
	}

	public String getName() {
		return this.name;
	}

	public static EnumClusterType fromString(String value) {
		for (EnumClusterType item : EnumClusterType.values()) {
			if (item.name().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return EnumClusterType.UNDEFINED;
	}

	public static class Deserializer extends JsonDeserializer<EnumClusterType> {

		@Override
		public EnumClusterType deserialize(JsonParser parser, DeserializationContext context) throws IOException,
						JsonProcessingException {
			return EnumClusterType.fromString(parser.getValueAsString());
		}
	}
}