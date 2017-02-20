package eu.daiad.web.model;

public enum EnumStatistic 
{        
    AVERAGE_PER_USER, // Meaningful only for volume/duration
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