package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public enum EnumRecommendationType
{
    LESS_SHOWER_TIME(1),
    LOWER_TEMPERATURE(2),
    LOWER_FLOW(3),
    CHANGE_SHOWERHEAD(4),
    CHANGE_SHAMPOO(5),
    REDUCE_FLOW_WHEN_NOT_NEEDED(6),
    
    INSIGHT_A1(7), 
    INSIGHT_A2(8),
    INSIGHT_A3(9),
    INSIGHT_A4(10),
    
    INSIGHT_B1(11),
    INSIGHT_B2(12),
    INSIGHT_B3(13),
    INSIGHT_B4(14),
    INSIGHT_B5(15),
    ;
    
    private final int value;
    private final int priority;
    
    private EnumRecommendationType(int value) {
        this(value, 5);
    }

    private EnumRecommendationType(int value, int priority) {
        this.value = value;
        this.priority = priority;
    }
    
    private static final Map<Integer, EnumRecommendationType> intToTypeMap = new HashMap<>();
    static {
        for (EnumRecommendationType type: EnumRecommendationType.values()) {
            Assert.state(!intToTypeMap.containsKey(type.value));
            intToTypeMap.put(type.value, type);
        }
    }

    public static EnumRecommendationType valueOf(int value)
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
