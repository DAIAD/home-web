package eu.daiad.web.model.message;

import static eu.daiad.web.model.Priority.NORMAL_PRIORITY;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import eu.daiad.web.model.Priority;

public enum EnumAlertType
{
	WATER_LEAK(1, NORMAL_PRIORITY, "A1"),
	SHOWER_ON(2, NORMAL_PRIORITY, "A2"),
	WATER_FIXTURES(3, NORMAL_PRIORITY, "A3"), // requires meters with high granularity.
	UNUSUAL_ACTIVITY(4, NORMAL_PRIORITY, "A4"), // requires consumption patterns
	WATER_QUALITY(5, NORMAL_PRIORITY, "A5"),
	HIGH_TEMPERATURE(6, NORMAL_PRIORITY, "A6"),
	NEAR_DAILY_BUDGET(7, NORMAL_PRIORITY, "A7", "A9"),
	NEAR_WEEKLY_BUDGET(8, NORMAL_PRIORITY, "A8", "A10"),
	REACHED_DAILY_BUDGET(9, NORMAL_PRIORITY, "A11", "A13"),
	REACHED_WEEKLY_BUDGET(10, NORMAL_PRIORITY, "A12", "A14"),
	CHAMPION(11, NORMAL_PRIORITY, "A15", "A16"),
	TOO_MUCH_WATER(12, NORMAL_PRIORITY, "A17", "A18"),
	TOO_MUCH_ENERGY(13, NORMAL_PRIORITY, "A19"),
	REDUCED_WATER_USE(14, NORMAL_PRIORITY, "A20", "A21"),
	WATER_EFFICIENCY_LEADER(15, NORMAL_PRIORITY, "A22"),
	KEEP_UP_SAVING_WATER(16, NORMAL_PRIORITY, "P1"),
	GOOD_JOB_MONTHLY(17, NORMAL_PRIORITY, "P2"),
	LITERS_ALREADY_SAVED(18, NORMAL_PRIORITY, "P3"),
	TOP_25_PERCENT_OF_SAVERS(19, NORMAL_PRIORITY, "P4"),
	TOP_10_PERCENT_OF_SAVERS(20, NORMAL_PRIORITY, "P5")
	;

	private final int value;

	private final Priority priority;

	/** The list of codes associated with this alert-type */
	private final List<AlertCode> codes;

	private EnumAlertType(int value, Priority priority, String code1)
    {
        this.value = value;
        this.priority = priority;
        this.codes = singletonList(AlertCode.valueOf(code1));
    }

	private EnumAlertType(int value, Priority priority, String code1, String... aliasedCodes)
	{
        this.value = value;
        this.priority = priority;

        List<AlertCode> a = new ArrayList<>();
        a.add(AlertCode.valueOf(code1));
        for (String c: aliasedCodes)
            a.add(AlertCode.valueOf(c));
        this.codes = unmodifiableList(a);
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

	public Priority getPriority() {
        return this.priority;
    }

    public List<AlertCode> getCodes()
    {
        return codes;
    }
}
