package eu.daiad.common.service.message;

import org.joda.time.DateTime;

import eu.daiad.common.model.utility.UtilityInfo;

public interface IGeneratorContext
{
    /** 
     * Get reference date 
     */
    DateTime getRefDate();
 
    /**
     * Get info for target utility
     */
    UtilityInfo getUtilityInfo();
    
    /**
     * Provide access to utility-wide statistics
     */
    IUtilityConsumptionStatisticsService getStatsService();
}
