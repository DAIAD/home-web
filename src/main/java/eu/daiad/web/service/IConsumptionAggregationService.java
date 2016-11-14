package eu.daiad.web.service;

import org.joda.time.LocalDateTime;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IConsumptionAggregationService {

	/**
	 * Computes a set of aggregate values about the average user water consumption for a utility. This information
	 * is used for generating messages for a single user.
	 * 
	 * @param utility
	 * @param refDate a reference date for statistics to be computed. 
	 *    If null is supplied, assume the current date.
	 * @return utility-wide statistics
	 */
	public ConsumptionStats compute(UtilityInfo utility, LocalDateTime refDate);

}
