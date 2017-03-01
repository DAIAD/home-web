package eu.daiad.web.service.message;

import org.joda.time.DateTime;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.service.IUtilityConsumptionStatisticsService;

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
