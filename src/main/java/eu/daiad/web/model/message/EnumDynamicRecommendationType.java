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
    
    // Let insights begin at >= 1000 
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR(1000),
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR(1001);
    
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
