package eu.daiad.web.model;

import java.util.Map;

public enum EnumStatistic 
{    
    // Fixme Deprecated values
    
    AVERAGE_MONTHLY,
    AVERAGE_WEEKLY,
    AVERAGE_MONTHLY_PER_SESSION, // Meaningful only for volume/duration
    AVERAGE_WEEKLY_PER_SESSION,  // Meaningful only for volume/duration
    THRESHOLD_BOTTOM_10P_MONTHLY,
    THRESHOLD_BOTTOM_10P_WEEKLY,
    THRESHOLD_BOTTOM_25P_MONTHLY,
    THRESHOLD_BOTTOM_25P_WEEKLY,
    
    // New values
    
    
    AVERAGE_PER_USER,
    AVERAGE_PER_SESSION,
    MIN,
    MAX,
    COUNT,
    SUM,
    PERCENTILE_10P_OF_USERS,
    PERCENTILE_25P_OF_USERS,
    PERCENTILE_50P_OF_USERS,
    PERCENTILE_75P_OF_USERS,
    PERCENTILE_90P_OF_USERS
    ;
}