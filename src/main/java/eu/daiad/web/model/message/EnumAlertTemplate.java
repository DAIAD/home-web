package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public enum EnumAlertTemplate
{
    WATER_LEAK(100, EnumAlertType.WATER_LEAK),

    SHOWER_ON(200, EnumAlertType.SHOWER_ON),

    WATER_FIXTURES(300, EnumAlertType.WATER_FIXTURES),

    UNUSUAL_ACTIVITY(400, EnumAlertType.UNUSUAL_ACTIVITY),

    WATER_QUALITY(500, EnumAlertType.WATER_QUALITY),

    HIGH_TEMPERATURE(600, EnumAlertType.HIGH_TEMPERATURE),

    NEAR_DAILY_WATER_BUDGET(700, EnumAlertType.NEAR_DAILY_BUDGET),
    NEAR_DAILY_SHOWER_BUDGET(701, EnumAlertType.NEAR_DAILY_BUDGET),

    NEAR_WEEKLY_WATER_BUDGET(800, EnumAlertType.NEAR_WEEKLY_BUDGET),
    NEAR_WEEKLY_SHOWER_BUDGET(801, EnumAlertType.NEAR_WEEKLY_BUDGET),

    REACHED_DAILY_WATER_BUDGET(900, EnumAlertType.REACHED_DAILY_BUDGET),
    REACHED_DAILY_SHOWER_BUDGET(901, EnumAlertType.REACHED_DAILY_BUDGET),

    REACHED_WEEKLY_WATER_BUDGET(1000, EnumAlertType.REACHED_WEEKLY_BUDGET),
    REACHED_WEEKLY_SHOWER_BUDGET(1001, EnumAlertType.REACHED_WEEKLY_BUDGET),

    WATER_CHAMPION(1100, EnumAlertType.CHAMPION),
    SHOWER_CHAMPION(1101, EnumAlertType.CHAMPION),

    TOO_MUCH_WATER_METER(1200, EnumAlertType.TOO_MUCH_WATER),
    TOO_MUCH_WATER_SHOWER(1201, EnumAlertType.TOO_MUCH_WATER),

    TOO_MUCH_ENERGY(1300, EnumAlertType.TOO_MUCH_ENERGY),

    REDUCED_WATER_USE_METER(1400, EnumAlertType.REDUCED_WATER_USE),
    REDUCED_WATER_USE_SHOWER(1401, EnumAlertType.REDUCED_WATER_USE),

    WATER_EFFICIENCY_LEADER(1500, EnumAlertType.WATER_EFFICIENCY_LEADER),

    KEEP_UP_SAVING_WATER(1600, EnumAlertType.KEEP_UP_SAVING_WATER),

    GOOD_JOB_MONTHLY(1700, EnumAlertType.GOOD_JOB_MONTHLY),

    LITERS_ALREADY_SAVED(1800, EnumAlertType.LITERS_ALREADY_SAVED),

    TOP_25_PERCENT_OF_SAVERS(1900, EnumAlertType.TOP_25_PERCENT_OF_SAVERS),

    TOP_10_PERCENT_OF_SAVERS(2000, EnumAlertType.TOP_10_PERCENT_OF_SAVERS);

    private final EnumAlertType type;
    private final int value;

    private EnumAlertTemplate(int value, EnumAlertType type) {
        this.value = value;
        this.type = type;
    }

    public EnumAlertType getType() {
        return this.type;
    }

    private static final Map<Integer, EnumAlertTemplate> intToTemplateMap = new HashMap<>();
    static {
        for (EnumAlertTemplate template: EnumAlertTemplate.values()) {
            Assert.state(!intToTemplateMap.containsKey(template.value), "[Assertion failed] - Database is inconsistent");
            intToTemplateMap.put(template.value, template);
        }
    }

    public static EnumAlertTemplate valueOf(int value)
    {
        return intToTemplateMap.get(value);
    }

    public int getValue() {
        return this.value;
    }
}
