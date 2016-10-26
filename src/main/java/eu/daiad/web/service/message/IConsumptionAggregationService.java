package eu.daiad.web.service.message;

import eu.daiad.web.model.message.ConsumptionStats;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IConsumptionAggregationService {

	/**
	 * Computes a set of aggregate values about the average user water consumption for a utility. This information
	 * is used for generating messages for a single user.
	 * 
	 * @param utility
	 * @return utility-wide statistics
	 */
	public ConsumptionStats compute(UtilityInfo utility);

}
