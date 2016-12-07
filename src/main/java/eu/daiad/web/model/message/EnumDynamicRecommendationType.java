package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

public enum EnumDynamicRecommendationType {
    UNDEFINED(0),
    
    LESS_SHOWER_TIME(1),
    LOWER_TEMPERATURE(2),
    LOWER_FLOW(3),
    CHANGE_SHOWERHEAD(4),
    SHAMPOO_CHANGE(5),
    REDUCE_FLOW_WHEN_NOT_NEEDED(6),
    
    // Let insights A.x begin at >= 1000 
    
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR(1000),
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR(1001),
    
    INSIGHT_A2_DAILY_CONSUMPTION_INCR(1002),
    INSIGHT_A2_DAILY_CONSUMPTION_DECR(1003),
    
    INSIGHT_A3_MORNING_CONSUMPTION_INCR(1004),
    INSIGHT_A3_MORNING_CONSUMPTION_DECR(1005),
    INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR(1006),
    INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR(1007),
    INSIGHT_A3_NIGHT_CONSUMPTION_INCR(1008),
    INSIGHT_A3_NIGHT_CONSUMPTION_DECR(1009),
    
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING(1010),
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON(1011),
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT(1012),
    
    // Let insights B.x begin at >= 1100 
    
    INSIGHT_B1_WEEKLY_CONSUMPTION_INCR(1100),
    INSIGHT_B1_WEEKLY_CONSUMPTION_DECR(1101),
    INSIGHT_B1_MONTHLY_CONSUMPTION_INCR(1102),
    INSIGHT_B1_MONTHLY_CONSUMPTION_DECR(1103),
    
    INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR(1104),
    INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR(1105),
    INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR(1106),
    INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR(1107),
    
    
    // Let insights C.x begin at >= 1200 
    
    ;
    
    private final int value;

    private EnumDynamicRecommendationType(int value) {
        this.value = value;
    }

    private static final Map<Integer, EnumDynamicRecommendationType> intToTypeMap = new HashMap<Integer, EnumDynamicRecommendationType>();
    static {
        for (EnumDynamicRecommendationType type : EnumDynamicRecommendationType.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumDynamicRecommendationType fromInteger(int value) {
        EnumDynamicRecommendationType type = intToTypeMap.get(Integer.valueOf(value));
        if (type == null)
            return EnumDynamicRecommendationType.UNDEFINED;
        return type;
    }

    public int getValue() {
        return this.value;
    }

}
