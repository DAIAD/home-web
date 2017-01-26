package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public enum EnumRecommendationTemplate
{
    LESS_SHOWER_TIME(100, EnumRecommendationType.LESS_SHOWER_TIME),

    LOWER_TEMPERATURE(200, EnumRecommendationType.LOWER_TEMPERATURE),

    LOWER_FLOW(300, EnumRecommendationType.LOWER_FLOW),

    CHANGE_SHOWERHEAD(400, EnumRecommendationType.CHANGE_SHOWERHEAD),

    CHANGE_SHAMPOO(500, EnumRecommendationType.CHANGE_SHAMPOO),

    REDUCE_FLOW_WHEN_NOT_NEEDED(600, EnumRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED),

    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR(700, EnumRecommendationType.INSIGHT_A1),
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR(701, EnumRecommendationType.INSIGHT_A1),

    INSIGHT_A2_DAILY_CONSUMPTION_INCR(800, EnumRecommendationType.INSIGHT_A2),
    INSIGHT_A2_DAILY_CONSUMPTION_DECR(801, EnumRecommendationType.INSIGHT_A2),

    INSIGHT_A3_MORNING_CONSUMPTION_INCR(900, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_MORNING_CONSUMPTION_DECR(901, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR(902, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR(903, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_NIGHT_CONSUMPTION_INCR(904, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_NIGHT_CONSUMPTION_DECR(905, EnumRecommendationType.INSIGHT_A3),

    INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING(1000, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON(1001, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT(1002, EnumRecommendationType.INSIGHT_A4),

    INSIGHT_B1_WEEKLY_CONSUMPTION_INCR(1100, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_WEEKLY_CONSUMPTION_DECR(1101, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_MONTHLY_CONSUMPTION_INCR(1102, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_MONTHLY_CONSUMPTION_DECR(1103, EnumRecommendationType.INSIGHT_B1),

    INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR(1200, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR(1201, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR(1202, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR(1203, EnumRecommendationType.INSIGHT_B2),

    INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK(1300, EnumRecommendationType.INSIGHT_B3),
    INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW(1301, EnumRecommendationType.INSIGHT_B3),

    INSIGHT_B4_MORE_ON_WEEKEND(1400, EnumRecommendationType.INSIGHT_B4),
    INSIGHT_B4_LESS_ON_WEEKEND(1401, EnumRecommendationType.INSIGHT_B4),

    INSIGHT_B5_MONTHLY_CONSUMPTION_INCR(1500, EnumRecommendationType.INSIGHT_B5),
    INSIGHT_B5_MONTHLY_CONSUMPTION_DECR(1501, EnumRecommendationType.INSIGHT_B5),
    ;

    private final EnumRecommendationType type;
    private final int value;

    private EnumRecommendationTemplate(int value, EnumRecommendationType type) {
        this.value = value;
        this.type = type;
    }

    public EnumRecommendationType getType() {
        return this.type;
    }

    private static final Map<Integer, EnumRecommendationTemplate> intToTemplateMap = new HashMap<>();
    static {
        for (EnumRecommendationTemplate template: EnumRecommendationTemplate.values()) {
            Assert.state(!intToTemplateMap.containsKey(template.value));
            intToTemplateMap.put(template.value, template);
        }
    }

    public static EnumRecommendationTemplate valueOf(int value)
    {
        return intToTemplateMap.get(value);
    }

    public int getValue() {
        return this.value;
    }
}
