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

    INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_INCR(700, EnumRecommendationType.INSIGHT_A1),
    INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_DECR(701, EnumRecommendationType.INSIGHT_A1),
    INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_INCR(702, EnumRecommendationType.INSIGHT_A1),
    INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_DECR(703, EnumRecommendationType.INSIGHT_A1),
    
    INSIGHT_A2_METER_DAILY_CONSUMPTION_INCR(800, EnumRecommendationType.INSIGHT_A2),
    INSIGHT_A2_METER_DAILY_CONSUMPTION_DECR(801, EnumRecommendationType.INSIGHT_A2),
    INSIGHT_A2_SHOWER_DAILY_CONSUMPTION_INCR(802, EnumRecommendationType.INSIGHT_A2),
    INSIGHT_A2_SHOWER_DAILY_CONSUMPTION_DECR(803, EnumRecommendationType.INSIGHT_A2),

    INSIGHT_A3_METER_MORNING_CONSUMPTION_INCR(900, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_METER_MORNING_CONSUMPTION_DECR(901, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_METER_AFTERNOON_CONSUMPTION_INCR(902, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_METER_AFTERNOON_CONSUMPTION_DECR(903, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_METER_NIGHT_CONSUMPTION_INCR(904, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_METER_NIGHT_CONSUMPTION_DECR(905, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_SHOWER_MORNING_CONSUMPTION_INCR(906, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_SHOWER_MORNING_CONSUMPTION_DECR(907, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_SHOWER_AFTERNOON_CONSUMPTION_INCR(908, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_SHOWER_AFTERNOON_CONSUMPTION_DECR(909, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_SHOWER_NIGHT_CONSUMPTION_INCR(910, EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_SHOWER_NIGHT_CONSUMPTION_DECR(911, EnumRecommendationType.INSIGHT_A3),
    
    INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_MORNING(1000, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_AFTERNOON(1001, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_METER_CONSUMPTION_MAINLY_IN_NIGHT(1002, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_MORNING(1003, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_AFTERNOON(1004, EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_SHOWER_CONSUMPTION_MAINLY_IN_NIGHT(1005, EnumRecommendationType.INSIGHT_A4),
    
    INSIGHT_B1_METER_WEEKLY_CONSUMPTION_INCR(1100, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_METER_WEEKLY_CONSUMPTION_DECR(1101, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_METER_MONTHLY_CONSUMPTION_INCR(1102, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_METER_MONTHLY_CONSUMPTION_DECR(1103, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_SHOWER_WEEKLY_CONSUMPTION_INCR(1104, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_SHOWER_WEEKLY_CONSUMPTION_DECR(1105, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_SHOWER_MONTHLY_CONSUMPTION_INCR(1106, EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_SHOWER_MONTHLY_CONSUMPTION_DECR(1107, EnumRecommendationType.INSIGHT_B1),
       
    INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_INCR(1200, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_DECR(1201, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_METER_MONTHLY_PREV_CONSUMPTION_INCR(1202, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_METER_MONTHLY_PREV_CONSUMPTION_DECR(1203, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_INCR(1204, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_DECR(1205, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_SHOWER_MONTHLY_PREV_CONSUMPTION_INCR(1206, EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_SHOWER_MONTHLY_PREV_CONSUMPTION_DECR(1207, EnumRecommendationType.INSIGHT_B2),
    
    INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_PEAK(1300, EnumRecommendationType.INSIGHT_B3),
    INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_LOW(1301, EnumRecommendationType.INSIGHT_B3),
    INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_PEAK(1302, EnumRecommendationType.INSIGHT_B3),
    INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_LOW(1303, EnumRecommendationType.INSIGHT_B3),
    
    INSIGHT_B4_METER_MORE_ON_WEEKEND(1400, EnumRecommendationType.INSIGHT_B4),
    INSIGHT_B4_METER_LESS_ON_WEEKEND(1401, EnumRecommendationType.INSIGHT_B4),
    INSIGHT_B4_SHOWER_MORE_ON_WEEKEND(1402, EnumRecommendationType.INSIGHT_B4),
    INSIGHT_B4_SHOWER_LESS_ON_WEEKEND(1403, EnumRecommendationType.INSIGHT_B4),
    
    INSIGHT_B5_METER_MONTHLY_CONSUMPTION_INCR(1500, EnumRecommendationType.INSIGHT_B5),
    INSIGHT_B5_METER_MONTHLY_CONSUMPTION_DECR(1501, EnumRecommendationType.INSIGHT_B5),
    INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_INCR(1502, EnumRecommendationType.INSIGHT_B5),
    INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_DECR(1503, EnumRecommendationType.INSIGHT_B5),
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
            Assert.state(!intToTemplateMap.containsKey(template.value), "[Assertion failed] - Database is inconsistent");
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
