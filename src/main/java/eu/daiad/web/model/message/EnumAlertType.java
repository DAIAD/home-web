package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public enum EnumAlertType
{
	WATER_LEAK(1),
	SHOWER_ON(2),
	WATER_FIXTURES(3), // requires meters with high granularity.
	UNUSUAL_ACTIVITY(4), // requires consumption patterns
	WATER_QUALITY(5),
	HIGH_TEMPERATURE(6),
	NEAR_DAILY_BUDGET(7),
	NEAR_WEEKLY_BUDGET(8),
	REACHED_DAILY_BUDGET(9),
	REACHED_WEEKLY_BUDGET(10),
	CHAMPION(11),
	TOO_MUCH_WATER(12),
	TOO_MUCH_ENERGY(13),
	REDUCED_WATER_USE(14),
	WATER_EFFICIENCY_LEADER(15),
	KEEP_UP_SAVING_WATER(16),
	GOOD_JOB_MONTHLY(17),
	LITERS_ALREADY_SAVED(18),
	TOP_25_PERCENT_OF_SAVERS(19),
	TOP_10_PERCENT_OF_SAVERS(20);

	private final int value;
	private final int priority;

	private EnumAlertType(int value) {
	    this(value, 5);
	}

	private EnumAlertType(int value, int priority) {
		this.value = value;
		this.priority = priority;
	}

	private static final Map<Integer, EnumAlertType> intToTypeMap = new HashMap<>();
	static {
		for (EnumAlertType type : EnumAlertType.values()) {
		    Assert.state(!intToTypeMap.containsKey(type.value));
			intToTypeMap.put(type.value, type);
		}
	}

	public static EnumAlertType valueOf(int value)
	{
		return intToTypeMap.get(value);
	}

	public int getValue() {
		return this.value;
	}

	public int getPriority() {
        return this.priority;
    }
}
