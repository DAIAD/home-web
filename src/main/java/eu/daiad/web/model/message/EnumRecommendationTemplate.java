package eu.daiad.web.model.message;

public enum EnumRecommendationTemplate 
{
    LESS_SHOWER_TIME(EnumRecommendationType.LESS_SHOWER_TIME),
    
    LOWER_TEMPERATURE(EnumRecommendationType.LOWER_TEMPERATURE),
    
    LOWER_FLOW(EnumRecommendationType.LOWER_FLOW),
    
    CHANGE_SHOWERHEAD(EnumRecommendationType.CHANGE_SHOWERHEAD),
    
    CHANGE_SHAMPOO(EnumRecommendationType.CHANGE_SHAMPOO),
    
    REDUCE_FLOW_WHEN_NOT_NEEDED(EnumRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED),
    
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_A1),
    INSIGHT_A1_DAYOFWEEK_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_A1),
    
    INSIGHT_A2_DAILY_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_A2),
    INSIGHT_A2_DAILY_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_A2),
    
    INSIGHT_A3_MORNING_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_MORNING_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_NIGHT_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_A3),
    INSIGHT_A3_NIGHT_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_A3),
    
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING(EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON(EnumRecommendationType.INSIGHT_A4),
    INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT(EnumRecommendationType.INSIGHT_A4),
    
    INSIGHT_B1_WEEKLY_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_WEEKLY_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_MONTHLY_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_B1),
    INSIGHT_B1_MONTHLY_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_B1),
    
    INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_B2),
    INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_B2),
    
    INSIGHT_B3_DAYOFWEEK_CONSUMPTION_PEAK(EnumRecommendationType.INSIGHT_B3),
    INSIGHT_B3_DAYOFWEEK_CONSUMPTION_LOW(EnumRecommendationType.INSIGHT_B3),
    
    INSIGHT_B4_MORE_ON_WEEKEND(EnumRecommendationType.INSIGHT_B4),
    INSIGHT_B4_LESS_ON_WEEKEND(EnumRecommendationType.INSIGHT_B4),
    
    INSIGHT_B5_MONTHLY_CONSUMPTION_INCR(EnumRecommendationType.INSIGHT_B5),
    INSIGHT_B5_MONTHLY_CONSUMPTION_DECR(EnumRecommendationType.INSIGHT_B5),
    
    ;

    private final EnumRecommendationType type;
    
    private EnumRecommendationTemplate(EnumRecommendationType type) {
        this.type = type;
    }
    
    public EnumRecommendationType getType() {
        return this.type;
    }
}
