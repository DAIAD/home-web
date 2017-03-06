package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.util.Assert;

import eu.daiad.web.model.Priority;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.singletonList;

import static eu.daiad.web.model.Priority.*;

public enum EnumRecommendationType
{
    LESS_SHOWER_TIME(1, NORMAL_PRIORITY, "R1"),
    LOWER_TEMPERATURE(2, NORMAL_PRIORITY, "R2"),
    LOWER_FLOW(3, NORMAL_PRIORITY, "R3"),
    CHANGE_SHOWERHEAD(4, NORMAL_PRIORITY, "R4"),
    CHANGE_SHAMPOO(5, NORMAL_PRIORITY, "R5"),
    REDUCE_FLOW_WHEN_NOT_NEEDED(6, NORMAL_PRIORITY, "R6"),
    
    INSIGHT_A1(7, NORMAL_PRIORITY, "IA1"), 
    INSIGHT_A2(8, NORMAL_PRIORITY, "IA2"),
    INSIGHT_A3(9, NORMAL_PRIORITY, "IA3"),
    INSIGHT_A4(10, NORMAL_PRIORITY, "IA4"),
    
    INSIGHT_B1(11, NORMAL_PRIORITY, "IB1"),
    INSIGHT_B2(12, NORMAL_PRIORITY, "IB2"),
    INSIGHT_B3(13, NORMAL_PRIORITY, "IB3"),
    INSIGHT_B4(14, NORMAL_PRIORITY, "IB4"),
    INSIGHT_B5(15, NORMAL_PRIORITY, "IB5"),
    ;
    
    private final int value;
    
    private final Priority priority;
    
    /** The list of codes associated with this recommendation-type */
    private final List<RecommendationCode> codes;

    private EnumRecommendationType(int value, Priority priority, String code1) 
    {
        this.value = value;
        this.priority = priority;
        this.codes = singletonList(RecommendationCode.valueOf(code1));
    }
    
    private EnumRecommendationType(int value, Priority priority, String code1, String... aliasedCodes) 
    {
        this.value = value;
        this.priority = priority;
        
        List<RecommendationCode> a = new ArrayList<>();
        a.add(RecommendationCode.valueOf(code1));
        for (String c: aliasedCodes)
            a.add(RecommendationCode.valueOf(c));
        this.codes = unmodifiableList(a);
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
    
    public Priority getPriority() {
        return this.priority;
    }

    public List<RecommendationCode> getCodes()
    {
        return codes;
    }
}
