package eu.daiad.web.service.message;

import org.joda.time.DateTime;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;

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
     * Get consumption statistics for target utility
     * 
     * Todo: Change to getUtilityStats(DateTime)
     */
    ConsumptionStats getStats();
}
